package com.riel_dev.hayaku;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat {

    SharedPreferences preferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // logout (or not logged in) -> can't switch on
        if(!CustomPreferenceManager.getBoolean(getContext(), "login")){
            getPreferenceScreen().findPreference("notificationSwitch").setEnabled(false);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onResume() {
        super.onResume();
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            /* notificationSwitch on/off */
            if (isAdded() && key.equals("notificationSwitch")) {    // when fragment is attached to activity
                if (sharedPreferences.getBoolean(key, true)) {
                    ((MainActivity) requireActivity()).createNotification();
                } else {
                    ((MainActivity) requireActivity()).removeNotification();
                }
            }

            else if(isAdded() && key.equals("timelineSwitch")){
                // show timeline ();
            }

            else if(isAdded() && key.equals("refreshRate")){
                // change refresh rate ();
            }
        }
    };



}