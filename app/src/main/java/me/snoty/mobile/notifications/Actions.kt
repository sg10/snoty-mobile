package me.snoty.mobile.notifications

import android.util.Log
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle


/**
 * Created by Stefan on 13.01.2018.
 */
class Actions {


    private object Holder { val INSTANCE = Actions() }

    companion object {
        private val TAG = "Actions"

        val instance: Actions by lazy { Holder.INSTANCE }
    }

    fun close(id : String?) {
        if(id == null) return

        val sbn = Repository.instance.get(id)

        try {
            ListenerService.instance?.cancelNotification(sbn?.key)
        }
        catch(ex : Exception) {
            Log.w(TAG, ex)
        }
    }

    fun action(id : String?, num : Int?, inputValue : String?) {
        if(id == null || num == null || num < 0) return

        val sbn = Repository.instance.get(id)
        if(sbn == null) {
            Log.d(TAG, "notification $id does not exist")
            return
        }

        val actions = sbn?.notification?.actions

        if(actions != null && actions.size > num) {
            val action = actions[num]
            try {
                val pendingIntent = action.actionIntent
                val localIntent = Intent()
                localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val localBundle = Bundle()
                if(inputValue != null && action.remoteInputs != null) {
                    action.remoteInputs.forEach {
                        localBundle.putCharSequence(it.resultKey, inputValue)
                    }
                    RemoteInput.addResultsToIntent(action.remoteInputs, localIntent, localBundle)
                }
                pendingIntent.send(ListenerService.instance, 0, localIntent)
                Log.d(TAG, "sent intent for action")
            }
            catch(ex : Exception) {
                Log.w(TAG, ex.message, ex)
            }
        }
        else {
            Log.w(TAG, "Action $num does not exist on notification $id\n" +
                    "(${actions?.size} actions available)")
        }
    }

}

