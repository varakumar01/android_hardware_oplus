/*
 * SPDX-FileCopyrightText: 2021-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.device

import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.android.settingslib.widget.SettingsBasePreferenceFragment

class ButtonSettingsFragment : SettingsBasePreferenceFragment(), Preference.OnPreferenceChangeListener {
    private lateinit var topPositionPref: ListPreference
    private lateinit var middlePositionPref: ListPreference
    private lateinit var bottomPositionPref: ListPreference

    private lateinit var emojiTopPref: EditTextPreference
    private lateinit var emojiMiddlePref: EditTextPreference
    private lateinit var emojiBottomPref: EditTextPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.button_panel, rootKey)

        topPositionPref = findPreference("config_top_position")!!
        middlePositionPref = findPreference("config_middle_position")!!
        bottomPositionPref = findPreference("config_bottom_position")!!

        emojiTopPref = findPreference("config_emoji_top")!!
        emojiMiddlePref = findPreference("config_emoji_middle")!!
        emojiBottomPref = findPreference("config_emoji_bottom")!!

        topPositionPref.onPreferenceChangeListener = this
        middlePositionPref.onPreferenceChangeListener = this
        bottomPositionPref.onPreferenceChangeListener = this

        findPreference<SwitchPreferenceCompat>("config_alert_slider_island")?.onPreferenceChangeListener = this
        findPreference<SwitchPreferenceCompat>("config_alert_slider_glass")?.onPreferenceChangeListener = this
        findPreference<SwitchPreferenceCompat>("config_alert_slider_hide_label")?.onPreferenceChangeListener = this
        findPreference<SwitchPreferenceCompat>("config_alert_slider_glow")?.onPreferenceChangeListener = this
        findPreference<ListPreference>("config_alert_slider_glow_spread")?.onPreferenceChangeListener = this
        findPreference<ListPreference>("config_alert_slider_glow_strength")?.onPreferenceChangeListener = this

        findPreference<SwitchPreferenceCompat>("config_mute_media")?.onPreferenceChangeListener = this
        findPreference<SwitchPreferenceCompat>("config_show_dialog")?.onPreferenceChangeListener = this
        emojiTopPref.onPreferenceChangeListener = this
        emojiMiddlePref.onPreferenceChangeListener = this
        emojiBottomPref.onPreferenceChangeListener = this

        val resolver = requireContext().contentResolver
        findPreference<SwitchPreferenceCompat>("config_alert_slider_island")?.isChecked =
            Settings.System.getInt(resolver, "config_alert_slider_island", 0) != 0
        findPreference<SwitchPreferenceCompat>("config_alert_slider_glass")?.isChecked =
            Settings.System.getInt(resolver, "config_alert_slider_glass", 0) != 0
        findPreference<SwitchPreferenceCompat>("config_alert_slider_hide_label")?.isChecked =
            Settings.System.getInt(resolver, "config_alert_slider_hide_label", 0) != 0
        findPreference<SwitchPreferenceCompat>("config_alert_slider_glow")?.isChecked =
            Settings.System.getInt(resolver, "config_alert_slider_glow", 1) != 0
        findPreference<ListPreference>("config_alert_slider_glow_spread")?.let { pref ->
            pref.value = Settings.System.getInt(resolver, "config_alert_slider_glow_spread", 8).toString()
        }
        findPreference<ListPreference>("config_alert_slider_glow_strength")?.let { pref ->
            pref.value = Settings.System.getInt(resolver, "config_alert_slider_glow_strength", 80).toString()
        }
        findPreference<SwitchPreferenceCompat>("config_mute_media")?.isChecked =
            Settings.System.getInt(resolver, "config_mute_media", 0) != 0
        findPreference<SwitchPreferenceCompat>("config_show_dialog")?.isChecked =
            Settings.System.getInt(resolver, "config_show_dialog", 1) != 0

        syncPositionToSystem(topPositionPref)
        syncPositionToSystem(middlePositionPref)
        syncPositionToSystem(bottomPositionPref)
    }

    override fun onResume() {
        super.onResume()
        syncEmojiSummaries()
    }

    private fun syncPositionToSystem(pref: ListPreference) {
        val resolver = requireContext().contentResolver
        val current = Settings.System.getString(resolver, pref.key)
        if (current == null) {
            val value = pref.value ?: pref.entryValues?.firstOrNull()?.toString() ?: "0"
            Settings.System.putString(resolver, pref.key, value)
        }
    }

    private fun syncEmojiSummaries() {
        val resolver = requireContext().contentResolver
        listOf(
            emojiTopPref to "top",
            emojiMiddlePref to "middle",
            emojiBottomPref to "bottom"
        ).forEach { (pref, pos) ->
            val saved = Settings.System.getString(resolver, "config_emoji_$pos")
            if (!saved.isNullOrEmpty()) {
                pref.summary = saved
                pref.text = saved
            } else {
                pref.summary = "Not set — type emoji"
                pref.text = ""
            }
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        val resolver = requireContext().contentResolver

        when (preference.key) {
            "config_top_position", "config_middle_position", "config_bottom_position" -> {
                val value = newValue as? String ?: return true
                val (otherPref1, otherPref2) = when (preference.key) {
                    "config_top_position" -> Pair(middlePositionPref, bottomPositionPref)
                    "config_middle_position" -> Pair(topPositionPref, bottomPositionPref)
                    "config_bottom_position" -> Pair(topPositionPref, middlePositionPref)
                    else -> return true
                }
                if (value == otherPref1.value || value == otherPref2.value) {
                    Toast.makeText(requireContext(), R.string.alert_slider_action_already_mapped, Toast.LENGTH_SHORT).show()
                    return false
                }
                Settings.System.putString(resolver, preference.key, value)
            }
            "config_alert_slider_island", "config_alert_slider_glass", "config_alert_slider_hide_label", "config_alert_slider_glow" -> {
                Settings.System.putInt(resolver, preference.key, if (newValue as Boolean) 1 else 0)
            }
            "config_alert_slider_glow_spread", "config_alert_slider_glow_strength" -> {
                val strVal = newValue as? String ?: return true
                Settings.System.putInt(resolver, preference.key, strVal.toIntOrNull() ?: 8)
            }
            "config_mute_media", "config_show_dialog" -> {
                Settings.System.putInt(resolver, preference.key, if (newValue as Boolean) 1 else 0)
            }
            "config_emoji_top", "config_emoji_middle", "config_emoji_bottom" -> {
                val emoji = newValue as String
                if (emoji.isNotEmpty()) {
                    val hasTextOrSymbol = emoji.any { it.isLetterOrDigit() || it.isWhitespace() || it in '!'..'~' }
                    if (hasTextOrSymbol) {
                        Toast.makeText(requireContext(), "Only emojis are allowed", Toast.LENGTH_SHORT).show()
                        return false
                    }
                    val codePointCount = emoji.replace(Regex("[\\u200D\\uFE0F]"), "").let { it.codePointCount(0, it.length) }
                    if (codePointCount > 5) {
                        Toast.makeText(requireContext(), "Max 5 emojis allowed", Toast.LENGTH_SHORT).show()
                        return false
                    }
                }
                Settings.System.putString(resolver, preference.key, emoji)
                preference.summary = if (emoji.isNotEmpty()) emoji
                    else "Not set — type emoji"
            }
        }
        return true
    }
}
