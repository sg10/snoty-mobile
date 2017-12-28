package me.snoty.mobile.activities

import android.app.Notification
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import me.snoty.mobile.notifications.Listener
import me.snoty.mobile.notifications.Repository
import android.content.ComponentName
import android.os.Build
import android.provider.Settings
import android.widget.ListView
import android.widget.Toast
import me.snoty.mobile.R
import me.snoty.mobile.plugins.PluginInterface


class MainActivity : AppCompatActivity(), PluginInterface {
    override val commandFilter = null

    private val TAG = "MainActivity"

    private var listAdapter : NotificationsListAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listView : ListView = findViewById<ListView>(R.id.notificationsHistoryList)
        listAdapter = NotificationsListAdapter(this)
        listView.adapter = listAdapter

        val startServiceButton : Button = findViewById(R.id.startServiceButton)
        startServiceButton.setOnClickListener {
            if (!isPermissionGranted()) {
                val toast = Toast.makeText(this, "Listener Permission NOT granted", Toast.LENGTH_LONG)
                startActivity( Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                toast.show()
            }
            else {
                Listener.addPlugin(this)
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

    override fun posted(n : Notification) {
        listAdapter?.add(n)
    }

    override fun removed(n : Notification) {

    }


    private fun isPermissionGranted() : Boolean {
        val cn = ComponentName(this, Listener::class.java)
        val flat = Settings.Secure.getString(this.contentResolver, "enabled_notification_listeners")
        return flat != null && flat!!.contains(cn.flattenToString())
    }

}
