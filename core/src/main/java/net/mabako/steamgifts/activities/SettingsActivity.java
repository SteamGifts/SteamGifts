package net.mabako.steamgifts.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import net.mabako.sgtools.SGToolsLoginActivity;
import net.mabako.steamgifts.ApplicationTemplate;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.fragments.WhitelistBlacklistFragment;
import net.mabako.steamgifts.persistentdata.SGToolsUserData;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

public class SettingsActivity extends BaseActivity {
    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_app);

            if (SteamGiftsUserData.getCurrent(getActivity()).isLoggedIn()) {
                addPreferencesFromResource(R.xml.preferences_sg);

                findPreference("preference_sg_sync").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        getActivity().startActivity(new Intent(getActivity(), SyncActivity.class));
                        return true;
                    }
                });

                findPreference("preference_sg_whitelist").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(getActivity(), DetailActivity.class);
                        intent.putExtra(WhitelistBlacklistFragment.ARG_TYPE, WhitelistBlacklistFragment.What.WHITELIST);
                        getActivity().startActivity(intent);
                        return true;
                    }
                });

                findPreference("preference_sg_blacklist").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(getActivity(), DetailActivity.class);
                        intent.putExtra(WhitelistBlacklistFragment.ARG_TYPE, WhitelistBlacklistFragment.What.BLACKLIST);
                        getActivity().startActivity(intent);
                        return true;
                    }
                });

                findPreference("preference_sg_hidden_games").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(getActivity(), DetailActivity.class);
                        intent.putExtra(DetailActivity.ARG_HIDDEN_GAMES, true);
                        getActivity().startActivity(intent);
                        return true;
                    }
                });

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
                        SharedPreferences.Editor spEditor = getActivity().getSharedPreferences(SGToolsLoginActivity.PREF_ACCOUNT, MODE_PRIVATE).edit();
                        spEditor.remove(SGToolsLoginActivity.PREF_KEY_SESSION_ID);
                        spEditor.apply();

                        SGToolsUserData.clear();
                        Toast.makeText(getActivity(), R.string.preference_sgtools_logged_out, Toast.LENGTH_SHORT).show();

                        getPreferenceScreen().removePreference(category);
                        return true;
                    }
                });
            }

            addPreferencesFromResource(R.xml.preferences_other);

            ListPreference browserPreferences = (ListPreference) findPreference("preference_external_browser");

            boolean tabsSupported = ChromeTabsDelegate.isCustomTabsSupported(getActivity());
            if (tabsSupported) {
                // We have some chrome version installed which supports custom tabs.
                browserPreferences.setEntries(R.array.preference_browser_entries_with_tabs);
                browserPreferences.setEntryValues(R.array.preference_browser_entry_values_with_tabs);
            } else {
                // No chrome, no tabs. This probably is also the case for API levels < Jellybean, at least custom tabs are not officially supported before that.
                browserPreferences.setEntries(R.array.preference_browser_entries);
                browserPreferences.setEntryValues(R.array.preference_browser_entry_values);

            }

            if (!tabsSupported || isDefaultApp()) {
                // Notification for default app isn't displayed if we -are- the default app.
                // TODO if we're not the default app, maybe offer a selection to set this the default at least?
                Preference chromeTabsInfo = findPreference("tools_preference_default_app");
                ((PreferenceCategory) findPreference("preferences_other")).removePreference(chromeTabsInfo);
            }

            if (!((ApplicationTemplate) getActivity().getApplication()).allowGameImages()) {
                Preference images = findPreference("preference_giveaway_load_images");
                ((PreferenceCategory) findPreference("preferences_giveaways")).removePreference(images);
            }
        }

        private boolean isDefaultApp() {
            PackageManager pm = getActivity().getPackageManager();
            ResolveInfo resolveInfo = pm.resolveActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.steamgifts.com/giveaway/xxxxx/")), PackageManager.MATCH_DEFAULT_ONLY);
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (activityInfo == null || !activityInfo.exported)
                return false;

            Log.d(SettingsActivity.class.getSimpleName(), "Current default handler for SteamGifts URLs: " + activityInfo.name);
            return UrlHandlingActivity.class.getName().equals(activityInfo.name);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }

        getFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
    }
}
