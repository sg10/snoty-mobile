package me.snoty.mobile.notifications

import android.app.Notification
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Log
import me.snoty.mobile.R
import kotlin.properties.Delegates

/**
 * Created by Stefan on 27.12.2017.
 */
class Filter(private val context : Context) {

    private val TAG = "Filter"

    private var PREF_PACKAGES = ""
    private var PREF_NOCLEAR = ""

    private var mPref : SharedPreferences by Delegates.notNull()

    private val ignoredPackages : ArrayList<String> = ArrayList()
    private var ignoreNotClearable : Boolean = true

    init {
        mPref = PreferenceManager.getDefaultSharedPreferences(context)
        load()
    }

    fun load() {
        PREF_PACKAGES = context.getString(R.string.preference_ignored_packages_key)
        PREF_NOCLEAR = context.getString(R.string.preference_noclear_key)

        Log.d(TAG, " --- Loading Notifications Filter ---")

        val ignoredPackagesString = mPref.getString(PREF_PACKAGES, "android")
        Log.d(TAG, "Filtered packages: \n" + ignoredPackagesString.replace(";", "\n\t"))
        ignoredPackages.addAll(ignoredPackagesString!!.split(";"))

        ignoreNotClearable = mPref.getBoolean(PREF_NOCLEAR, false)
        Log.d(TAG, "Ignore non-clearable: " + ignoreNotClearable)
    }

    fun save() {
        if(mPref != null) {
            val editor : SharedPreferences.Editor = mPref.edit()
            editor.putString(PREF_PACKAGES, TextUtils.join(";", ignoredPackages))
            editor.apply()
        }
    }

    fun shouldIgnore(sbn : StatusBarNotification?) : Boolean {

        if(sbn?.packageName == context.packageName &&
                sbn?.notification?.extras?.getInt(Notification.EXTRA_NOTIFICATION_ID) == Listener.SERVICE_NOTIFICATION_ID) {
            return true
        }
        if(sbn == null || ignoredPackages.contains(sbn?.packageName)) {
            return true
        }
        if(!sbn?.isClearable && ignoreNotClearable) {
            return true
        }

        return false
    }


}