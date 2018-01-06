package me.snoty.mobile.processors

import android.service.notification.StatusBarNotification
import android.util.Log
import me.snoty.mobile.server.NetworkPacketHandler
import me.snoty.mobile.server.protocol.NotificationOperationPacket
import me.snoty.mobile.server.protocol.NotificationPostedPacket
import javax.net.SocketFactory
import javax.net.ssl.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter


/**
 * Created by Stefan on 02.01.2018.
 */
class ServerConnection : ProcessorInterface {

    private var socket : SSLSocket? = null

    private var handler : NetworkPacketHandler = NetworkPacketHandler.instance

    private var TAG = "ServerConn"

    override fun created(id: String, n: StatusBarNotification) {
        val packet = handler.create(NotificationPostedPacket(id, n))
        Log.d(TAG, handler.toJSON(packet))

        val pBody = NotificationOperationPacket()
        pBody.id = id
        pBody.actionId = 0
        pBody.inputValue = null
        pBody.operation = NotificationOperationPacket.NotificationOperation.action
        val packet2 = handler.create(pBody)
        Log.d(TAG, handler.toJSON(packet2))

        // send command
    }

    override fun removed(id: String, n: StatusBarNotification) {
        // send command
    }

    override fun updated(id: String, n: StatusBarNotification) {
        // send command
    }

    private fun establishConnection() {
        // Open SSLSocket directly to gmail.com
        val sf : SocketFactory = SSLSocketFactory.getDefault();
        socket = sf.createSocket("gmail.com", 443) as SSLSocket
        val hv : HostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier()
        val s : SSLSession = socket!!.session

        // Verify that the certicate hostname is for mail.google.com
        // This is due to lack of SNI support in the current SSLSocket.
        if (!hv.verify("mail.google.com", s)) {
            socket?.close()
            throw SSLHandshakeException("Expected mail.google.com, found " + s.peerPrincipal)
        }
    }

    private fun waitForCommand() {
        if(socket == null) {
            establishConnection()
        }

        val outw = BufferedWriter(OutputStreamWriter(socket?.outputStream))
        val inr = BufferedReader(InputStreamReader(socket?.inputStream))
        // read
        // - remove
        // - call intent
    }
}