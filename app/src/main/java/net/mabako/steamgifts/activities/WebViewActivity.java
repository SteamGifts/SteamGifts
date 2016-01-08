package net.mabako.steamgifts.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.mabako.steamgifts.R;

/**
 * Simple activity to hold a WebView.
 */
public class WebViewActivity extends CommonActivity {
    public static final String ARG_URL = "url";

    private WebView webView;
    private ActionBar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String url = getIntent().getStringExtra(ARG_URL);
        if (TextUtils.isEmpty(url)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_web_view);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        toolbar = getSupportActionBar();
        toolbar.setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(R.string.loading);
        toolbar.setSubtitle(url);

        webView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            webSettings.setDisplayZoomControls(false);

        webView.setWebViewClient(new CustomWebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack())
            webView.goBack();
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.web_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.open_browser:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webView.getUrl())));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            toolbar.setTitle(R.string.loading);
            toolbar.setSubtitle(url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            toolbar.setTitle(view.getTitle());
            toolbar.setSubtitle(url);
        }

        /**
         * If the URL is handled by the application, we want to start that as activity instead of loading the related webview.
         *
         * @param view current webview
         * @param url  url to load
         * @return {@code true} if this app handles the related url, {@code false} otherwise
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Intent intent = UrlHandlingActivity.getIntentForUri(WebViewActivity.this, Uri.parse(url));
            if (intent != null) {
                startActivity(intent);
                return true;
            }
            return false;
        }
    }
}
