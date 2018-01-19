package me.snoty.mobile.notifications

import android.app.Notification
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Log
import me.snoty.mobile.ContextHelper
import me.snoty.mobile.R
import kotlin.properties.Delegates

/**
 * Created by Stefan on 27.12.2017.
 */
class Filter {

    private val TAG = "Filter"

    companion object {
        var instance : Filter? = null
    }

    private var PREF_PACKAGES = ""
    private var PREF_NOCLEAR = ""

    private val ignoredPackages : ArrayList<String> = ArrayList()
    private var ignoreNotClearable : Boolean = true

    constructor() {
        instance = this
        load()
    }

    private fun load() {
        val context = ContextHelper.get()
        val preferenceManager = PreferenceManager.getDefaultSharedPreferences(context)

        PREF_PACKAGES = context.getString(R.string.preference_ignored_packages_key)
        PREF_NOCLEAR = context.getString(R.string.preference_noclear_key)

        val ignoredPackagesString = preferenceManager.getString(PREF_PACKAGES, "android")
        ignoredPackages.addAll(ignoredPackagesString!!.split(";"))

        ignoreNotClearable = preferenceManager.getBoolean(PREF_NOCLEAR, ignoreNotClearable)
        Log.d(TAG, " --- Loading Notifications Filter ---\n" +
                "Ignore non-clearable: " + ignoreNotClearable + "\n" +
                "Filtered packages: \n\t" + ignoredPackagesString.replace(";", "\n\t"))
    }

    private fun save() {
        val preferenceManager = PreferenceManager.getDefaultSharedPreferences(ContextHelper.get())
        val editor : SharedPreferences.Editor = preferenceManager.edit()
        editor.putString(PREF_PACKAGES, TextUtils.join(";", ignoredPackages))
        editor.apply()
    }

    fun addIgnorePackage(packageName : String) {
        ignoredPackages.add(packageName)

        save()
    }

    fun shouldIgnore(sbn : StatusBarNotification?) : Boolean {
        if(sbn == null) {
            return true
        }
        if(sbn.packageName == ContextHelper.get().packageName && !sbn.isClearable) {
            return true
        }
        if(sbn == null || ignoredPackages.contains(sbn?.packageName)) {
            return true
        }
        if(!sbn?.isClearable && ignoreNotClearable) {
            return true
        }

        var title = sbn.notification.extras?.get(Notification.EXTRA_TITLE)?.toString() ?: ""
        var text = sbn.notification.extras?.get(Notification.EXTRA_TEXT)?.toString() ?: ""
        if(title == "" && text == "") {
            return true
        }

        return false
    }


}