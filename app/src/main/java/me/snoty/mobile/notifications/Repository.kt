package me.snoty.mobile.notifications

import android.app.Notification
import android.os.AsyncTask
import android.service.notification.StatusBarNotification
import android.util.Log
import me.snoty.mobile.plugins.DebugNotification
import me.snoty.mobile.plugins.DebugPost
import me.snoty.mobile.plugins.PluginInterface

/**
 * Created by Stefan on 27.12.2017.
 */
class Repository private constructor(){

    init {
        Log.d(TAG, "initializing repository")
        addPlugin(DebugPost())
    }

    private object Holder { val INSTANCE = Repository() }

    private var map : HashMap<String, StatusBarNotification> = HashMap()

    companion object {
        private val TAG = "NotificationRepo"

        val instance: Repository by lazy { Holder.INSTANCE }

        private val pluginsList : ArrayList<PluginInterface> = ArrayList()

        fun addPlugin(plugin : PluginInterface) {
            pluginsList.forEach {
                val className = it::class.java.simpleName
                if(className == plugin::class.java.simpleName) {
                    Log.d(TAG, "Plugin $className was already added, deleting first")
                    pluginsList.remove(it)
                }
            }
            pluginsList.add(plugin)
            Log.d(TAG, "Plugin loaded: " + plugin::class.java.simpleName)
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
        if(map.remove(id) != null) {
            Log.d(TAG, "REMOVED notification\n"+getSummary(sbn))
            pluginsList.forEach { it.removed(id, sbn) }
        }
    }

    private fun getNotificationId(sbn : StatusBarNotification) : String {
        return sbn.packageName+"#"+sbn.id
    }

    private fun getSummary(sbn : StatusBarNotification) : String {
        val extras = sbn?.notification?.extras
        val title = extras?.get(Notification.EXTRA_TITLE)?.toString()
        val text = extras?.get(Notification.EXTRA_TEXT)?.toString()
        return getNotificationId(sbn) + "\t" + title + "\t" + text
    }

}