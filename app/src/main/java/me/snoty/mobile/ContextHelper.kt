package me.snoty.mobile

import android.content.Context
import me.snoty.mobile.activities.MainActivity
import me.snoty.mobile.notifications.ListenerService

/**
 * Created by Stefan on 19.01.2018.
 */
class ContextHelper {

    companion object {

        fun get() : Context {
            val currentContext = ListenerService.instance ?: MainActivity.instance ?: null

            if(currentContext == null) {
                android.os.Process.killProcess(android.os.Process.myPid())
                System.exit(-100)
                throw Exception("error fetching context: no ${ListenerService::class.java} or ${MainActivity::class.java}")
            }

            return currentContext as Context
        }

    }

}