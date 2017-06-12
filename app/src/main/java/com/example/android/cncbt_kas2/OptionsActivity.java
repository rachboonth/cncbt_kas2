package com.example.android.cncbt_kas2;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.example.android.cnccommander.C0013R;

public class OptionsActivity extends PreferenceActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(C0013R.layout.preferences);
    }
}
