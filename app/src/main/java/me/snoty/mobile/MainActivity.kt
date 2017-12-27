package me.snoty.mobile

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import me.snoty.mobile.notifications.Listener
import me.snoty.mobile.notifications.Repository

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startServiceButton : Button = findViewById(R.id.startServiceButton)
        startServiceButton.setOnClickListener {
            val serviceIntent = Intent(this, Listener::class.java)
            if(android.os.Build.VERSION.SDK_INT >= 26) {
                startForegroundService(serviceIntent)
            }
            else {
                startService(serviceIntent)
            }
        }

        val stopServiceButton : Button = findViewById(R.id.stopServiceButton)
        stopServiceButton.setOnClickListener {
            val serviceIntent = Intent(this, Listener::class.java)
            stopService(serviceIntent)
        }
    }

    override fun onResume() {
        super.onResume()

        val logView = findViewById<TextView>(R.id.logView)
        logView.text = Repository.log
    }
}
