package me.snoty.mobile.server.protocol

import android.app.Notification
import android.service.notification.StatusBarNotification
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by Stefan on 06.01.2018.
 */
class NotificationOperationPacket : IPacketBody {

    var id : String? = null
    var operation: NotificationOperation? = null
    var actionId : Number? = null
    var inputValue : String? = null

    enum class NotificationOperation {
        close,
        action
    }

}