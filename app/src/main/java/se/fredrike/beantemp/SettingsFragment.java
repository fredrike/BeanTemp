package se.fredrike.beantemp;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by fer on 2017-01-20.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

    }
}