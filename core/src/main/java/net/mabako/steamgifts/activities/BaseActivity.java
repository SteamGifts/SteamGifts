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
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.persistentdata.SGToolsUserData;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

public class BaseActivity extends AppCompatActivity {
    public static final String CLOSE_NESTED = "close-nested";

    private boolean nightMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);

        // sgtools.info preferences
        SharedPreferences sp = getSharedPreferences(SGToolsLoginActivity.PREF_ACCOUNT, Activity.MODE_PRIVATE);
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

    protected Fragment getCurrentFragment(String fragmentTag) {
        return getSupportFragmentManager().findFragmentByTag(fragmentTag);
    }

    protected void onAccountChange() {
        // Persist all relevant data.
        SteamGiftsUserData.getCurrent(this).save(this);
    }

    private boolean setTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean nightMode = prefs.getBoolean("preference_theme_nightmode", true);
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
