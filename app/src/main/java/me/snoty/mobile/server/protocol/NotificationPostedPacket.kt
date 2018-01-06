package me.snoty.mobile.server.protocol

import android.app.Notification
import android.service.notification.StatusBarNotification
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by Stefan on 06.01.2018.
 */
class NotificationPostedPacket : IPacketBody {

    @JsonProperty("package")
    private var aPackage : String? = null

    var id : String? = null

    var title : String? = null
    var text : String? = null

    var actions : ArrayList<NotificationAction> = arrayListOf()

    var clearable : Boolean? = null

    class NotificationAction(var id : Number,
                             var label : String,
                             var isInput : Boolean) {
    }

    constructor(id : String, sbn : StatusBarNotification) {

        this.aPackage = sbn.packageName
        this.id = id

        var nExtras = sbn?.notification?.extras
        this.title = nExtras?.get(Notification.EXTRA_TITLE)?.toString()
        this.text = nExtras?.get(Notification.EXTRA_TEXT)?.toString()

        if(sbn.notification.actions != null) {
            var idx = 0
            sbn.notification.actions.forEach {
                var isInput : Boolean = it.remoteInputs != null && it.remoteInputs.isNotEmpty()
                actions.add(NotificationAction(idx, it.title.toString(), isInput))
                idx += 1
            }
        }

        this.clearable = sbn.isClearable
    }

}