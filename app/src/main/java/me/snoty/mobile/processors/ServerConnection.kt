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
import java.security.cert.X509Certificate
import java.util.*


/**
 * Created by Stefan on 02.01.2018.
 */
class ServerConnection : ProcessorInterface {

    private var handler : NetworkPacketHandler = NetworkPacketHandler.instance

    private var TAG = "ServerConn"

    override fun created(id: String, n: StatusBarNotification) {
        /*
        val packet = handler.create(NotificationPostedPacket(id, n))
        Log.d(TAG, handler.toJSON(packet))

        val pBody = NotificationOperationPacket()
        pBody.id = id
        pBody.actionId = 0
        pBody.inputValue = null
        pBody.operation = NotificationOperationPacket.NotificationOperation.action
        val packet2 = handler.create(pBody)
        Log.d(TAG, handler.toJSON(packet2))
        */
        // send command
        Log.d(TAG, "sending data to server")
        ConnectionHandler("{test:'data'}").execute()
    }

    override fun removed(id: String, n: StatusBarNotification) {
        // send command
    }

    override fun updated(id: String, n: StatusBarNotification) {
        // send command
    }

    private fun waitForCommand() {

        // read
        // - remove
        // - call intent
    }
}