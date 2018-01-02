package me.snoty.mobile.plugins

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.service.notification.StatusBarNotification
import android.support.v4.app.NotificationCompat
import me.snoty.mobile.R
import me.snoty.mobile.notifications.Listener
import me.snoty.mobile.notifications.Repository

/**
 * Created by Stefan on 01.01.2018.
 */
class DebugNotification(context : Context) : PluginInterface {

    private val context : Context = context

    override fun created(id : String, sbn : StatusBarNotification) {
        updateServiceNotification()
    }

    override fun removed(id : String, sbn : StatusBarNotification) {
        updateServiceNotification()
    }

    override fun updated(id : String, sbn : StatusBarNotification) {
        updateServiceNotification()
    }

    @SuppressLint("NewApi")
    private fun updateServiceNotification() {
        if (Build.VERSION.SDK_INT >= 23) {
            val mNotificationManager = this.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notificationText = "Posted: "+Listener.countPosted+", Removed: "+Listener.countRemoved
            val mNotifyBuilder = NotificationCompat.Builder(context, Listener.channelId)
            mNotifyBuilder.setOngoing(true)
            mNotifyBuilder.mContentTitle = "Snoty Notification Listener"
            mNotifyBuilder.mContentText = notificationText
            mNotifyBuilder.setSmallIcon(R.drawable.notification_icon_background)

            mNotificationManager.notify(Listener.serviceNotificationId, mNotifyBuilder.build())
        }
    }


}