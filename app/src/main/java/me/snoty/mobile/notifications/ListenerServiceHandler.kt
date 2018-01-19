package me.snoty.mobile.notifications

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import me.snoty.mobile.ContextHelper
import me.snoty.mobile.Prerequisites
import me.snoty.mobile.server.connection.ConnectionHandler


/**
 * Created by Stefan on 27.12.2017.
 */
class ListenerServiceHandler {

    companion object {

        private val TAG = "Listener"

        fun start(context : Context) {
            Log.d(TAG, "supposed to start listener service")

            var listenerInstance = ListenerService.instance

            if(! Prerequisites.isFullyInitialized()) {
                Toast
                    .makeText(ContextHelper.get(), "Please take initialize first", Toast.LENGTH_LONG)
                    .show()
            }
            else if(listenerInstance == null) {
                Log.d(TAG, "cold start")
                val serviceIntent = Intent(context, ListenerService::class.java)
                if (Build.VERSION.SDK_INT >= 26) {
                    Log.d(TAG, context.startForegroundService(serviceIntent)?.toString())
                } else {
                    context.startService(serviceIntent)
                }
            }
            else if(!listenerInstance.isStarted()) {
                Log.d(TAG, "restart")
                listenerInstance.setStarted()
            }
            else if(!listenerInstance.listenerConnected) {
                Toast.makeText(listenerInstance,
                        "Could not link to notification listener (zombie service)",
                        Toast.LENGTH_LONG)
                        .show()
                Log.e(TAG, "Could not link to notification listener (zombie service)")
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

            // ConnectionHandler.instance.disconnect()
        }
    }
}