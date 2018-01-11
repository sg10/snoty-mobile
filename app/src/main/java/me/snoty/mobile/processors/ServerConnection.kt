package me.snoty.mobile.processors

import android.os.AsyncTask
import android.service.notification.StatusBarNotification
import android.util.Log
import me.snoty.mobile.server.ConnectionHandler
import me.snoty.mobile.server.NetworkPacketHandler
import me.snoty.mobile.server.protocol.NotificationOperationPacket
import me.snoty.mobile.server.protocol.NotificationPostedPacket
import javax.net.SocketFactory
import javax.net.ssl.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*


/**
 * Created by Stefan on 02.01.2018.
 */
class ServerConnection : ProcessorInterface {

    private var handler : NetworkPacketHandler = NetworkPacketHandler.instance

    private var TAG = "ServerConn"

    constructor() {
        ConnectionHandler.initServerConnectionListener(this)
    }

    override fun created(id: String, n: StatusBarNotification) {
        Log.d(TAG, "sending data to server")
        val packet = handler.create(NotificationPostedPacket(id, n))
        try {
            ConnectionHandler.send(handler.toJSON(packet))
        } catch(cex : CertificateException) {
            Log.e(TAG, cex.message)
        }
    }

    override fun removed(id: String, n: StatusBarNotification) {
        // send command
    }

    override fun updated(id: String, n: StatusBarNotification) {
        // send command
    }

    fun receivedCommand(command : String) {
        Log.d(TAG, "received command\n$command")
    }
}