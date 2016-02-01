package com.dhsdevelopments.potato.channelmessages.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.dhsdevelopments.potato.R;

public class ChannelSettingsFragment extends PreferenceFragment
{
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        addPreferencesFromResource( R.xml.channel_prefs );
    }
}
