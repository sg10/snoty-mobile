package me.snoty.mobile.server.protocol

import android.service.notification.StatusBarNotification
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by Stefan on 06.01.2018.
 */
class IgnorePackagePacket : IPacketBody {

    @JsonProperty("package")
    private var aPackage: String? = null

    fun getPackage() : String {
        return aPackage ?: ""
    }

}