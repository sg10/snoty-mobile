package me.snoty.mobile.processors

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.StatusBarNotification
import android.support.v4.app.NotificationCompat
import me.snoty.mobile.R
import me.snoty.mobile.activities.MainActivity
import me.snoty.mobile.notifications.ListenerHandler
import me.snoty.mobile.notifications.ListenerService


/**
 * Created by Stefan on 01.01.2018.
 */
class DebugNotification(context : Context) : ProcessorInterface {

    private val context : Context = context
    private var countPosted = 0
    private var countRemoved = 0
    private var countUpdated = 0

    override fun created(id : String, sbn : StatusBarNotification) {
        countPosted++
        updateServiceNotification()
    }

    override fun removed(id : String, sbn : StatusBarNotification) {
        countRemoved++
        updateServiceNotification()
    }

    override fun updated(id : String, sbn : StatusBarNotification) {
        countRemoved++
        updateServiceNotification()
    }

    @SuppressLint("NewApi")
    private fun updateServiceNotification() {
        if (Build.VERSION.SDK_INT >= 23) {
            val mNotificationManager = this.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notificationText = "Posted: $countPosted, Updated: $countUpdated, Removed: $countRemoved"
            val mNotifyBuilder = NotificationCompat.Builder(context, ListenerService.SERVICE_CHANNEL_ID)
            mNotifyBuilder.setOngoing(true)
            mNotifyBuilder.mContentTitle = "Snoty NotificationPostedPacket Listener"
            mNotifyBuilder.mContentText = notificationText
            mNotifyBuilder.setSmallIcon(R.drawable.notification_icon_background)

            val notificationIntent = Intent(context, MainActivity::class.java)
            notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            notificationIntent.action = Intent.ACTION_MAIN
            mNotifyBuilder.setContentIntent(PendingIntent.getActivity(context, 0, notificationIntent, 0))

            mNotificationManager.notify(ListenerService.SERVICE_NOTIFICATION_ID, mNotifyBuilder.build())
        }
    }


}