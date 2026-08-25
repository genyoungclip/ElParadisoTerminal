package net.elparadisogonzalo.terminal.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import net.elparadisogonzalo.terminal.R
import net.elparadisogonzalo.terminal.core.BootstrapInstaller
import net.elparadisogonzalo.terminal.core.TerminalService
import net.elparadisogonzalo.terminal.databinding.ActivityTerminalBinding

class TerminalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTerminalBinding
    private var service: TerminalService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as TerminalService.LocalBinder).service()
            openInitialSession()
        }
        override fun onServiceDisconnected(name: ComponentName?) { service = null }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTerminalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        if (!BootstrapInstaller.isInstalled()) {
            lifecycleScope.launch {
                binding.terminalView.appendBanner("Installing rootfs from elp.elparadisogonzalo.net …\n")
                val ok = BootstrapInstaller.install { line ->
                    runOnUiThread { binding.terminalView.appendBanner("$line\n") }
                }
                if (ok) startTerminalService()
                else Toast.makeText(this@TerminalActivity, R.string.bootstrap_failed, Toast.LENGTH_LONG).show()
            }
        } else startTerminalService()

        binding.extraKeys.setOnKeyPress { seq ->
            service?.sessions?.firstOrNull()?.write(seq.toByteArray())
        }
    }

    private fun startTerminalService() {
        val i = Intent(this, TerminalService::class.java)
        startForegroundService(i)
        bindService(i, connection, BIND_AUTO_CREATE)
    }

    private fun openInitialSession() {
        val s = service?.newSession("bash") ?: return
        binding.terminalView.attach(s)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu); return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_new_session -> { service?.let {
                binding.terminalView.attach(it.newSession("bash")) }; true
            }
            R.id.action_pwsh        -> { service?.let {
                binding.terminalView.attach(it.newSession("pwsh")) }; true
            }
            R.id.action_zsh         -> { service?.let {
                binding.terminalView.attach(it.newSession("zsh")) }; true
            }
            R.id.action_packages    -> {
                startActivity(Intent(this, PackageManagerActivity::class.java)); true
            }
            R.id.action_settings    -> {
                startActivity(Intent(this, SettingsActivity::class.java)); true
            }
            R.id.action_homepage    -> {
                startActivity(Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://elp.elparadisogonzalo.net"))); true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        try { unbindService(connection) } catch (_: Throwable) {}
        super.onDestroy()
    }
}
