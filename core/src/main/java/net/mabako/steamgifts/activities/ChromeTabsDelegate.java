package net.mabako.steamgifts.activities;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;

import net.mabako.steamgifts.activities.UrlHandlingActivity.IntentDelegate;
import net.mabako.steamgifts.core.R;

public class ChromeTabsDelegate implements IntentDelegate {
    @NonNull
    private final Uri uri;

    public ChromeTabsDelegate(@NonNull Uri uri) {
        this.uri = uri;
    }

    @Override
    public void start(@NonNull Activity activity) {
        @ColorInt
        int color = activity.obtainStyledAttributes(new int[]{R.attr.colorPrimary}).getColor(0, 0);

        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                .setToolbarColor(color)
                .build();

        customTabsIntent.launchUrl(activity, uri);
    }
}
