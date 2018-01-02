package me.snoty.mobile.notifications

import android.app.Notification
import android.os.AsyncTask
import android.service.notification.StatusBarNotification
import android.util.Log
import me.snoty.mobile.plugins.PluginInterface

/**
 * Created by Stefan on 27.12.2017.
 */
class Repository private constructor(){

    private val TAG = "NotificationRepo"

    init { println("($this) is the notifications repository") }

    private object Holder { val INSTANCE = Repository() }

    private var map : HashMap<String, StatusBarNotification> = HashMap()

    companion object {
        val instance: Repository by lazy { Holder.INSTANCE }

        private val pluginsList : ArrayList<PluginInterface> = ArrayList()

        fun addPlugin(plugin : PluginInterface) {
            if(!pluginsList.contains(plugin)) {
                pluginsList.add(plugin)
            }
        }
    }

    fun add(sbn : StatusBarNotification) {
        val id = getNotificationId(sbn)
        val updated = map.containsKey(id)
        map[id] = sbn

        if(updated) {
            pluginsList.forEach { it.updated(id, sbn) }
            Log.d(TAG, "UPDATED notification\n"+getSummary(sbn))
        }
        else {
            pluginsList.forEach { it.created(id, sbn) }
            Log.d(TAG, "CREATED notification\n"+getSummary(sbn))
        }
    }

    fun remove(sbn : StatusBarNotification) {
        val id = getNotificationId(sbn)
        Log.d(TAG, "REMOVED notification\n"+getSummary(sbn))
        map.remove(id)

        pluginsList.forEach { it.removed(id, sbn) }
    }

    private fun getNotificationId(sbn : StatusBarNotification) : String {
        return sbn.packageName+"#"+sbn.id
    }

    private fun getSummary(sbn : StatusBarNotification) : String {
        val extras = sbn?.notification?.extras
        return getNotificationId(sbn) + "\t" +
                extras?.getString(Notification.EXTRA_TITLE) + "\t" +
                extras?.getString(Notification.EXTRA_TEXT)
    }

}