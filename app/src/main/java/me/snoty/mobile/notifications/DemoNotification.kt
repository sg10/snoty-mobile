package me.snoty.mobile.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.RemoteInput
import android.util.Log
import android.widget.Toast
import me.snoty.mobile.R
import me.snoty.mobile.activities.MainActivity

/**
 * Created by Stefan on 19.01.2018.
 */
class DemoNotification constructor(private val mainActivity: MainActivity) {

    private var demoNotificationCounter = 0


    companion object {
        val TAG = "DemoNotification"

        val DEMO_CHANNEL_ID: String = "Demo Notifications"
        val TEXT_INTENT_KEY: String = "textIntentKey"
    }


    fun showNew() {
        showDemoNotification()
    }

    private fun showDemoNotification() {
        registerNotificationChannel()

        val mNotifyBuilder = NotificationCompat.Builder(mainActivity, DEMO_CHANNEL_ID)
        mNotifyBuilder.mContentTitle = "Demo Notification"
        mNotifyBuilder.mContentText = "Notification #$demoNotificationCounter"
        mNotifyBuilder.setChannelId(DEMO_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon_background)
                .setAutoCancel(true)

        // notification action: open main activity
        val mainActivityIntent = Intent(mainActivity, MainActivity::class.java)
        mainActivityIntent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        mainActivityIntent.putExtra("demo_id", demoNotificationCounter)
        val appIntent = PendingIntent.getActivity(mainActivity, 0, Intent(mainActivity, MainActivity::class.java), PendingIntent.FLAG_CANCEL_CURRENT)
        val openAppAction = NotificationCompat.Action(12, "Open App", appIntent)
        mNotifyBuilder.addAction(openAppAction)

        // notification action: enter text
        val uniqueInt = (System.currentTimeMillis() and 0xfffffff).toInt()
        val textIntent = PendingIntent.getActivity(mainActivity, uniqueInt,
                mainActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        val remoteInput = RemoteInput.Builder(TEXT_INTENT_KEY)
                .setLabel("Some Text")
                .build()
        val enterTextAction = NotificationCompat.Action
                .Builder(12, "Enter Text", textIntent)
                .addRemoteInput(remoteInput)
                .build()
        mNotifyBuilder.addAction(enterTextAction)

        // notification action: open chrome (show only if it is installed)
        val chromeLaunchIntent = mainActivity.packageManager.getLaunchIntentForPackage("com.android.chrome")
        if(chromeLaunchIntent != null) {
            val chromeIntent = PendingIntent.getActivity(mainActivity, 0, chromeLaunchIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            val chromeAction = NotificationCompat.Action(12, "Start Chrome", chromeIntent)
            mNotifyBuilder.addAction(chromeAction)
        }

        val mNotifyMgr = mainActivity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotifyMgr.notify(demoNotificationCounter, mNotifyBuilder.build())

        demoNotificationCounter++
    }

    @SuppressLint("NewApi") // stupid IDE doesn't get the if clause regarding the build version
    private fun registerNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mngr = mainActivity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (mngr.getNotificationChannel(DEMO_CHANNEL_ID) != null) {
                return
            }

            val channel = NotificationChannel(
                            DEMO_CHANNEL_ID,
                            DEMO_CHANNEL_ID,
                            NotificationManager.IMPORTANCE_LOW)

            channel.description = DEMO_CHANNEL_ID
            channel.enableLights(false)
            channel.enableVibration(false)
            mngr.createNotificationChannel(channel)
        }
    }

    fun checkIntent(intent: Intent?) {
        if(intent == null) {
            return
        }

        val results = RemoteInput.getResultsFromIntent(intent)
        if (results != null) {
            val quickReplyResult = results.getCharSequence(DemoNotification.TEXT_INTENT_KEY)
            Log.d(TAG, "remote input: $quickReplyResult")
            Toast.makeText(mainActivity, "entered: $quickReplyResult", Toast.LENGTH_LONG).show()
        }
        val demoId = intent?.getIntExtra("demo_id", -1)
        if(demoId != -1) {
            val mNotifyMgr = mainActivity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotifyMgr.cancel(demoId)
        }

    }

}