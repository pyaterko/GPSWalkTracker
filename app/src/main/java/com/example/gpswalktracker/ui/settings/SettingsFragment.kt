package com.example.gpswalktracker.ui.settings

import android.graphics.Color
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.gpswalktracker.R

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var timePreference: Preference
    private lateinit var colorPreference: Preference
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_peferences, rootKey)
        init()
    }

    private fun init() {
        timePreference = findPreference(getString(R.string.update_time_key))!!
        colorPreference = findPreference(getString(R.string.track_color_key))!!
        timePreference.onPreferenceChangeListener = onChangeListener()
        colorPreference.onPreferenceChangeListener = onChangeListener()
        initPreference(timePreference, colorPreference)
    }

    private fun onChangeListener() =
        Preference.OnPreferenceChangeListener { preference, newValue ->
            when (preference.key) {
                getString(R.string.update_time_key) -> setSummaryTimePreference(
                    preference,
                    newValue
                )
                getString(R.string.track_color_key) -> setIconColorPreference(
                    preference,
                    newValue
                )
            }
            true
        }

    private fun setIconColorPreference(preference: Preference, colorPref: Any) {
        val arrayName = resources.getStringArray(R.array.color_name)
        val arrayValue = resources.getStringArray(R.array.color_value)
        val color = arrayName[arrayValue.indexOf(colorPref)]
        preference.icon?.setTint(Color.parseColor(color))
    }

    private fun setSummaryTimePreference(preference: Preference, summary: Any) {
        val arrayName = resources.getStringArray(R.array.loc_time_update_name)
        val arrayValue = resources.getStringArray(R.array.loc_time_update_value)
        val title = resources.getString(R.string.update_time_summary)
        preference.summary = "$title ${arrayName[arrayValue.indexOf(summary)]}"
    }

    private fun initPreference(timePreference: Preference, colorPreference: Preference) {
        val preferenceTime = timePreference.preferenceManager.sharedPreferences
        val preferenceColor = colorPreference.preferenceManager.sharedPreferences
        val summaryTimePreference =
            preferenceTime?.getString(
                getString(R.string.update_time_key),
                getString(R.string.default_time)
            )
                ?: getString(R.string.default_time)
        val colorPref =
            preferenceColor?.getString(
                getString(R.string.track_color_key),
                getString(R.string.default_color)
            )
                ?: getString(R.string.default_color)
        setSummaryTimePreference(timePreference, summaryTimePreference)
        setIconColorPreference(colorPreference, colorPref)
    }
}
