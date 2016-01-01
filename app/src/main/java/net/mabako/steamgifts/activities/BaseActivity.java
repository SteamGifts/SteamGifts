package net.mabako.steamgifts.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.fragments.IFragmentNotifications;
import net.mabako.steamgifts.web.WebUserData;

public class BaseActivity extends AppCompatActivity {
    private static  final String TAG = BaseActivity.class.getSimpleName();

    public static final int REQUEST_LOGIN = 3;
    public static final int REQUEST_LOGIN_PASSIVE = 4;

    public static final int RESPONSE_LOGIN_SUCCESSFUL = 5;

    private static final String PREF_KEY_SESSION_ID = "session-id";
    private static final String PREF_ACCOUNT = "account";
    private static final String PREF_KEY_USERNAME = "username";
    private static final String PREF_KEY_IMAGE = "image-url";
    private static final String FRAGMENT_TAG = "Main Fragment Thing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

    public void requestLogin() {
        startActivityForResult(new Intent(this, LoginActivity.class), REQUEST_LOGIN);
    }

    protected void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.replace(R.id.fragment_container, fragment, FRAGMENT_TAG);

        ft.commitAllowingStateLoss();

        // Update the title.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            Log.v(TAG, "Current Fragment is a " + fragment.getClass().getName());
            if (fragment instanceof IFragmentNotifications) {
                int resource = ((IFragmentNotifications) fragment).getTitleResource();
                String title = getString(resource);

                String extra = ((IFragmentNotifications) fragment).getExtraTitle();
                if(extra != null && !extra.isEmpty())
                    title = String.format("%s: %s", extra, title);

                actionBar.setTitle(title);
                Log.v(TAG, "Setting Toolbar title to " + title);
            } else
                actionBar.setTitle(R.string.app_name);
        }
    }

    public Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOGIN:
            case REQUEST_LOGIN_PASSIVE:
                // Do not show an explicit notification.
                if (resultCode == RESPONSE_LOGIN_SUCCESSFUL && WebUserData.getCurrent().isLoggedIn())
                    onAccountChange();

                // Pass on the result.
                setResult(resultCode);

                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
