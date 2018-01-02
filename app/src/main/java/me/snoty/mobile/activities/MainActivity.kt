package me.snoty.mobile.activities

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.service.notification.StatusBarNotification
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import me.snoty.mobile.R
import me.snoty.mobile.notifications.Listener
import me.snoty.mobile.notifications.Repository
import me.snoty.mobile.plugins.DebugNotification
import me.snoty.mobile.plugins.PluginInterface


class MainActivity : AppCompatActivity(), PluginInterface {

    private val TAG = "MainActivity"

    private var listAdapter : NotificationsListAdapter? = null

    private val debugNotification = DebugNotification(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                Repository.addPlugin(this)
                Repository.addPlugin(debugNotification)
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

}
