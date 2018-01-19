package me.snoty.mobile.server.connection

import android.os.AsyncTask
import android.util.Log
import me.snoty.mobile.ServerPreferences
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.security.SecureRandom
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/**
 * Created by Stefan on 19.01.2018.
 */
class ConnectionChecker {

    private val TAG ="ConnCheck"

    var socket : SSLSocket? = null
        private set

    private var task : CheckTask? = null

    var started : Boolean = false

    constructor() {
        poke()
    }

    fun poke() {
        started = true
        if(task == null || task?.isCancelled == true) {
            val task = CheckTask(this)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            this.task = task
        }
    }

    fun resetSocket() {
        socket?.close()
        socket = null
    }

    private class CheckTask
        constructor(private val connectionChecker: ConnectionChecker)
        : AsyncTask<Void, Void, Void?>() {

        private val TAG ="ConnCheck"

        private val TIMEOUT_SOCKET = 5000
        private val TIMEOUT_WHEN_CONNECTED = 5000L
        private val TIMEOUT_WHEN_DISCONNECTED = 3000L

        override fun doInBackground(vararg p0: Void?): Void? {
           while(true) {
               // in case task was started a second time, kill
               if(connectionChecker.task != this) {
                   this.cancel(true)
                   break
               }

               if(!connectionChecker.started) {
                   Thread.sleep(TIMEOUT_WHEN_DISCONNECTED)
                   continue
               }
               else if(isConnected()) {
                   Thread.sleep(TIMEOUT_WHEN_CONNECTED)
               }
               else {
                   createSocket()
                   Thread.sleep(TIMEOUT_WHEN_DISCONNECTED)
               }
           }

           return null
        }

        private fun isConnected() : Boolean {
            var isConnected = false
            try {
                isConnected = connectionChecker.socket != null
                        && connectionChecker.socket?.isConnected == true
            }
            catch(ex : Exception) {
                ConnectionHandler.instance.handleSocketException(ex)
            }

            if(isConnected) {
                ConnectionHandler.instance.onConnected()
            }
            else {
                ConnectionHandler.instance.onDisconnected()
            }

            return isConnected
        }

        private fun createSocket() {
            try {
                val sslContext = SSLContext.getInstance("TLSv1.2")
                sslContext.init(null, arrayOf(ConnectionHandler.instance.trustManager), SecureRandom())
                val ssf: SSLSocketFactory = sslContext.socketFactory

                val serverAddress = ServerPreferences.instance.getAddress()
                val inetAddress = Inet4Address.getByName(serverAddress)
                val port = ServerPreferences.instance.getPort()

                //Log.d(TAG, "establishing connection to $inetAddress:$port ...")
                val newSocket = ssf.createSocket() as SSLSocket
                newSocket.connect(InetSocketAddress(inetAddress, port), TIMEOUT_SOCKET)

                newSocket.startHandshake()

                Log.d(TAG, "socket created "+newSocket.isConnected)

                ConnectionHandler.instance.onConnected()

                connectionChecker.socket = newSocket
            }
            catch(ex : Exception) {
                ConnectionHandler.instance.handleSocketException(ex)
            }
        }
    }

}