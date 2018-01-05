package me.snoty.mobile.server.protocol

import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * Created by Stefan on 05.01.2018.
 */
abstract class INetworkPacket {

    companion object {
        private val builder = GsonBuilder().setPrettyPrinting().
        private var gson = builder.create()
    }

    var version = 1
    abstract var type : String

    fun toJSON() : String {
        return gson.toJson(this)
    }
}