package me.snoty.mobile.server.protocol

import android.service.notification.StatusBarNotification
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by Stefan on 06.01.2018.
 */
class NotificationRemovedPacket : IPacketBody {

    @JsonProperty("package")

    var id : String? = null

    constructor(id : String, sbn : StatusBarNotification) {
        this.id = id
    }

}