package net.mabako.sgtools;

import android.content.SharedPreferences;
import android.os.Bundle;

import net.mabako.common.SteamLoginActivity;
import net.mabako.steamgifts.activities.CommonActivity;
import net.mabako.steamgifts.persistentdata.SGToolsUserData;

public class SGToolsLoginActivity extends SteamLoginActivity {
    private static final String LOGIN_URL = "https://steamcommunity.com/openid/login?openid.ns=http://specs.openid.net/auth/2.0&openid.mode=checkid_setup&openid.return_to=http://www.sgtools.info/login&openid.realm=http://www.sgtools.info&openid.identity=http://specs.openid.net/auth/2.0/identifier_select&openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select";
    private static final String REDIRECTED_URL = "http://www.sgtools.info/";

    public static final String PREF_ACCOUNT = "sgtools:account";
    public static final String PREF_KEY_SESSION_ID = "session-id";

    public SGToolsLoginActivity() {
        super(REDIRECTED_URL, REDIRECTED_URL);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView.postUrl(LOGIN_URL, null);
    }

    @Override
    protected void onLoginSuccessful(String phpSessionId) {
        SGToolsUserData.getCurrent().setSessionId(phpSessionId);

        // Persist all relevant data.
        SharedPreferences.Editor spEditor = getSharedPreferences(PREF_ACCOUNT, MODE_PRIVATE).edit();
        spEditor.putString(PREF_KEY_SESSION_ID, phpSessionId);
        spEditor.apply();

        setResult(CommonActivity.RESPONSE_LOGIN_SGTOOLS_SUCCESSFUL);
        finish();
    }

    @Override
    protected void onLoginCancelled() {
        SGToolsUserData.clear();

        setResult(RESULT_CANCELED);
        finish();
    }
}
