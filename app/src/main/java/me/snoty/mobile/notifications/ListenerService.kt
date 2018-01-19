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
import me.snoty.mobile.ContextHelper
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

    private var started = true

    private val listenerServiceNotification = ListenerServiceNotification()

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
        return super.onBind(intent)
    }

    override fun onCreate() {
        instance = this
        if (Filter.instance == null) {
            Filter()
        }
        Repository.reset()

        // create new key pair at this point
        // if it doesn't exist yet
        if(!Cryptography.instance.keysAlreadyGenerated()) {
            Toast.makeText(ContextHelper.get(), "Generating keys ...", Toast.LENGTH_LONG).show()
            Thread {
                try {
                    Cryptography.instance.createKeys()
                } catch (ex: Exception) {
                    Log.e(TAG, ex.message)
                }
            }.run()
        }

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
        ConnectionHandler.instance.start()
        started = true
        Log.d(TAG, "ListenerService started (flag)")
        val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotifyMgr.notify(SERVICE_NOTIFICATION_ID, listenerServiceNotification.create())
    }

    fun setStopped() {
        ConnectionHandler.instance.stop()
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

        this.startForeground(SERVICE_NOTIFICATION_ID, listenerServiceNotification.create())

        return Service.START_STICKY
    }



    fun updateServerConnectedStatus() {
        if(!started) return

        val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotifyMgr?.notify(SERVICE_NOTIFICATION_ID, listenerServiceNotification.create())
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if(!started) return

        if (Filter.instance?.shouldIgnore(sbn) == true) {
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

    override fun onListenerDisconnected() {
        onDestroy()
        super.onListenerDisconnected()
    }

    override fun onDestroy() {
        Log.d(TAG, "destroying service ...")

        NotificationManagerCompat.from(this).cancel(SERVICE_NOTIFICATION_ID)
        stopForeground(true)
        instance = null

        super.onDestroy()
    }
}