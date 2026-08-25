package net.elparadisogonzalo.terminal.core

import android.util.Log
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Wraps a PTY / forked shell process. Native code is provided by libelpterm.so
 * (see app/src/main/cpp).
 */
class TerminalSession(
    private val service: TerminalService,
    private val shell: String
) {
    private var pid    : Int = -1
    private var fd     : Int = -1

    var output: FileInputStream? = null
        private set
    var input : FileOutputStream? = null
        private set

    fun start() {
        val prefix = BootstrapInstaller.prefixDir.absolutePath
        val home   = BootstrapInstaller.homeDir.absolutePath
        val shellBin = "$prefix/bin/$shell"

        val env = arrayOf(
            "TERM=xterm-256color",
            "HOME=$home",
            "PREFIX=$prefix",
            "PATH=$prefix/bin:$prefix/bin/applets:/system/bin",
            "LD_LIBRARY_PATH=$prefix/lib",
            "LANG=en_US.UTF-8",
            "COLORTERM=truecolor",
            "SHELL=$shellBin",
            "TMPDIR=$prefix/tmp",
            "ELP_HOMEPAGE=https://elp.elparadisogonzalo.net"
        )

        val result = ElpNative.createSubprocess(
            shellBin,
            home,
            arrayOf(shellBin, "-l"),
            env,
            80, 24
        )
        pid = result[0]
        fd  = result[1]

        val pfd = ElpNative.dupFd(fd)
        output = FileInputStream(pfd)
        input  = FileOutputStream(pfd)

        Log.i(TAG, "Started $shell pid=$pid fd=$fd")
    }

    fun write(data: ByteArray) { input?.write(data); input?.flush() }

    fun resize(cols: Int, rows: Int) {
        if (fd >= 0) ElpNative.setPtyWindowSize(fd, rows, cols, 0, 0)
    }

    fun finish() {
        if (pid > 0) ElpNative.killSubprocess(pid, 9)
        pid = -1
        fd  = -1
    }

    companion object { private const val TAG = "ElpSession" }
}

/** JNI façade — implemented by src/main/cpp/elpterm.cpp */
object ElpNative {
    init { System.loadLibrary("elpterm") }

    external fun createSubprocess(
        cmd: String, cwd: String, args: Array<String>, envVars: Array<String>,
        cols: Int, rows: Int
    ): IntArray

    external fun killSubprocess(pid: Int, signal: Int)
    external fun setPtyWindowSize(fd: Int, rows: Int, cols: Int, hPixels: Int, vPixels: Int)
    external fun dupFd(fd: Int): FileDescriptor
    external fun waitFor(pid: Int): Int
}
