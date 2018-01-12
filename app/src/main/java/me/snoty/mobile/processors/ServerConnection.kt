package me.snoty.mobile.processors

import android.service.notification.StatusBarNotification
import android.util.Log
import me.snoty.mobile.server.connection.ConnectionHandler
import me.snoty.mobile.server.NetworkPacketHandler
import me.snoty.mobile.server.protocol.NotificationPostedPacket
import me.snoty.mobile.server.protocol.NotificationRemovedPacket
import java.security.cert.CertificateException


/**
 * Created by Stefan on 02.01.2018.
 */
class ServerConnection : ProcessorInterface {

    private var packetHandler: NetworkPacketHandler = NetworkPacketHandler.instance

    private var TAG = "ServerConn"

    constructor() {
        ConnectionHandler.instance.serverConnectionListener = this
    }

    override fun created(id: String, n: StatusBarNotification) {
        val packet = packetHandler.create(NotificationPostedPacket(id, n, false))
        ConnectionHandler.instance.addRequestToQueue(packet)
    }

    override fun removed(id: String, n: StatusBarNotification) {
        val packet = packetHandler.create(NotificationRemovedPacket(id, n))
        ConnectionHandler.instance.addRequestToQueue(packet)
    }

    override fun updated(id: String, n: StatusBarNotification) {
        val packet = packetHandler.create(NotificationPostedPacket(id, n, true))
        ConnectionHandler.instance.addRequestToQueue(packet)
    }

    fun receivedCommand(command : String) {
        Log.d(TAG, "received command\n$command")
    }
}