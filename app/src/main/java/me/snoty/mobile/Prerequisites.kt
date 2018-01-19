package me.snoty.mobile

import android.content.ComponentName
import android.provider.Settings
import me.snoty.mobile.notifications.ListenerService

/**
 * Created by Stefan on 19.01.2018.
 */
class Prerequisites {

    companion object {
        fun hasListenerPermission() : Boolean {
            val context = ContextHelper.get()
            val cn = ComponentName(context, ListenerService::class.java)
            val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            return flat != null && flat.contains(cn.flattenToString())
        }

        fun wasCertificateScanned() : Boolean {
            val fingerprint = ServerPreferences.instance.getFingerprint()
            return (fingerprint != "")
        }

        var fingerprintValid: Boolean = true

        fun isFullyInitialized() : Boolean {
            return hasListenerPermission() && wasCertificateScanned() && fingerprintValid
        }
    }

}