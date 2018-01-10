package me.snoty.mobile.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import me.snoty.mobile.R
import me.snoty.mobile.server.ConnectionHandler
import java.io.DataInputStream.readUTF
import android.system.Os.accept
import java.io.DataInputStream
import java.io.IOException
import java.io.PrintStream
import javax.net.ssl.SSLServerSocket
import javax.net.ssl.SSLServerSocketFactory
import javax.net.ssl.SSLSocket


/**
 * Created by Stefan on 27.12.2017.
 */
class ListenerHandler {

    companion object {

        private val TAG = "Listener"

        fun start(context : Context) {
            Log.d(TAG, "supposed to start listener service")

            var instance = ListenerService.instance

            if(instance == null) {
                Log.d(TAG, "cold start")
                val serviceIntent = Intent(context, ListenerService::class.java)
                if (Build.VERSION.SDK_INT >= 26) {
                    Log.d(TAG, context.startForegroundService(serviceIntent)?.toString())
                } else {
                    context.startService(serviceIntent)
                }
            }
            else if(!instance.isStarted()) {
                Log.d(TAG, "restart")
                instance.setStarted()
            }
            else {
                Log.d(TAG, "listener already running")
            }
        }

        fun stop() {
            var instance = ListenerService.instance

            if(instance == null || !instance.isStarted()) {
                Log.d(TAG, "listener not running")
            }
            else if(instance.isStarted()) {
                instance.setStopped()
            }
            else {
                Log.e(TAG, "unknown error")
            }
        }
    }
}