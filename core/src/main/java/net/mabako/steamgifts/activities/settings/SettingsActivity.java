package net.mabako.steamgifts.activities.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import net.mabako.sgtools.SGToolsLoginActivity;
import net.mabako.steamgifts.activities.BaseActivity;
import net.mabako.steamgifts.activities.CommonActivity;
import net.mabako.steamgifts.compat.PreferenceFragment;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.persistentdata.SGToolsUserData;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

public class SettingsActivity extends BaseActivity {
    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_app);

            if (SteamGiftsUserData.getCurrent().isLoggedIn()) {
                addPreferencesFromResource(R.xml.preferences_sg);

                final PreferenceCategory category = (PreferenceCategory) findPreference("preferences_sg_header");

                findPreference("preference_sg_logout").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        getActivity().setResult(CommonActivity.RESPONSE_LOGOUT);
                        getActivity().finish();
                        return true;
                    }
                });
            }

            if (SGToolsUserData.getCurrent().isLoggedIn()) {
                addPreferencesFromResource(R.xml.preferences_sgtools);

                final PreferenceCategory category = (PreferenceCategory) findPreference("preferences_sgtools_header");

                findPreference("preference_sgtools_logout").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        SharedPreferences.Editor spEditor = getContext().getSharedPreferences(SGToolsLoginActivity.PREF_ACCOUNT, MODE_PRIVATE).edit();
                        spEditor.remove(SGToolsLoginActivity.PREF_KEY_SESSION_ID);
                        spEditor.apply();

                        SGToolsUserData.clear();
                        Toast.makeText(getContext(), R.string.preference_sgtools_logged_out, Toast.LENGTH_SHORT).show();

                        getPreferenceScreen().removePreference(category);
                        return true;
                    }
                });
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
