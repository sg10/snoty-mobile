package me.snoty.mobile.plugins

import android.app.Notification

/**
 * Created by Stefan on 28.12.2017.
 */
interface PluginInterface {

    fun isApplicable(command : String) : Boolean {
        return true
    }

    fun posted(n : Notification)
    fun removed(n : Notification)

}