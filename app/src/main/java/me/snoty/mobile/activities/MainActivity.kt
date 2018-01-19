package me.snoty.mobile.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import me.snoty.mobile.Prerequisites
import me.snoty.mobile.R
import me.snoty.mobile.Utils
import me.snoty.mobile.notifications.DemoNotification
import me.snoty.mobile.notifications.ListenerServiceHandler
import me.snoty.mobile.processors.HistoryList
import me.snoty.mobile.processors.history.NotificationHistoryItem
import me.snoty.mobile.server.connection.ConnectionHandler


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    companion object {
        var instance : MainActivity? = null
    }

    private var listAdapter : NotificationsListAdapter? = null

    private var inForeground : Boolean = false

    private var demoNotification : DemoNotification = DemoNotification(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        instance = this

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setButtonClickHandlers()
        initNotificationsHistoryList()
    }

    override fun onResume() {
        inForeground = true
        updateViews()
        super.onResume()
    }

    override fun onPause() {
        inForeground = false
        super.onPause()
    }

    override fun onDestroy() {
        if(instance == this) {
            instance = null
        }
        super.onDestroy()
    }

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

    private fun setButtonClickHandlers() {
        // Initialize app: permission, certificate scanning, ...
        //
        findViewById<Button>(R.id.buttonGrantListener).setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
        findViewById<Button>(R.id.buttonScanCode).setOnClickListener {
            startActivity(Intent(this, CertificateScannerActivity::class.java))
        }
        findViewById<Button>(R.id.buttonScanCode2).setOnClickListener {
            startActivity(Intent(this, CertificateScannerActivity::class.java))
        }

        // Start & Stop Service
        //
        findViewById<Button>(R.id.startServiceButton).setOnClickListener {
            ListenerServiceHandler.start(this)
            updateNotificationsHistoryList()
        }
        findViewById<Button>(R.id.stopServiceButton).setOnClickListener {
            Log.d(TAG, "clicked stopping service ...")
            ListenerServiceHandler.stop()
        }

        // Show demo notification
        //
        findViewById<Button>(R.id.demoNotificationButton).setOnClickListener {
            demoNotification.showNew()
        }

    }

    private fun initNotificationsHistoryList() {
        val listView: ListView = findViewById(R.id.notificationsHistoryList)
        this.listAdapter = NotificationsListAdapter(this)
        listView.adapter = listAdapter
    }

    private fun updateNotificationsHistoryList() {
        if (HistoryList.instance != null) {
            listAdapter?.clear()
            listAdapter?.addAll(HistoryList.instance?.historyList)
        }
    }

    fun addToNotificationList(n : NotificationHistoryItem) {
        Log.d(TAG, "adding history item to view")
        listAdapter?.insert(n, 0)
    }

    fun updateViews() {
        // do right now if in foreground
        if(inForeground) {
            this.runOnUiThread {
                updateInitSteps()

                updateNotificationsHistoryList()

                val label = findViewById<TextView>(R.id.statusTextView)
                if (ConnectionHandler.instance.connected) {
                    label.text = "Connected"
                } else {
                    label.text = ConnectionHandler.instance.lastConnectionError?.name ?: ""
                }
            }
        }

        // otherwise postpone until activity is shown the next time
    }

    private fun updateInitSteps() {
        findViewById<LinearLayout>(R.id.statusListener)
                .visibility = Utils.viewGoneIfTrue(Prerequisites.hasListenerPermission())

        findViewById<LinearLayout>(R.id.statusCode)
                .visibility = Utils.viewGoneIfTrue(Prerequisites.wasCertificateScanned())

        findViewById<LinearLayout>(R.id.statusFingerprint)
                .visibility = Utils.viewGoneIfTrue(!Prerequisites.wasCertificateScanned()
                    || Prerequisites.fingerprintValid)

        findViewById<LinearLayout>(R.id.statusConnection)
                .visibility = Utils.viewGoneIfTrue(ConnectionHandler.instance.connected)
    }

    override fun onNewIntent(intent: Intent?) {
        Log.d(TAG, "received intent in MainActivity")
        demoNotification.checkIntent(intent)
        super.onNewIntent(intent)
    }

}
