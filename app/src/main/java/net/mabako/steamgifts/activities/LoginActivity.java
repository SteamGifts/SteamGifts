package net.mabako.steamgifts.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.web.WebUserData;

import org.jsoup.Jsoup;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final String LOGIN_URL = "http://www.steamgifts.com/?login";
    private static final String REDIRECTED_URL = "http://www.steamgifts.com/";
    private static final String STEAM_OPENID_URL = "https://steamcommunity.com/openid/";

    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        webView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JavaScriptContentHandler(), "contenthandler");

        webView.setWebViewClient(new LoginWebViewClient());
        webView.loadUrl(LOGIN_URL);
    }

    public class LoginWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "Page started: " + url);

            if (REDIRECTED_URL.equals(url)) {
                webView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }

            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (REDIRECTED_URL.equals(url)) {
                // Fetch all cookies
                String cookie = CookieManager.getInstance().getCookie(url);
                Log.v(TAG, "Cookies: " + cookie);

                // Look for the session id
                for (String c : cookie.split("; ")) {
                    Log.v(TAG, "Cookie -> " + c);
                    String[] details = c.split("=", 2);
                    if ("PHPSESSID".equals(details[0])) {
                        WebUserData.clear();
                        WebUserData.getCurrent().setSessionId(details[1].trim());

                        webView.loadUrl("javascript:contenthandler.processHTML(document.documentElement.outerHTML);");

                        return;
                    }
                }

                LoginActivity.this.setResult(-1);
                LoginActivity.this.finish();
            } else if (url.startsWith(STEAM_OPENID_URL)) {
                webView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    private class JavaScriptContentHandler {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html) {
            Log.v(TAG, "Parsing HTML for post-login");
            WebUserData.extract(Jsoup.parse(html));

            LoginActivity.this.setResult(BaseActivity.RESPONSE_LOGIN_SUCCESSFUL);
            LoginActivity.this.finish();
        }
    }
}
