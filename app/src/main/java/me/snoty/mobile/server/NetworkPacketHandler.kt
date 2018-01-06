package me.snoty.mobile.server

import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import me.snoty.mobile.server.protocol.IPacketBody
import me.snoty.mobile.server.protocol.NetworkPacket

/**
 * Created by Stefan on 06.01.2018.
 */
class NetworkPacketHandler private constructor() {

    private object Holder { val INSTANCE = NetworkPacketHandler() }

    companion object {
        private val TAG = "NetPackHan"

        val instance: NetworkPacketHandler by lazy { Holder.INSTANCE }
    }

    private val objectMapper = ObjectMapper()

    init {
        Log.d(TAG, "initializing network packet handler")
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT)
    }

    fun create(body : IPacketBody) : NetworkPacket {
        return NetworkPacket(body)
    }

    fun toJSON(packet : NetworkPacket) : String {
        return objectMapper.writeValueAsString(packet)
    }
}