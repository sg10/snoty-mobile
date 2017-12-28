package me.snoty.mobile.activities

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import me.snoty.mobile.R


/**
 * Created by Stefan on 28.12.2017.
 */
class NotificationsListAdapter(context: Context) : ArrayAdapter<Notification>(context, R.id.notificationsHistoryList) {

    override fun add(notification: Notification) {
        super.insert(notification, 0)
    }

    @SuppressLint("NewApi")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {

        var v: View? = convertView

        if (v == null) {
            val vi: LayoutInflater = LayoutInflater.from(context)
            v = vi.inflate(R.layout.history_list_item, null)
        }

        val notification : Notification = getItem(position)

        if (notification != null) {
            val title = v!!.findViewById(R.id.titleTextView) as TextView
            val text = v!!.findViewById(R.id.textTextView) as TextView
            val icon = v!!.findViewById(R.id.icon) as ImageView

            title?.text = notification.extras?.getString("android.title")
            text?.text = notification.extras?.getString("android.text")
            //icon?.setImageIcon(notification.)
        }

        return v
    }

    override fun hasStableIds(): Boolean {
        return true
    }


}