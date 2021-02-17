package com.userempowermentlab.kidsrecorder.UI;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.View;

import com.userempowermentlab.kidsrecorder.R;

/**
 * Fragment UI class for preference setting
 */

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
    EditTextPreference record_length;
    EditTextPreference preceding_time;
    EditTextPreference storage_fileprefix;
    EditTextPreference storage_buffersize;
    EditTextPreference storage_limit;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.setting_preference);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        record_length = (EditTextPreference) findPreference("record_length");
        record_length.setOnPreferenceChangeListener(this);

        preceding_time = (EditTextPreference) findPreference("preceding_time");
        preceding_time.setOnPreferenceChangeListener(this);

        storage_fileprefix = (EditTextPreference) findPreference("storage_fileprefix");
        storage_fileprefix.setOnPreferenceChangeListener(this);
        storage_buffersize = (EditTextPreference) findPreference("storage_buffersize");
        storage_buffersize.setOnPreferenceChangeListener(this);
        storage_limit = (EditTextPreference) findPreference("storage_limit");
        storage_limit.setOnPreferenceChangeListener(this);
    }

    /**
     * Here we add some checkes for certain options
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // input values should be positive numbers
        if (preference == record_length || preference == storage_buffersize || preference == storage_limit || preference == preceding_time){
            try{
                int n = Integer.parseInt(newValue.toString());
                if (n < 0) return false;
            } catch (Exception e){
                return false;
            }
        }
        // input values should be a legal file prefix
        else if (preference == storage_fileprefix){
            String name = newValue.toString();
            name = name.replaceAll("\\W+", "");
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString("storage_fileprefix", name).commit();
            storage_fileprefix.setText(name);
            return false;
        }
        return true;
    }
}
