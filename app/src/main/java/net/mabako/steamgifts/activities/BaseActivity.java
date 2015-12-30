package net.mabako.steamgifts.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import net.mabako.steamgifts.web.WebUserData;

/**
 * Created by mabako on 30.12.2015.
 */
public class BaseActivity extends AppCompatActivity {
    public static final int REQUEST_LOGIN = 3;
    public static final int REQUEST_LOGIN_PASSIVE = 4;

    public static final int RESPONSE_LOGIN_SUCCESSFUL = 5;

    public static final String PREF_KEY_SESSION_ID = "session-id";
    public static final String PREF_ACCOUNT = "account";
    public static final String PREF_KEY_USERNAME = "username";
    public static final String PREF_KEY_IMAGE = "image-url";

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
