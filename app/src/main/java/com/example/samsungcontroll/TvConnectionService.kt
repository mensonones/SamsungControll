package com.example.samsungcontroll

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.media.VolumeProvider
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

/**
 * Foreground service whose only job is to keep the app process alive (at foreground
 * priority) while a TV connection is active, so the WebSocket owned by
 * [RemoteViewModel] is not frozen or dropped when the app goes to the background.
 *
 * It holds no connection logic itself: the ViewModel starts it once a connection is
 * established and stops it as soon as the connection is closed or lost.
 */
class TvConnectionService : Service() {

    private var volumeSession: MediaSession? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        // The user swiped the app away: drop the keep-alive so no orphan
        // notification lingers after the process goes away.
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val label = intent?.getStringExtra(EXTRA_TV_LABEL)?.takeIf { it.isNotBlank() }
            ?: getString(R.string.app_name)
        startForegroundCompat(buildNotification(label))
        startVolumeSession()
        // If the system kills us under memory pressure, do not recreate the service:
        // reconnection is handled by the app when it returns to the foreground.
        return START_NOT_STICKY
    }

    /**
     * A remote-playback media session makes the system route the hardware volume buttons to us
     * even with the screen off; each press is forwarded to the app, which sends the TV key.
     */
    private fun startVolumeSession() {
        if (volumeSession != null) return
        val session = MediaSession(this, "TvVolume")
        session.setPlaybackState(
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_PLAYING, 0L, 0f)
                .build()
        )
        session.setPlaybackToRemote(object : VolumeProvider(VOLUME_CONTROL_RELATIVE, 100, 50) {
            override fun onAdjustVolume(direction: Int) {
                if (direction == 0) return
                sendBroadcast(
                    Intent(ACTION_VOLUME)
                        .setPackage(packageName)
                        .putExtra(EXTRA_VOLUME_DIRECTION, direction)
                )
            }
        })
        session.isActive = true
        volumeSession = session
    }

    override fun onDestroy() {
        volumeSession?.release()
        volumeSession = null
        super.onDestroy()
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(label: String): Notification {
        ensureChannel()

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val disconnectIntent = Intent(ACTION_DISCONNECT).setPackage(packageName)
        val disconnectPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            disconnectIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_tv)
            .setColor(ContextCompat.getColor(this, R.color.remote_accent))
            .setContentTitle(getString(R.string.notification_connected_title))
            .setContentText(getString(R.string.notification_connected_text, label))
            .setContentIntent(contentIntent)
            .addAction(
                0,
                getString(R.string.notification_action_disconnect),
                disconnectPendingIntent
            )
            .setOngoing(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = getString(R.string.notification_channel_description)
                    setShowBadge(false)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    companion object {
        /** Broadcast action fired when the user taps "Disconnect" on the notification. */
        const val ACTION_DISCONNECT = "com.example.samsungcontroll.action.DISCONNECT"

        /** Broadcast action carrying a hardware volume press ([EXTRA_VOLUME_DIRECTION] = +1 / -1). */
        const val ACTION_VOLUME = "com.example.samsungcontroll.action.VOLUME"
        const val EXTRA_VOLUME_DIRECTION = "direction"

        private const val CHANNEL_ID = "tv_connection"
        private const val NOTIFICATION_ID = 1001
        private const val EXTRA_TV_LABEL = "tv_label"

        fun start(context: Context, tvLabel: String?) {
            val intent = Intent(context, TvConnectionService::class.java).apply {
                putExtra(EXTRA_TV_LABEL, tvLabel)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TvConnectionService::class.java))
        }
    }
}
