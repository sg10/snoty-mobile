package me.snoty.mobile.activities

import android.preference.Preference
import android.widget.Toast
import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import me.snoty.mobile.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.preference.PreferenceFragment
import android.preference.Preference.OnPreferenceClickListener






/**
 * Created by Stefan on 05.01.2018.
 */
class SettingsActivity : PreferenceActivity(), Preference.OnPreferenceChangeListener {

    public override fun onCreate(savedInstanceState: Bundle?) {
        fragmentManager.beginTransaction().replace(android.R.id.content, MainPreferenceFragment()).commit()

        super.onCreate(savedInstanceState)
    }

    class MainPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)

            val myPref = findPreference(getString(R.string.preference_ignored_packages_key)) as Preference
            myPref.onPreferenceClickListener = OnPreferenceClickListener {
                //open browser or intent here
                true
            }

        }
    }

    override fun onPreferenceChange(preference: Preference, value: Any): Boolean {

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPrefs.edit()

        when(preference.key) {
            getString(R.string.preference_noclear_key) -> {
                editor.putBoolean(preference.key, value as Boolean)
            }
        }

        editor.apply()

        return true
    }
}