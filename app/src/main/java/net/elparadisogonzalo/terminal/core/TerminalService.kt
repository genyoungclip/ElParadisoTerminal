package net.elparadisogonzalo.terminal.core

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import net.elparadisogonzalo.terminal.ElpApplication
import net.elparadisogonzalo.terminal.R
import net.elparadisogonzalo.terminal.ui.TerminalActivity

class TerminalService : Service() {

    private val binder = LocalBinder()
    val sessions = mutableListOf<TerminalSession>()

    inner class LocalBinder : Binder() { fun service(): TerminalService = this@TerminalService }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    fun newSession(shell: String = "bash"): TerminalSession {
        val s = TerminalSession(this, shell)
        sessions += s
        s.start()
        return s
    }

    fun killAll() {
        sessions.forEach { it.finish() }
        sessions.clear()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(): Notification {
        val pi = PendingIntent.getActivity(
            this, 0, Intent(this, TerminalActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, ElpApplication.CHANNEL_TERMINAL)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_running))
            .setSmallIcon(R.drawable.ic_terminal)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }

    companion object { const val NOTIFICATION_ID = 4711 }
}
