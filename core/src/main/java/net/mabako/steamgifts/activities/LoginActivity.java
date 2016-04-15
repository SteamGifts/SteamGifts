package net.mabako.steamgifts.activities;

import android.os.Bundle;
import android.webkit.JavascriptInterface;

import net.mabako.common.SteamLoginActivity;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import org.jsoup.Jsoup;

public class LoginActivity extends SteamLoginActivity {
    private static final String LOGIN_URL = "https://www.steamgifts.com/?login";
    private static final String REDIRECTED_URL = "https://www.steamgifts.com/";

    public LoginActivity() {
        super(REDIRECTED_URL, REDIRECTED_URL);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView.addJavascriptInterface(new JavaScriptContentHandler(), "contenthandler");
        webView.loadUrl(LOGIN_URL);
    }

    @Override
    protected void onLoginSuccessful(String phpSessionId) {
        SteamGiftsUserData.clear();
        SteamGiftsUserData.getCurrent(this).setSessionId(phpSessionId);

        webView.loadUrl("javascript:contenthandler.processHTML(document.documentElement.outerHTML);");
    }

    @Override
    protected void onLoginCancelled() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private class JavaScriptContentHandler {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html) {
            SteamGiftsUserData.extract(LoginActivity.this, Jsoup.parse(html));

            setResult(CommonActivity.RESPONSE_LOGIN_SUCCESSFUL);
            finish();
        }
    }
}
