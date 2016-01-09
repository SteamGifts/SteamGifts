package net.mabako.steamgifts.activities.settings;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.activities.BaseActivity;
import net.mabako.steamgifts.compat.PreferenceFragment;
import net.mabako.steamgifts.web.SteamGiftsUserData;

public class SettingsActivity extends BaseActivity {
    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_app);

            if (SteamGiftsUserData.getCurrent().isLoggedIn()) {
                addPreferencesFromResource(R.xml.preferences_sg);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_one_fragment);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loadFragment(R.id.fragment_container, new SettingsFragment(), "settings");
    }
}
