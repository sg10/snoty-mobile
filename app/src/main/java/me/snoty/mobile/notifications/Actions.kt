package me.snoty.mobile.notifications

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

        ListenerService.instance?.cancelNotification(sbn?.key)
    }

    fun action(id : String?, num : String) {

    }

    fun input(id : String?, num : String, input : String) {

    }

}

