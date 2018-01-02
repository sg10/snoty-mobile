package me.snoty.mobile.plugins

import android.app.Notification
import android.service.notification.StatusBarNotification

/**
 * Created by Stefan on 28.12.2017.
 */
interface PluginInterface {
    fun created(id : String, n : StatusBarNotification)
    fun removed(id : String, n : StatusBarNotification)
    fun updated(id : String, n : StatusBarNotification)
}