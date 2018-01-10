package me.snoty.mobile.activities

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import me.snoty.mobile.R
import me.snoty.mobile.notifications.Repository
import me.snoty.mobile.processors.history.NotificationHistoryItem


/**
 * Created by Stefan on 28.12.2017.
 */
class NotificationsListAdapter(context: Context) : ArrayAdapter<NotificationHistoryItem>(context, R.id.notificationsHistoryList) {

    override fun add(notification: NotificationHistoryItem) {
        super.insert(notification, 0)
    }

    @SuppressLint("NewApi")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {

        var v: View? = convertView

        if (v == null) {
            val vi: LayoutInflater = LayoutInflater.from(context)
            v = vi.inflate(R.layout.history_list_item, null)
        }

        val notification : NotificationHistoryItem = getItem(position)

        if (notification != null) {
            val title = v!!.findViewById(R.id.titleTextView) as TextView
            val text = v!!.findViewById(R.id.textTextView) as TextView
            val icon = v!!.findViewById(R.id.icon) as ImageView

            title.text = notification.title
            text.text = notification.text

            if(notification.action == Repository.Companion.Action.CREATED) {
                title.setTextColor(Color.GREEN)
            }
            else if(notification.action == Repository.Companion.Action.REMOVED) {
                title.setTextColor(Color.RED)
            }
            else if(notification.action == Repository.Companion.Action.UPDATED) {
                title.setTextColor(Color.YELLOW)
            }

            //icon?.setImageIcon(notification.)
        }

        return v
    }

    override fun hasStableIds(): Boolean {
        return true
    }


}