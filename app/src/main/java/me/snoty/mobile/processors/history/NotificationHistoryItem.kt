package me.snoty.mobile.processors.history

import android.app.Notification
import android.service.notification.StatusBarNotification
import me.snoty.mobile.notifications.Repository
import kotlin.properties.Delegates

/**
 * Created by Stefan on 09.01.2018.
 */
class NotificationHistoryItem {

    var title : String by Delegates.notNull()
    var text : String by Delegates.notNull()
    var packageName : String by Delegates.notNull()
    var action : Repository.Companion.Action by Delegates.notNull()

    constructor(sbn : StatusBarNotification, action: Repository.Companion.Action) {
        title = sbn?.notification.extras?.get(Notification.EXTRA_TITLE)?.toString() ?: ""
        text = sbn?.notification.extras?.get(Notification.EXTRA_TEXT)?.toString() ?: ""
        packageName = sbn?.packageName ?: ""
        this.action = action
    }

}