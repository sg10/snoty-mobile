package me.snoty.mobile.activities

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.CheckBox
import android.widget.ListView
import android.widget.Toast
import me.snoty.mobile.R
import me.snoty.mobile.notifications.ListenerHandler
import me.snoty.mobile.notifications.ListenerService
import me.snoty.mobile.processors.HistoryList
import me.snoty.mobile.processors.history.NotificationHistoryItem
import me.snoty.mobile.server.ConnectionHandler


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private var listAdapter : NotificationsListAdapter? = null

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
            R.id.scanCertificateItem -> {
                scanCertificate()
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

        setButtonClickHandlers()
        initNotificationsHistoryList()
        updateNotificationsHistoryList()
    }

    override fun onResume() {
        updateNotificationsHistoryList()
        val checkBox = findViewById<CheckBox>(R.id.serverConnectionValidCheckBox)
        checkBox.isChecked = ConnectionHandler.isServerConnectionPossible()
        super.onResume()
    }

    private fun updateNotificationsHistoryList() {
        if (HistoryList.instance != null) {
            listAdapter?.clear()
            listAdapter?.addAll(HistoryList.instance?.historyList)
            HistoryList.instance?.setMainActivity(this@MainActivity)
        }
    }

    private fun initNotificationsHistoryList() {
        val listView: ListView = findViewById(R.id.notificationsHistoryList)
        this.listAdapter = NotificationsListAdapter(this)
        listView.adapter = listAdapter
    }

    private fun setButtonClickHandlers() {
        val startServiceButton: Button = findViewById(R.id.startServiceButton)
        startServiceButton.setOnClickListener {
            if (!isPermissionGranted()) {
                val toast = Toast.makeText(this, "Listener Permission NOT granted", Toast.LENGTH_LONG)
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                toast.show()
            } else {
                ListenerHandler.start(this)
                updateNotificationsHistoryList()
            }
        }

        val stopServiceButton: Button = findViewById(R.id.stopServiceButton)
        stopServiceButton.setOnClickListener {
            Log.d(TAG, "clicked stopping service ...")
            val serviceIntent = Intent(this@MainActivity, ListenerHandler::class.java)
            stopService(serviceIntent)
            ListenerHandler.stop()
        }

        val testNotificationButton: Button = findViewById(R.id.testNotificationButton)
        testNotificationButton.setOnClickListener {
            val mNotifyBuilder = NotificationCompat.Builder(this@MainActivity, DEMO_CHANNEL_ID)
            mNotifyBuilder.mContentTitle = "Demo Notification"
            mNotifyBuilder.mContentText = "Notification #$demoNotificationCounter"
            mNotifyBuilder.setChannelId(DEMO_CHANNEL_ID)
            mNotifyBuilder.setSmallIcon(R.drawable.notification_icon_background)

            val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotifyMgr.notify(demoNotificationCounter++, mNotifyBuilder.build())
        }
    }

    private fun isPermissionGranted() : Boolean {
        val cn = ComponentName(this, ListenerService::class.java)
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

    private fun scanCertificate() {
        val intent = Intent(this, CertificateScannerActivity::class.java)
        startActivity(intent)
    }

    fun addToNotificationList(n : NotificationHistoryItem) {
        Log.d(TAG, "adding history item to view")
        listAdapter?.insert(n, 0)
    }

}
