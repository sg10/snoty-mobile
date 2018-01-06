package me.snoty.mobile.server.protocol

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import kotlin.properties.Delegates

/**
 * Created by Stefan on 05.01.2018.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY )
@JsonPropertyOrder("header")
class NetworkPacket constructor(val body : IPacketBody) {

    var header = PacketHeader()

    init {
        header.type = body::class.java.simpleName.replace("Packet", "")
    }

}