package me.snoty.mobile.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.preference.PreferenceManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import me.snoty.mobile.R
import me.snoty.mobile.server.ConnectionHandler


/**
 * Created by Stefan on 27.12.2017.
 */
class Listener : NotificationListenerService() {

    private val TAG = "ListenerService"

    private var filter : Filter? = null

    companion object {

        val SERVICE_NOTIFICATION_ID: Int = 98742
        val SERVICE_CHANNEL_ID: String = "NotificationPostedPacket Listener Service"

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
        ConnectionHandler.updateServerPreferences(PreferenceManager.getDefaultSharedPreferences(this))
        return super.onBind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "listener UNbind")
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        Log.d(TAG, "listener created")
        instance = this
        if(filter == null) {
            filter = Filter(this)
        }
        super.onCreate()
    }

    @SuppressLint("NewApi")
    private fun registerNotificationChannel(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            val mngr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (mngr.getNotificationChannel(SERVICE_CHANNEL_ID) != null) {
                return
            }
            //
            val channel = NotificationChannel(
                    SERVICE_CHANNEL_ID,
                    SERVICE_CHANNEL_ID,
                    NotificationManager.IMPORTANCE_LOW)
            // Configure the notification channel.
            channel.description = SERVICE_CHANNEL_ID
            channel.enableLights(false)
            channel.enableVibration(false)
            mngr.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "starting service ...")

        registerNotificationChannel(this)

        val mNotifyBuilder = NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
        mNotifyBuilder.setOngoing(true)
        mNotifyBuilder.mContentTitle = "Snoty NotificationPostedPacket Listener"
        mNotifyBuilder.mContentText = "Service up and running ..."
        mNotifyBuilder.setChannelId(SERVICE_CHANNEL_ID)
        mNotifyBuilder.setSmallIcon(R.drawable.notification_icon_background)

        this.startForeground(SERVICE_NOTIFICATION_ID, mNotifyBuilder.build())

        if (android.os.Build.VERSION.SDK_INT >= 24) {
            Log.d(TAG, "requesting rebind ...")
            val componentName = ComponentName(applicationContext, Listener::class.java)
            requestRebind(componentName)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onListenerConnected() {
        Log.d(TAG, "Listener connected")
        super.onListenerConnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if(filter!!.shouldIgnore(sbn)) {
            Log.d(TAG, "IGNORING " + sbn?.packageName)
            return
        }

        if(sbn != null) {
            Repository.instance.add(sbn)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?, rankingMap: RankingMap?, reason: Int) {
        if(sbn?.packageName == this.packageName) return

        if(sbn != null) {
            Repository.instance.remove(sbn)
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "stopping service ...")

        NotificationManagerCompat.from(this).cancel(SERVICE_NOTIFICATION_ID)

        stopForeground(true)

        super.onDestroy()
    }
}