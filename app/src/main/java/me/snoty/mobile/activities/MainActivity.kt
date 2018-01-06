package me.snoty.mobile.activities

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.service.notification.StatusBarNotification
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import me.snoty.mobile.R
import me.snoty.mobile.notifications.Listener
import me.snoty.mobile.notifications.Repository
import me.snoty.mobile.processors.DebugNotification
import me.snoty.mobile.processors.ProcessorInterface
import android.view.MenuItem
import android.app.NotificationManager
import android.content.Context
import android.app.NotificationChannel


class MainActivity : AppCompatActivity(), ProcessorInterface {

    private val TAG = "MainActivity"

    private var listAdapter : NotificationsListAdapter? = null

    private val debugNotification = DebugNotification(this)

    private var demoNotificationCounter = 0

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.openSettingsItem -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private val DEMO_CHANNEL_ID: String = "Demo NotificationPostedPacket"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerNotificationChannel()

        val listView : ListView = findViewById(R.id.notificationsHistoryList)
        listAdapter = NotificationsListAdapter(this)
        listView.adapter = listAdapter

        val startServiceButton : Button = findViewById(R.id.startServiceButton)
        startServiceButton.setOnClickListener {
            if (!isPermissionGranted()) {
                val toast = Toast.makeText(this, "Listener Permission NOT granted", Toast.LENGTH_LONG)
                startActivity( Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                toast.show()
            }
            else {
                startListener()
            }
        }

        val stopServiceButton : Button = findViewById(R.id.stopServiceButton)
        stopServiceButton.setOnClickListener {
            Log.d(TAG, "clicked stopping service ...")
            val serviceIntent = Intent(this@MainActivity, Listener::class.java)
            stopService(serviceIntent)
            Listener.stop()
        }

        val testNotificationButton : Button = findViewById(R.id.testNotificationButton)
        testNotificationButton.setOnClickListener {
            val mNotifyBuilder = NotificationCompat.Builder(this@MainActivity, DEMO_CHANNEL_ID)
            mNotifyBuilder.mContentTitle = "Demo Notification"
            mNotifyBuilder.mContentText = "Notification #$demoNotificationCounter"
            mNotifyBuilder.setChannelId(DEMO_CHANNEL_ID)
            mNotifyBuilder.setSmallIcon(R.drawable.notification_icon_background)

            val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotifyMgr.notify(demoNotificationCounter++, mNotifyBuilder.build())
        }

        addDefaultProcessors()
    }

    override fun onDestroy() {
        removeDefaultProcessors()
        super.onDestroy()
    }

    private fun addDefaultProcessors() {
        Repository.addProcessor(this)
        Repository.addProcessor(debugNotification)
    }

    private fun removeDefaultProcessors() {
        //Repository.removeProcessor(this)
        //Repository.removeProcessor(debugNotification)
    }

    private fun startListener() {
        val serviceIntent = Intent(this@MainActivity, Listener::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    override fun updated(id : String, sbn : StatusBarNotification) {
    }

    override fun removed(id : String, sbn : StatusBarNotification) {

    }

    override fun created(id: String, sbn: StatusBarNotification) {
        listAdapter?.add(sbn.notification)
    }


    private fun isPermissionGranted() : Boolean {
        val cn = ComponentName(this, Listener::class.java)
        val flat = Settings.Secure.getString(this.contentResolver, "enabled_notification_listeners")
        return flat != null && flat!!.contains(cn.flattenToString())
    }


    @SuppressLint("NewApi")
    private fun registerNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            val mngr = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (mngr.getNotificationChannel(DEMO_CHANNEL_ID) != null) {
                return
            }
            //
            val channel = NotificationChannel(
                    DEMO_CHANNEL_ID,
                    DEMO_CHANNEL_ID,
                    NotificationManager.IMPORTANCE_LOW)
            // Configure the notification channel.
            channel.description = DEMO_CHANNEL_ID
            channel.enableLights(false)
            channel.enableVibration(false)
            mngr.createNotificationChannel(channel)
        }
    }

}
