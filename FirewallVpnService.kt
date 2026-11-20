package com.dataguard.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import java.io.FileInputStream
import java.util.concurrent.atomic.AtomicBoolean

class FirewallVpnService : VpnService() {
    companion object {
        const val ACTION_START = "com.dataguard.app.START"
        const val ACTION_STOP = "com.dataguard.app.STOP"
        private const val CHANNEL_ID = "data_guard_firewall"
        private const val NOTIFICATION_ID = 7401
    }

    private var tun: ParcelFileDescriptor? = null
    private var worker: Thread? = null
    private val running = AtomicBoolean(false)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopFirewall()
            ACTION_START -> startFirewall()
        }
        return START_STICKY
    }

    private fun startFirewall() {
        if (running.get()) stopTunnelOnly()
        val blocked = getSharedPreferences("firewall", MODE_PRIVATE)
            .getStringSet("blocked", emptySet()) ?: emptySet()
        if (blocked.isEmpty()) {
            stopSelf()
            return
        }
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        try {
            val builder = Builder()
                .setSession("DATA GUARD Firewall")
                .setMtu(1500)
                .addAddress("10.10.0.1", 32)
                .addRoute("0.0.0.0", 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.addAddress("fd00:1:fd00:1::1", 128)
                builder.addRoute("::", 0)
            }
            blocked.forEach { builder.addAllowedApplication(it) }
            tun = builder.establish()
            if (tun == null) {
                stopSelf()
                return
            }
            running.set(true)
            worker = Thread {
                try {
                    FileInputStream(tun!!.fileDescriptor).use { input ->
                        val packet = ByteArray(32767)
                        while (running.get()) {
                            val count = input.read(packet)
                            if (count < 0) break
                            // Intentionally discard packets from selected apps: no forwarding or remote server.
                        }
                    }
                } catch (_: Exception) {
                    // Tunnel may close during a user-requested stop.
                }
            }.apply { name = "DataGuardPacketDropper"; start() }
        } catch (_: Exception) {
            stopTunnelOnly()
            stopSelf()
        }
    }

    private fun stopFirewall() {
        stopTunnelOnly()
        stopForeground(true)
        stopSelf()
    }

    private fun stopTunnelOnly() {
        running.set(false)
        try { tun?.close() } catch (_: Exception) {}
        tun = null
        worker = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "DATA GUARD Firewall", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
        )
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("DATA GUARD firewall imewashwa")
                .setContentText("Programu ulizochagua zinazuiwa kupata intaneti")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setContentIntent(openApp)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("DATA GUARD firewall imewashwa")
                .setContentText("Programu ulizochagua zinazuiwa kupata intaneti")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setContentIntent(openApp)
                .build()
        }
    }

    override fun onDestroy() {
        stopTunnelOnly()
        super.onDestroy()
    }
}
