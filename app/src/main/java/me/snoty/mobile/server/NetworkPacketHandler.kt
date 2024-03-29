package me.snoty.mobile.server

import android.util.Log
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import me.snoty.mobile.ServerPreferences
import me.snoty.mobile.server.protocol.NetworkPacket
import me.snoty.mobile.server.protocol.NotificationPostedPacket
import me.snoty.mobile.server.protocol.NotificationRemovedPacket

/**
 * Created by Stefan on 06.01.2018.
 */
class NetworkPacketHandler private constructor() {

    private object Holder { val INSTANCE = NetworkPacketHandler() }

    companion object {
        private val TAG = "NetPackHan"
        val instance: NetworkPacketHandler by lazy { Holder.INSTANCE }
    }

    private var secret = ""
    fun setSecret(newSecret: String) {
        if(newSecret != "") {
            this.secret = newSecret
        }
        else {
            Log.w(TAG, "attempted to set empty secret")
        }
    }

    private val objectMapper = ObjectMapper()
    private val prettyObjectMapper = ObjectMapper()

    init {
        Log.d(TAG, "initializing network packet handler")
        objectMapper.disable(SerializationFeature.INDENT_OUTPUT)
        prettyObjectMapper.enable(SerializationFeature.INDENT_OUTPUT)

        setSecret(ServerPreferences.instance.getSecret())
    }

    fun toJSON(packet : NetworkPacket) : String {
        if(packet is NotificationRemovedPacket) {
            packet.secret = this.secret
        }
        if(packet is NotificationPostedPacket) {
            packet.secret = this.secret
        }

        return objectMapper.writeValueAsString(packet)
    }

    fun fromJSON(json : String) : NetworkPacket? {
        try {
            return objectMapper.readValue(json, NetworkPacket::class.java)
        }
        catch(ex : JsonMappingException) {
            Log.w(TAG, "received unknown data format\n$json\n", ex)
        }
        return null
    }

    fun toPrettyJSON(packet : NetworkPacket) : String {
        return prettyObjectMapper.writeValueAsString(packet)
    }
}