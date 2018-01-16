package me.snoty.mobile.activities

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import android.support.v4.app.RemoteInput
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import me.snoty.mobile.Cryptography
import me.snoty.mobile.R
import me.snoty.mobile.ServerPreferences
import me.snoty.mobile.notifications.ListenerHandler
import me.snoty.mobile.notifications.ListenerService
import me.snoty.mobile.processors.HistoryList
import me.snoty.mobile.processors.history.NotificationHistoryItem
import me.snoty.mobile.server.connection.ConnectionHandler
import java.util.*


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    companion object {
        var instance : MainActivity? = null
    }

    private var listAdapter : NotificationsListAdapter? = null

    private var demoNotificationCounter = 0

    private val DEMO_CHANNEL_ID: String = "Demo NotificationPostedPacket"

    private val TEXT_INTENT_KEY: String = "textIntentKey"

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
                startActivity(Intent(this, CertificateScannerActivity::class.java))
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        instance = this

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerNotificationChannel()

        setButtonClickHandlers()
        initNotificationsHistoryList()

        updateViews()
    }

    override fun onResume() {
        updateViews()
        super.onResume()
    }

    private fun initNotificationsHistoryList() {
        val listView: ListView = findViewById(R.id.notificationsHistoryList)
        this.listAdapter = NotificationsListAdapter(this)
        listView.adapter = listAdapter
    }

    private fun setButtonClickHandlers() {
        val startServiceButton: Button = findViewById(R.id.startServiceButton)
        startServiceButton.setOnClickListener {
            if (!checkListenerPermissionGranted()) {
                val toast = Toast.makeText(this, "Listener Permission not granted", Toast.LENGTH_LONG)
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
            showDemoNotification()
        }

        val grantListenerButton = findViewById<Button>(R.id.buttonGrantListener)
        grantListenerButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        val scanCodeButton = findViewById<Button>(R.id.buttonScanCode)
        scanCodeButton.setOnClickListener {
            startActivity(Intent(this, CertificateScannerActivity::class.java))
        }

    }

    private fun showDemoNotification() {
        val mNotifyBuilder = NotificationCompat.Builder(this@MainActivity, DEMO_CHANNEL_ID)
        mNotifyBuilder.mContentTitle = "Demo Notification"
        mNotifyBuilder.mContentText = "Notification #$demoNotificationCounter"
        mNotifyBuilder.setChannelId(DEMO_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon_background)
        val mainActivityIntent = Intent(this, this::class.java)
        mainActivityIntent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        mainActivityIntent.putExtra("demo_id", demoNotificationCounter)

        val appIntent = PendingIntent.getActivity(this, 0, Intent(this, this::class.java), PendingIntent.FLAG_CANCEL_CURRENT)
        val openAppAction = NotificationCompat.Action(12, "Open App", appIntent)
        val uniqueInt = (System.currentTimeMillis() and 0xfffffff).toInt()
        val textIntent = PendingIntent.getActivity(this, uniqueInt,
                mainActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        val remoteInput = RemoteInput.Builder(TEXT_INTENT_KEY)
                .setLabel("Some Text")
                .build()
        val enterTextAction = NotificationCompat.Action
                .Builder(12, "Enter Text", textIntent)
                .addRemoteInput(remoteInput)
                .build()
        mNotifyBuilder
                .addAction(openAppAction)
                .addAction(enterTextAction)
                .setAutoCancel(true)

        val chromeLaunchIntent = packageManager.getLaunchIntentForPackage("com.android.chrome")
        if(chromeLaunchIntent != null) {
            val chromeIntent = PendingIntent.getActivity(this, 0, chromeLaunchIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            val chromeAction = NotificationCompat.Action(12, "Start Chrome", chromeIntent)
            mNotifyBuilder.addAction(chromeAction)
        }


        val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotifyMgr.notify(demoNotificationCounter, mNotifyBuilder.build())
        demoNotificationCounter++
    }

    private fun updateNotificationsHistoryList() {
        this.run {
            if (HistoryList.instance != null) {
                listAdapter?.clear()
                listAdapter?.addAll(HistoryList.instance?.historyList)
            }
        }
    }

    private fun checkListenerPermissionGranted() : Boolean {
        val cn = ComponentName(this, ListenerService::class.java)
        val flat = Settings.Secure.getString(this.contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(cn.flattenToString())
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

    fun addToNotificationList(n : NotificationHistoryItem) {
        Log.d(TAG, "adding history item to view")
        listAdapter?.insert(n, 0)
    }

    fun updateViews() {
        this.runOnUiThread { // necessary to update views from other threads
            updateNotificationsHistoryList()

            updateStatusProgresses()

            val label = findViewById<TextView>(R.id.statusTextView)
            if(ConnectionHandler.instance.connected) {
                label.setTextColor(Color.BLACK)
                label.text = "Connected"
            }
            else {
                /*
                // show QR certificate scan if no server set
                if(ConnectionHandler.instance.lastConnectionError ==
                        me.snoty.mobile.server.connection.ConnectionHandler.ConnectionError.NO_SERVER_SET) {
                    startActivity(Intent(this, CertificateScannerActivity::class.java))
                }*/

                label.setTextColor(Color.RED)
                label.text = ConnectionHandler.instance.lastConnectionError?.name ?: ""
            }
        }
    }

    fun updateStatusProgresses() {
        val listenerVisibility = if(checkListenerPermissionGranted()) View.GONE
                                 else View.VISIBLE
        findViewById<LinearLayout>(R.id.statusListener).visibility = listenerVisibility

        val codeVisibility = if(ServerPreferences.instance.getFingerprint() != "") View.GONE
                             else View.VISIBLE
        findViewById<LinearLayout>(R.id.statusCode).visibility = codeVisibility

        val serverVisibility =
                if(ConnectionHandler.instance.connected) View.GONE else View.VISIBLE
        findViewById<LinearLayout>(R.id.statusConnection).visibility = serverVisibility


    }

    override fun onNewIntent(intent: Intent?) {
        Log.d(TAG, "received intent in MainActivity")
        val results = RemoteInput.getResultsFromIntent(intent)
        if (results != null) {
            val quickReplyResult = results.getCharSequence(TEXT_INTENT_KEY)
            Log.d(TAG, "remote input: $quickReplyResult")
            Toast.makeText(this, "entered: $quickReplyResult", Toast.LENGTH_LONG).show()
        }
        val demo_id = intent?.getIntExtra("demo_id", -1) ?: -1
        if(demo_id != -1) {
            val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotifyMgr.cancel(demo_id)
        }
        super.onNewIntent(intent)
    }

}
