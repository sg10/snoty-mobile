package me.snoty.mobile.processors

import android.service.notification.StatusBarNotification
import android.util.Log
import me.snoty.mobile.notifications.Actions
import me.snoty.mobile.notifications.Filter
import me.snoty.mobile.server.connection.ConnectionHandler
import me.snoty.mobile.server.protocol.*


/**
 * Created by Stefan on 02.01.2018.
 */
class ServerConnection : ProcessorInterface {

    private var TAG = "ServerConn"

    constructor() {
        ConnectionHandler.instance.processor = this
    }

    override fun created(id: String, n: StatusBarNotification) {
        val packet = NotificationPostedPacket(id, n, false)
        ConnectionHandler.instance.addRequestToQueue(packet)
    }

    override fun removed(id: String, n: StatusBarNotification) {
        val packet = NotificationRemovedPacket(id, n)
        ConnectionHandler.instance.addRequestToQueue(packet)
    }

    override fun updated(id: String, n: StatusBarNotification) {
        val packet = NotificationPostedPacket(id, n, true)
        ConnectionHandler.instance.addRequestToQueue(packet)
    }

    fun receivedCommand(packet: NetworkPacket) {
        Log.d(TAG, "received command\n$packet")

        if(packet is NotificationOperationPacket) {

            if(packet.operation == NotificationOperationPacket.NotificationOperation.close) {
                Actions.instance.close(packet.id)
            }
            else {
                Actions.instance.action(packet.id, packet.actionId, packet.inputValue)
            }

        }
        else if(packet is IgnorePackagePacket) {

            val packageName = packet.getPackage()
            Filter.instance?.addIgnorePackage(packageName)

        }
    }



}