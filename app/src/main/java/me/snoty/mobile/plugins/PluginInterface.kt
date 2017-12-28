package me.snoty.mobile.plugins

import android.app.Notification

/**
 * Created by Stefan on 28.12.2017.
 */
interface PluginInterface {

    val commandFilter : String?

    fun isApplicable(command : String) : Boolean {
        return this.commandFilter == null || this.commandFilter.equals(command)
    }

    fun posted(n : Notification)
    fun removed(n : Notification)

}