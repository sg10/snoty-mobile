package me.snoty.mobile.notifications

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import me.snoty.mobile.R
import android.app.NotificationManager
import android.app.NotificationChannel
import android.content.BroadcastReceiver
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi
import android.provider.Settings.Secure
import android.content.ComponentName
import android.provider.Settings
import android.widget.Toast
import me.snoty.mobile.plugins.PluginInterface


/**
 * Created by Stefan on 27.12.2017.
 */
class Listener : NotificationListenerService() {

    private val TAG = "ListenerService"
    companion object {

        var serviceNotificationId: Int = 98742
        val channelId : String = "SnotyService"

        var countPosted = 0
        var countRemoved = 0

        private var instance: Listener? = null

        fun stop() {
            if (android.os.Build.VERSION.SDK_INT >= 24) {
                instance?.requestUnbind()
            }
            else {
                instance?.stopForeground(true)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "listener bind")
        return super.onBind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "listener UNbind")
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        Log.d(TAG, "listener created")
        instance = this
        super.onCreate()
    }

    @SuppressLint("NewApi")
    private fun registerNotificationChannel(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            val mngr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (mngr.getNotificationChannel(channelId) != null) {
                return
            }
            //
            val channel = NotificationChannel(
                    channelId,
                    channelId,
                    NotificationManager.IMPORTANCE_LOW)
            // Configure the notification channel.
            channel.description = channelId
            channel.enableLights(false)
            channel.enableVibration(false)
            mngr.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "starting service ...")

        registerNotificationChannel(this)

        val mNotifyBuilder = NotificationCompat.Builder(this, channelId)
        mNotifyBuilder.setOngoing(true)
        mNotifyBuilder.mContentTitle = "Snoty Notification Listener"
        mNotifyBuilder.mContentText = "Service up and running ..."
        mNotifyBuilder.setSmallIcon(R.drawable.notification_icon_background)

        this.startForeground(serviceNotificationId, mNotifyBuilder.build())

        if (android.os.Build.VERSION.SDK_INT >= 24) {
            Log.d(TAG, "requesting rebind ...")
            val componentName = ComponentName(applicationContext, Listener::class.java)
            requestRebind(componentName)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onListenerConnected() {
        Log.d(TAG, "Listener connected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if(sbn?.packageName == this.packageName) return

        countPosted++

        if(sbn != null) {
            Repository.instance.add(sbn)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?, rankingMap: RankingMap?, reason: Int) {
        if(sbn?.packageName == this.packageName) return

        countRemoved++

        if(sbn != null) {
            Repository.instance.remove(sbn)
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "stopping service ...")

        NotificationManagerCompat.from(this).cancel(serviceNotificationId)

        stopForeground(true)

        super.onDestroy()
    }
}