package me.snoty.mobile.notifications

import android.app.Notification
import android.service.notification.StatusBarNotification
import android.util.Log
import me.snoty.mobile.processors.HistoryList
import me.snoty.mobile.processors.ProcessorInterface
import me.snoty.mobile.processors.ServerConnection
import java.util.*

/**
 * Created by Stefan on 27.12.2017.
 */
class Repository private constructor(){

    init {
        reset()
    }

    private object Holder { val INSTANCE = Repository() }

    private var map : HashMap<String, StatusBarNotification> = HashMap()

    companion object {
        private val TAG = "NotificationRepo"

        val instance: Repository by lazy { Holder.INSTANCE }

        private val processorsList: ArrayList<ProcessorInterface> = ArrayList()

        enum class Action { CREATED, UPDATED, REMOVED }

        fun addProcessor(processor: ProcessorInterface) {
            try {
                removeProcessor(processor)
                processorsList.add(processor)
                Log.d(TAG, "Plugin loaded: " + processor::class.java.simpleName)
            } catch(cex : ConcurrentModificationException) {
                cex.printStackTrace()
            }
        }

        fun removeProcessor(processor: ProcessorInterface) {
            processorsList.forEach {
                val className = it::class.java.simpleName
                if(className == processor::class.java.simpleName) {
                    Log.d(TAG, "Plugin $className was already added, deleting first")
                    processorsList.remove(it)
                }
            }
        }

        fun reset() {
            Log.d(TAG, "resetting repository")
            processorsList.clear()
            //addProcessor(DebugPost())
            addProcessor(ServerConnection())
            addProcessor(HistoryList())
        }
    }

    fun add(sbn : StatusBarNotification) {
        val id = getNotificationId(sbn)
        val updated = map.containsKey(id)
        map[id] = sbn

        if(updated) {
            processorsList.forEach { it.updated(id, sbn) }
            Log.d(TAG, "UPDATED notification\n"+getSummary(sbn))
        }
        else {
            processorsList.forEach { it.created(id, sbn) }
            Log.d(TAG, "CREATED notification\n"+getSummary(sbn))
        }
    }

    fun remove(sbn : StatusBarNotification) {
        val id = getNotificationId(sbn)
        if(map.remove(id) != null) {
            Log.d(TAG, "REMOVED notification\n"+getSummary(sbn))
            processorsList.forEach { it.removed(id, sbn) }
        }
    }

    fun get(id : String?) : StatusBarNotification? {
        if(id == null) return null
        return map[id]
    }

    private fun getNotificationId(sbn : StatusBarNotification) : String {
        return sbn.packageName+"#"+sbn.id+"#"+sbn.tag
    }

    private fun getSummary(sbn : StatusBarNotification) : String {
        val extras = sbn?.notification?.extras
        val title = extras?.get(Notification.EXTRA_TITLE)?.toString()
        val text = extras?.get(Notification.EXTRA_TEXT)?.toString()
        val channel = extras?.get(Notification.EXTRA_CHANNEL_ID)?.toString()
        val groupInfo = sbn.groupKey
        return getNotificationId(sbn) +
                "\t" + channel +
                "\t" + title + "\t" + text +
                "\tgroup: " + groupInfo

    }

}