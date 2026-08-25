package net.elparadisogonzalo.terminal.core

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.elparadisogonzalo.terminal.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

/**
 * Downloads the base rootfs (busybox, bash, coreutils, dpkg, apt) from
 * https://elp.elparadisogonzalo.net/bootstrap/<abi>.zip and unpacks it into
 * $PREFIX = /data/data/net.elparadisogonzalo.terminal/files/usr
 */
object BootstrapInstaller {

    private const val TAG = "ElpBootstrap"

    lateinit var prefixDir: File
        private set
    lateinit var homeDir: File
        private set

    fun init(context: Context) {
        val filesDir = context.filesDir
        prefixDir = File(filesDir, "usr")
        homeDir   = File(filesDir, "home")
        if (!homeDir.exists()) homeDir.mkdirs()
    }

    fun isInstalled(): Boolean =
        File(prefixDir, "bin/bash").exists() || File(prefixDir, "bin/sh").exists()

    suspend fun install(onProgress: (String) -> Unit): Boolean = withContext(Dispatchers.IO) {
        try {
            val abi = pickAbi()
            val url = "${BuildConfig.BOOTSTRAP_URL}bootstrap-$abi.zip"
            onProgress("Downloading $url")

            val client  = OkHttpClient()
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) {
                    onProgress("HTTP ${resp.code}")
                    return@withContext false
                }
                val zip = File(prefixDir.parentFile, "bootstrap.zip")
                zip.parentFile?.mkdirs()
                FileOutputStream(zip).use { fos ->
                    resp.body?.byteStream()?.copyTo(fos)
                }
                onProgress("Extracting rootfs …")
                extract(zip, prefixDir)
                zip.delete()
            }
            symlinkPass()
            onProgress("Bootstrap complete")
            true
        } catch (t: Throwable) {
            Log.e(TAG, "install failed", t)
            onProgress("Failed: ${t.message}")
            false
        }
    }

    private fun extract(zipFile: File, outDir: File) {
        if (!outDir.exists()) outDir.mkdirs()
        ZipInputStream(zipFile.inputStream()).use { zin ->
            var entry = zin.nextEntry
            while (entry != null) {
                val out = File(outDir, entry.name)
                if (entry.isDirectory) {
                    out.mkdirs()
                } else {
                    out.parentFile?.mkdirs()
                    FileOutputStream(out).use { zin.copyTo(it) }
                    // Executable bits for anything under bin/, libexec/, sbin/
                    if (entry.name.contains("bin/") || entry.name.contains("libexec/")) {
                        out.setExecutable(true, false)
                    }
                }
                entry = zin.nextEntry
            }
        }
    }

    /** Second-pass: read SYMLINKS.txt in $PREFIX and rebuild symlinks. */
    private fun symlinkPass() {
        val list = File(prefixDir, "SYMLINKS.txt")
        if (!list.exists()) return
        list.forEachLine { line ->
            val parts = line.split("←")
            if (parts.size == 2) {
                val target = parts[0].trim()
                val link   = File(prefixDir, parts[1].trim())
                try {
                    if (link.exists()) link.delete()
                    Runtime.getRuntime().exec(arrayOf("ln", "-s", target, link.absolutePath))
                } catch (_: Throwable) {}
            }
        }
    }

    private fun pickAbi(): String {
        val supported = Build.SUPPORTED_ABIS
        return when {
            supported.contains("arm64-v8a")   -> "aarch64"
            supported.contains("armeabi-v7a") -> "arm"
            supported.contains("x86_64")      -> "x86_64"
            else                              -> "i686"
        }
    }
}
