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
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi


/**
 * Created by Stefan on 27.12.2017.
 */
class Listener : NotificationListenerService() {

    private val TAG = "ListenerService"
    private val channelId : String = "SnotyService"

    private var serviceNotificationId: Int = 98742

    override fun onNotificationRemoved(sbn: StatusBarNotification?, rankingMap: RankingMap?, reason: Int) {
        super.onNotificationRemoved(sbn, rankingMap, reason)
    }

    override fun onBind(intent: Intent?): IBinder {
        return super.onBind(intent)
    }

    @SuppressLint("NewApi")
    fun registerNotificationChannel(context: Context) {
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

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?, rankingMap: RankingMap?) {
        super.onNotificationPosted(sbn, rankingMap)

        val extras = sbn?.notification?.extras

        Repository.log =
                sbn?.packageName + "\n" +
                extras?.getString("android.title") + "\n" +
                extras?.getString("android.text") +"\n\n" +
                Repository.log
    }

    override fun onDestroy() {
        Log.d(TAG, "stopping service ...")

        super.onDestroy()
        NotificationManagerCompat.from(this).cancel(serviceNotificationId)
    }
}