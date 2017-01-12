package net.mabako.steamgifts.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import net.mabako.steamgifts.core.R;

/**
 * Simple activity to hold a WebView.
 */
public class WebViewActivity extends CommonActivity {
    public static final String ARG_URL = "url";
    public static final String ARG_NO_BACK_STACK = "no-back-stack";

    private WebView webView;
    private ActionBar toolbar;
    private ProgressBar progressBar;

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
        toolbar.setSubtitle(getUrl(url));

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        webView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setDisplayZoomControls(false);

        webView.setWebViewClient(new CustomWebViewClient());
        webView.setWebChromeClient(new CustomWebChromeClient());
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                // Open the file to download externally.
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        });
        webView.loadUrl(url);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack() && !getIntent().hasExtra(ARG_NO_BACK_STACK))
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
        int i = item.getItemId();
        if (i == R.id.open_browser) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webView.getUrl())));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private class CustomWebChromeClient extends WebChromeClient {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            Log.d("webview", "receive title");

            toolbar.setTitle(title);
            toolbar.setSubtitle(getUrl(view.getUrl()));
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setProgress(newProgress);
        }
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageCommitVisible(WebView view, String url) {
            toolbar.setTitle(view.getTitle());
            toolbar.setSubtitle(getUrl(url));
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Toast.makeText(WebViewActivity.this, "Invalid SSL Certificate.", Toast.LENGTH_SHORT).show();

            handler.cancel();
        }

        /**
         * If the URL is handled by the application, we want to start that as activity instead of loading the related webview.
         *
         * @param view current webview
         * @param uri  url to load
         * @return {@code true} if this app handles the related url, {@code false} otherwise
         */
        private boolean shouldOverrideUrlLoading(WebView view, Uri uri) {
            Intent intent = UrlHandlingActivity.getIntentForUri(WebViewActivity.this, uri);
            if (intent != null) {
                // Should we mark the next context read?
                if (getIntent().hasExtra(DetailActivity.ARG_MARK_CONTEXT_READ))
                    intent.putExtra(DetailActivity.ARG_MARK_CONTEXT_READ, true);

                startActivity(intent);
                checkForFinishActivity();
                return true;
            }
            return false;
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return shouldOverrideUrlLoading(view, request.getUrl());
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return shouldOverrideUrlLoading(view, Uri.parse(url));
        }
    }

    /**
     * Finish this activity, if we were opened with a 'go to comment' page, and we've not navigated in any way.
     */
    private void checkForFinishActivity() {
        String url = getIntent().getStringExtra(ARG_URL);
        if (!webView.canGoBack() && (url.startsWith("https://www.steamgifts.com/go/") || url.startsWith("https://www.steamgifts.com/user/id/")))
            finish();
    }

    /**
     * Return the host name.
     */
    private CharSequence getUrl(String url) {
        Uri uri = Uri.parse(url);
        if ("https".equals(uri.getScheme()))
            return "https://" + uri.getHost();
        return uri.getHost();
    }
}
