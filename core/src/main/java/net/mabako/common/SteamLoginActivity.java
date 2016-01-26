package net.mabako.common;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import net.mabako.steamgifts.core.R;

public abstract class SteamLoginActivity extends AppCompatActivity {
    private static final String TAG = SteamLoginActivity.class.getSimpleName();
    private static final String STEAM_OPENID_URL = "https://steamcommunity.com/openid/";

    private final String redirectUrl;
    private final String subtitle;

    protected WebView webView;
    protected ProgressBar progressBar;

    public SteamLoginActivity(String redirectUrl, String subtitle) {
        this.redirectUrl = redirectUrl;
        this.subtitle = subtitle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setSubtitle(subtitle);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        webView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new SteamWebViewClient());
    }

    protected abstract void onLoginSuccessful(String phpSessionId);

    protected abstract void onLoginCancelled();

    @Override
    protected void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }

    private class SteamWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "Page started: " + url);

            if (redirectUrl.equals(url)) {
                webView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }

            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (redirectUrl.equals(url)) {
                // Fetch all cookies
                String cookie = CookieManager.getInstance().getCookie(url);
                Log.v(TAG, "Cookies: " + cookie);

                // Look for the session id
                for (String c : cookie.split("; ")) {
                    String[] details = c.split("=", 2);
                    if ("PHPSESSID".equals(details[0])) {
                        Log.d(TAG, "onLoginSuccessful(" + details[1].trim() + ")");
                        onLoginSuccessful(details[1].trim());
                        return;
                    }
                }

                Log.d(TAG, "onLoginCancelled()");
                onLoginCancelled();
            } else if (url.startsWith(STEAM_OPENID_URL)) {
                webView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }
    }


}
