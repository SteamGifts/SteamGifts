package net.mabako.steamgifts.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import net.mabako.sgtools.SGToolsLoginActivity;
import net.mabako.steamgifts.R;
import net.mabako.steamgifts.web.SGToolsUserData;
import net.mabako.steamgifts.web.SteamGiftsUserData;

public class BaseActivity extends AppCompatActivity {
    public static final String CLOSE_NESTED = "close-nested";

    private static final String PREF_KEY_SESSION_ID = "session-id";
    private static final String PREF_ACCOUNT = "account";
    private static final String PREF_KEY_USERNAME = "username";
    private static final String PREF_KEY_IMAGE = "image-url";

    private boolean nightMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);

        // Load session & username if possible
        SharedPreferences sp = getSharedPreferences(PREF_ACCOUNT, MODE_PRIVATE);
        if (sp.contains(PREF_KEY_SESSION_ID) && sp.contains(PREF_KEY_USERNAME)) {
            SteamGiftsUserData.getCurrent().setSessionId(sp.getString(PREF_KEY_SESSION_ID, null));
            SteamGiftsUserData.getCurrent().setName(sp.getString(PREF_KEY_USERNAME, null));
            SteamGiftsUserData.getCurrent().setImageUrl(sp.getString(PREF_KEY_IMAGE, null));
        } else {
            SteamGiftsUserData.clear();
        }

        // sgtools.info preferences
        sp = getSharedPreferences(SGToolsLoginActivity.PREF_ACCOUNT, Activity.MODE_PRIVATE);
        if (sp.contains(SGToolsLoginActivity.PREF_KEY_SESSION_ID)) {
            SGToolsUserData.getCurrent().setSessionId(sp.getString(SGToolsLoginActivity.PREF_KEY_SESSION_ID, null));
        } else {
            SGToolsUserData.clear();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (setTheme()) {
            finish();
            startActivity(getIntent());
        }
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

        SteamGiftsUserData account = SteamGiftsUserData.getCurrent();
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

    private boolean setTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean nightMode = prefs.getBoolean("preference_theme_nightmode", false);
        if (nightMode != this.nightMode) {
            this.nightMode = nightMode;
            setTheme(nightMode ? R.style.AppTheme_Dark : R.style.AppTheme_Light);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Handle "up" navigation
     *
     * @param item the used menu item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent data = new Intent();
            data.putExtra(CLOSE_NESTED, getNestingStringForHomePressed());

            setResult(0, data);
            finish();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * What happens when the user presses home?
     */
    public String getNestingStringForHomePressed() {
        return "";
    }
}
