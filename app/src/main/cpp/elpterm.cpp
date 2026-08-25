/*
 * El Paradiso Terminal — JNI PTY layer.
 * Fork() + openpty() + execve() a child shell and expose file descriptors
 * back to Kotlin. Compatible with the Termux JNI ABI so the terminal-view
 * module can be dropped in unchanged.
 */
#include <jni.h>
#include <pty.h>
#include <utmp.h>
#include <fcntl.h>
#include <unistd.h>
#include <signal.h>
#include <termios.h>
#include <sys/ioctl.h>
#include <sys/wait.h>
#include <cstdlib>
#include <cstring>
#include <android/log.h>

#define LOG_TAG "elpterm"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jintArray JNICALL
Java_net_elparadisogonzalo_terminal_core_ElpNative_createSubprocess(
        JNIEnv* env, jobject /* this */,
        jstring cmd_j, jstring cwd_j,
        jobjectArray args_j, jobjectArray env_j,
        jint cols, jint rows) {

    const char* cmd = env->GetStringUTFChars(cmd_j, nullptr);
    const char* cwd = env->GetStringUTFChars(cwd_j, nullptr);

    int argc = env->GetArrayLength(args_j);
    char** argv = (char**) calloc(argc + 1, sizeof(char*));
    for (int i = 0; i < argc; ++i) {
        auto s = (jstring) env->GetObjectArrayElement(args_j, i);
        argv[i] = strdup(env->GetStringUTFChars(s, nullptr));
    }

    int envc = env->GetArrayLength(env_j);
    char** envv = (char**) calloc(envc + 1, sizeof(char*));
    for (int i = 0; i < envc; ++i) {
        auto s = (jstring) env->GetObjectArrayElement(env_j, i);
        envv[i] = strdup(env->GetStringUTFChars(s, nullptr));
    }

    int ptm = -1;
    struct winsize ws{}; ws.ws_row = rows; ws.ws_col = cols;
    pid_t pid = forkpty(&ptm, nullptr, nullptr, &ws);
    if (pid < 0) { LOGE("forkpty failed"); return nullptr; }
    if (pid == 0) {
        if (chdir(cwd) != 0) LOGE("chdir(%s) failed", cwd);
        execve(cmd, argv, envv);
        _exit(127);
    }
    fcntl(ptm, F_SETFD, FD_CLOEXEC);

    jintArray out = env->NewIntArray(2);
    jint vals[2] = { (jint) pid, (jint) ptm };
    env->SetIntArrayRegion(out, 0, 2, vals);

    env->ReleaseStringUTFChars(cmd_j, cmd);
    env->ReleaseStringUTFChars(cwd_j, cwd);
    return out;
}

extern "C" JNIEXPORT void JNICALL
Java_net_elparadisogonzalo_terminal_core_ElpNative_killSubprocess(
        JNIEnv*, jobject, jint pid, jint signal) {
    kill(pid, signal);
}

extern "C" JNIEXPORT void JNICALL
Java_net_elparadisogonzalo_terminal_core_ElpNative_setPtyWindowSize(
        JNIEnv*, jobject, jint fd, jint rows, jint cols, jint hp, jint vp) {
    struct winsize ws{};
    ws.ws_row = rows; ws.ws_col = cols;
    ws.ws_xpixel = hp; ws.ws_ypixel = vp;
    ioctl(fd, TIOCSWINSZ, &ws);
}

extern "C" JNIEXPORT jobject JNICALL
Java_net_elparadisogonzalo_terminal_core_ElpNative_dupFd(
        JNIEnv* env, jobject, jint fd) {
    jclass cls = env->FindClass("java/io/FileDescriptor");
    jmethodID ctor = env->GetMethodID(cls, "<init>", "()V");
    jfieldID  descriptor = env->GetFieldID(cls, "descriptor", "I");
    jobject o = env->NewObject(cls, ctor);
    env->SetIntField(o, descriptor, dup(fd));
    return o;
}

extern "C" JNIEXPORT jint JNICALL
Java_net_elparadisogonzalo_terminal_core_ElpNative_waitFor(
        JNIEnv*, jobject, jint pid) {
    int status = 0;
    waitpid(pid, &status, 0);
    return status;
}
