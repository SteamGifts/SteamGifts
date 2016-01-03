package net.mabako.steamgifts.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.web.WebUserData;

public class BaseActivity extends AppCompatActivity {
    private static final String PREF_KEY_SESSION_ID = "session-id";
    private static final String PREF_ACCOUNT = "account";
    private static final String PREF_KEY_USERNAME = "username";
    private static final String PREF_KEY_IMAGE = "image-url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);

        // Load session & username if possible
        SharedPreferences sp = getSharedPreferences(PREF_ACCOUNT, MODE_PRIVATE);
        if (sp.contains(PREF_KEY_SESSION_ID) && sp.contains(PREF_KEY_USERNAME)) {
            WebUserData.getCurrent().setSessionId(sp.getString(PREF_KEY_SESSION_ID, null));
            WebUserData.getCurrent().setName(sp.getString(PREF_KEY_USERNAME, null));
            WebUserData.getCurrent().setImageUrl(sp.getString(PREF_KEY_IMAGE, null));
        } else {
            WebUserData.clear();
        }
    }

    @Override
    protected void onResume() {
        setTheme();
        super.onResume();
    }

    protected void loadFragment(int id, Fragment fragment, String tag) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.replace(id, fragment, tag);

        ft.commitAllowingStateLoss();
    }

    protected void onAccountChange() {
        // Persist all relevant data.
        SharedPreferences.Editor spEditor = getSharedPreferences(PREF_ACCOUNT, MODE_PRIVATE).edit();

        WebUserData account = WebUserData.getCurrent();
        if (account.isLoggedIn()) {
            spEditor.putString(PREF_KEY_SESSION_ID, account.getSessionId());
            spEditor.putString(PREF_KEY_USERNAME, account.getName());
            spEditor.putString(PREF_KEY_IMAGE, account.getImageUrl());
        } else {
            spEditor.remove(PREF_KEY_SESSION_ID);
            spEditor.remove(PREF_KEY_USERNAME);
            spEditor.remove(PREF_KEY_IMAGE);
        }
        spEditor.apply();
    }

    private void setTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean nightMode = prefs.getBoolean("preference_theme_nightmode", false);

        setTheme(nightMode ? R.style.AppTheme_Dark : R.style.AppTheme_Light);
    }
}
