package me.snoty.mobile.notifications

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.preference.PreferenceManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import android.widget.Toast
import me.snoty.mobile.Cryptography
import me.snoty.mobile.R
import me.snoty.mobile.activities.MainActivity
import me.snoty.mobile.server.connection.ConnectionHandler

/**
 * Created by Stefan on 09.01.2018.
 */
class ListenerService : NotificationListenerService() {

    companion object {
        var instance: ListenerService? = null
        private val TAG = "ListenerService"

        val SERVICE_NOTIFICATION_ID: Int = 98742
        val SERVICE_CHANNEL_ID: String = "NotificationPostedPacket Listener Service"
    }

    private var filter : Filter? = null

    private var started = true

    // due to an Android cache bug, sometimes the service
    // becomes inaccessible (zombie service) and the app can't
    // reconnect anymore
    // todo: link stackoverflow page
    var listenerConnected = false
        private set

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "listener bind")
        instance = this
        setStarted()
        ConnectionHandler.instance.updateServerPreferences()
        return super.onBind(intent)
    }

    override fun onCreate() {
        instance = this
        if (filter == null) {
            filter = Filter(this)
        }
        Repository.reset()

        val handler = Handler()
        handler.postDelayed({
            // create new key pair
            // (we had to initalize the key store somewhere, so why not here ...)
            try {
                Cryptography.instance.createKeys()
            } catch (ex: Exception) {
                Log.e(TAG, ex.message)
            }
        }, 500)

        super.onCreate()
    }

    override fun onListenerConnected() {
        Log.d(TAG, "Listener connected")
        this.listenerConnected = true
        instance = this
        super.onListenerConnected()
    }

    fun setStarted() {
        ConnectionHandler.instance.updateServerPreferences()
        started = true
        Log.d(TAG, "ListenerService started (flag)")
        val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotifyMgr.notify(SERVICE_NOTIFICATION_ID, createNotification().build())
    }

    fun setStopped() {
        started = false
        Log.d(TAG, "ListenerService stopped (flag)")
        val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotifyMgr.cancel(SERVICE_NOTIFICATION_ID)
    }

    fun isStarted() : Boolean {
        return started
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "starting service ...")
        val mNotifyBuilder = createNotification()

        this.startForeground(SERVICE_NOTIFICATION_ID, mNotifyBuilder.build())

        return Service.START_STICKY
    }

    @SuppressLint("NewApi") // IDE doesn't recognize if() concerning build?
    private fun createNotification(): NotificationCompat.Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mngr = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (mngr.getNotificationChannel(SERVICE_CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                        SERVICE_CHANNEL_ID,
                        SERVICE_CHANNEL_ID,
                        NotificationManager.IMPORTANCE_LOW)
                // Configure the notification channel.
                channel.description = SERVICE_CHANNEL_ID
                channel.enableLights(false)
                channel.enableVibration(false)
                channel.importance = NotificationManager.IMPORTANCE_LOW
                mngr.createNotificationChannel(channel)
            }
        }

        val text = if(ConnectionHandler.instance.connected) "Connected" else "Disconnected"

        val mNotifyBuilder = NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
        mNotifyBuilder.mContentTitle = "Snoty Notification Listener"
        mNotifyBuilder.mContentText = text
        mNotifyBuilder.priority = NotificationManager.IMPORTANCE_LOW
        mNotifyBuilder
                .setOngoing(true)
                .setChannelId(SERVICE_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon_background)

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        notificationIntent.action = Intent.ACTION_MAIN
        mNotifyBuilder.setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, 0))

        return mNotifyBuilder
    }

    fun updateServerConnectedStatus() {
        if(started) {
            val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotifyMgr?.notify(SERVICE_NOTIFICATION_ID, createNotification()?.build())
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if(!started) return

        if (filter!!.shouldIgnore(sbn)) {
            Log.d(TAG, "IGNORING " + sbn?.packageName)
            return
        }

        if (sbn != null) {
            Repository.instance.add(sbn)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?, rankingMap: RankingMap?, reason: Int) {
        if(!started) return

        if (sbn != null) {
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