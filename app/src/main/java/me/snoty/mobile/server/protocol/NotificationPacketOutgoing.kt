package me.snoty.mobile.server.protocol

import com.google.gson.annotations.SerializedName

/**
 * Created by Stefan on 05.01.2018.
 */
class NotificationPacketOutgoing : INetworkPacket() {

    override var type = "notification"

    @SerializedName("package")
    var aPackage : String? = null
    var title : String? = null
    var text : String? = null
    var actions : Array<NotificationAction> = arrayOf()
    var actionId : String? = null
    var removeInputAvailable : Boolean = false
    var remoteInputValue : String? = null

    class NotificationAction {
        var id = null
        var label = ""
    }

}