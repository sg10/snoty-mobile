package me.snoty.mobile.notifications

import android.annotation.SuppressLint
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
    private val channelId : String = "SnotyService"

    private var serviceNotificationId: Int = 98742

    companion object {
        private var instance: Listener? = null

        private val pluginsList : ArrayList<PluginInterface> = ArrayList()

        fun addPlugin(plugin : PluginInterface) {
            if(!pluginsList.contains(plugin)) {
                pluginsList.add(plugin)
            }
        }

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
            val componentName = ComponentName(applicationContext, Listener::class.java)
            requestRebind(componentName)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onListenerConnected() {
        Log.d(TAG, "Listener connected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        Log.d(TAG, "NOTIFICATION POSTED")

        for(plugin in Listener.pluginsList) {
            if(plugin.isApplicable("command") && sbn?.notification != null) {
                plugin.posted(sbn?.notification)
            }
        }

        val extras = sbn?.notification?.extras

        val notificationSummary = sbn?.packageName + "\n" +
                extras?.getString("android.title") + "\n" +
                extras?.getString("android.text")

        Log.d(TAG, notificationSummary)

        Repository.log = notificationSummary + "\n\n" + Repository.log
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?, rankingMap: RankingMap?, reason: Int) {
        Log.d(TAG, "NOTIFICATION REMOVED")

        for(plugin in Listener.pluginsList) {
            if(plugin.isApplicable("command") && sbn?.notification != null) {
                plugin.removed(sbn?.notification)
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "stopping service ...")

        NotificationManagerCompat.from(this).cancel(serviceNotificationId)

        stopForeground(true)

        super.onDestroy()
    }
}