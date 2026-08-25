package net.elparadisogonzalo.terminal

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import net.elparadisogonzalo.terminal.core.BootstrapInstaller
import net.elparadisogonzalo.terminal.core.PackageRepository

class ElpApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
        BootstrapInstaller.init(this)
        PackageRepository.init(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(NotificationManager::class.java)
            mgr.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_TERMINAL,
                    "Terminal Sessions",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
            mgr.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_BOOTSTRAP,
                    "Bootstrap & Packages",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
    }

    companion object {
        const val CHANNEL_TERMINAL  = "elp.terminal.session"
        const val CHANNEL_BOOTSTRAP = "elp.terminal.bootstrap"
        lateinit var instance: ElpApplication
            private set
    }
}
