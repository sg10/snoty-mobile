package me.snoty.mobile.processors

import android.service.notification.StatusBarNotification

/**
 * Created by Stefan on 28.12.2017.
 */
interface ProcessorInterface {
    fun created(id : String, n : StatusBarNotification)
    fun removed(id : String, n : StatusBarNotification)
    fun updated(id : String, n : StatusBarNotification)
}