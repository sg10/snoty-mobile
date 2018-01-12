package me.snoty.mobile.processors

import android.service.notification.StatusBarNotification
import android.util.Log
import me.snoty.mobile.activities.MainActivity
import me.snoty.mobile.notifications.Repository
import me.snoty.mobile.processors.history.NotificationHistoryItem

/**
 * Created by Stefan on 09.01.2018.
 */
class HistoryList : ProcessorInterface {

    private object Holder { val INSTANCE = HistoryList() }

    companion object {
        var instance: HistoryList? = null
    }

    constructor() {
        instance = this
    }

    val historyList : ArrayList<NotificationHistoryItem> = ArrayList()

    private fun createHistoryItem(n: NotificationHistoryItem) {
        historyList.add(0, n)
        MainActivity.instance?.addToNotificationList(n)
        MainActivity.instance?.updateViews()
    }

    override fun created(id: String, n: StatusBarNotification) {
        val n = NotificationHistoryItem(n, Repository.Companion.Action.CREATED)
        createHistoryItem(n)
    }

    override fun removed(id: String, n: StatusBarNotification) {
        val n = NotificationHistoryItem(n, Repository.Companion.Action.REMOVED)
        createHistoryItem(n)
    }

    override fun updated(id: String, n: StatusBarNotification) {
        val n = NotificationHistoryItem(n, Repository.Companion.Action.UPDATED)
        createHistoryItem(n)
    }
}