package me.snoty.mobile.notifications

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import me.snoty.mobile.ContextHelper
import me.snoty.mobile.R
import me.snoty.mobile.activities.MainActivity
import me.snoty.mobile.server.connection.ConnectionHandler

/**
 * Created by Stefan on 19.01.2018.
 */
class ListenerServiceNotification {

    fun create(): Notification {
        val context = ContextHelper.get()

        registerNotificationChannel(context)

        val text = if(ConnectionHandler.instance.connected) "Connected" else "Disconnected"

        val mNotifyBuilder = NotificationCompat.Builder(context, ListenerService.SERVICE_CHANNEL_ID)
        mNotifyBuilder.mContentTitle = "Snoty Notification Listener"
        mNotifyBuilder.mContentText = text
        mNotifyBuilder.priority = NotificationManager.IMPORTANCE_LOW
        mNotifyBuilder
                .setOngoing(true)
                .setChannelId(ListenerService.SERVICE_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon_background)

        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        notificationIntent.action = Intent.ACTION_MAIN
        mNotifyBuilder.setContentIntent(PendingIntent.getActivity(context, 0, notificationIntent, 0))

        return mNotifyBuilder.build()
    }

    @SuppressLint("NewApi") // IDE doesn't get if clause
    private fun registerNotificationChannel(context : Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mngr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (mngr.getNotificationChannel(ListenerService.SERVICE_CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                        ListenerService.SERVICE_CHANNEL_ID,
                        ListenerService.SERVICE_CHANNEL_ID,
                        NotificationManager.IMPORTANCE_LOW)
                // Configure the notification channel.
                channel.description = ListenerService.SERVICE_CHANNEL_ID
                channel.enableLights(false)
                channel.enableVibration(false)
                channel.importance = NotificationManager.IMPORTANCE_LOW
                mngr.createNotificationChannel(channel)
            }
        }
    }


}