package com.riel_dev.hayaku;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

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
                if (sharedPreferences.getBoolean(key, false)) {
                    ((MainActivity) requireActivity()).createNotification();
                } else {
                    ((MainActivity) requireActivity()).removeNotification();
                }
            }

            /* start service at boot on/off */
            else if(isAdded() && key.equals("bootNotificationSwitch")){
                if(sharedPreferences.getBoolean(key, false)){
                    CustomPreferenceManager.setBoolean(getContext(), key, true);
                    Toast.makeText(getContext(),"Now Hayaku will start on boot", Toast.LENGTH_SHORT).show();
                }else{
                    CustomPreferenceManager.setBoolean(getContext(), key, false);
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