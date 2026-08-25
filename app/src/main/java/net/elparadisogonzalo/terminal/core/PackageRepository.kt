package net.elparadisogonzalo.terminal.core

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.elparadisogonzalo.terminal.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Talks to https://elp.elparadisogonzalo.net/apt/  which is a standard
 * dpkg/apt repo layout:
 *   apt/dists/stable/main/binary-<arch>/Packages.gz
 *   apt/pool/main/<pkg>/<pkg>_<ver>_<arch>.deb
 */
object PackageRepository {

    data class PackageInfo(
        val name: String,
        val version: String,
        val description: String,
        val size: Long,
        val depends: List<String> = emptyList()
    )

    private lateinit var context: Context
    private val client = OkHttpClient()

    fun init(ctx: Context) { context = ctx.applicationContext }

    suspend fun listAvailable(): List<PackageInfo> = withContext(Dispatchers.IO) {
        val url = "${BuildConfig.REPO_URL}dists/stable/main/binary-${arch()}/Packages"
        val req = Request.Builder().url(url).build()
        val body = client.newCall(req).execute().use { it.body?.string().orEmpty() }
        parsePackages(body)
    }

    private fun parsePackages(text: String): List<PackageInfo> {
        val out = mutableListOf<PackageInfo>()
        val blocks = text.split(Regex("\\n\\n"))
        for (b in blocks) {
            val map = HashMap<String, String>()
            for (line in b.lines()) {
                val idx = line.indexOf(':')
                if (idx > 0) map[line.substring(0, idx).trim()] =
                    line.substring(idx + 1).trim()
            }
            val name = map["Package"] ?: continue
            out += PackageInfo(
                name        = name,
                version     = map["Version"] ?: "",
                description = map["Description"] ?: "",
                size        = map["Size"]?.toLongOrNull() ?: 0,
                depends     = map["Depends"]?.split(",")?.map { it.trim() }.orEmpty()
            )
        }
        return out
    }

    /** Preferred packages the user can 1-tap install from the UI. */
    val featured = listOf(
        "python"     to "Python 3.12 — general-purpose language & stdlib",
        "nodejs"     to "Node.js 20 — server-side JavaScript",
        "ruby"       to "Ruby 3.3 — dynamic, expressive language",
        "golang"     to "Go 1.22 — Google's systems language",
        "rust"       to "Rust 1.79 — memory-safe systems language",
        "openjdk-21" to "OpenJDK 21 — Java runtime & compiler",
        "php"        to "PHP 8.3 — web scripting language",
        "clang"      to "Clang / LLVM — C / C++ compiler",
        "perl"       to "Perl 5.38",
        "lua"        to "Lua 5.4",
        "r-base"     to "R 4.4 — statistical computing",
        "julia"      to "Julia 1.10",
        "powershell" to "PowerShell 7.4 (pwsh)",
        "zsh"        to "Z shell",
        "fish"       to "Fish shell",
        "git"        to "Git version control",
        "nano"       to "nano editor",
        "vim"        to "Vim editor",
        "neovim"     to "Neovim editor",
        "tmux"       to "Terminal multiplexer",
        "openssh"    to "OpenSSH client & server",
        "curl"       to "curl HTTP client",
        "wget"       to "wget downloader",
        "ffmpeg"     to "FFmpeg media toolkit",
        "imagemagick" to "ImageMagick image toolkit",
        "sqlite"     to "SQLite database",
        "postgresql" to "PostgreSQL server",
        "nginx"      to "nginx web server",
        "docker"     to "Docker CLI",
        "kubectl"    to "Kubernetes CLI"
    )

    private fun arch(): String = when (android.os.Build.SUPPORTED_ABIS.first()) {
        "arm64-v8a"   -> "aarch64"
        "armeabi-v7a" -> "arm"
        "x86_64"      -> "x86_64"
        else          -> "i686"
    }
}
