package me.snoty.mobile.notifications

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import me.snoty.mobile.server.connection.ConnectionHandler


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

            ConnectionHandler.instance.disconnect()
        }
    }
}