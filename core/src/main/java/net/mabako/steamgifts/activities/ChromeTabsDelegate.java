package net.mabako.steamgifts.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsService;
import android.util.Log;

import net.mabako.steamgifts.activities.UrlHandlingActivity.IntentDelegate;
import net.mabako.steamgifts.core.R;

import java.util.ArrayList;
import java.util.List;

public class ChromeTabsDelegate implements IntentDelegate {
    private static final String TAG = ChromeTabsDelegate.class.getSimpleName();

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
                .setShowTitle(true)
                .build();
        customTabsIntent.launchUrl(activity, uri);
    }

    public static boolean isCustomTabsSupported(Context context) {
        return Helper.getPackageNameToUse(context) != null;
    }

    public static class Helper {
        private static final String STABLE_PACKAGE = "com.android.chrome";
        private static final String BETA_PACKAGE = "com.chrome.beta";
        private static final String DEV_PACKAGE = "com.chrome.dev";

        private static String sPackageNameToUse;

        public static String getPackageNameToUse(Context context) {
            if (sPackageNameToUse != null) return sPackageNameToUse;

            PackageManager pm = context.getPackageManager();
            // Get default VIEW intent handler.
            Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"));
            ResolveInfo defaultViewHandlerInfo = pm.resolveActivity(activityIntent, 0);
            String defaultViewHandlerPackageName = null;
            if (defaultViewHandlerInfo != null) {
                defaultViewHandlerPackageName = defaultViewHandlerInfo.activityInfo.packageName;
            }

            // Get all apps that can handle VIEW intents.
            List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, 0);
            List<String> packagesSupportingCustomTabs = new ArrayList<>();
            for (ResolveInfo info : resolvedActivityList) {
                Intent serviceIntent = new Intent();
                serviceIntent.setAction(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION);
                serviceIntent.setPackage(info.activityInfo.packageName);
                if (pm.resolveService(serviceIntent, 0) != null) {
                    packagesSupportingCustomTabs.add(info.activityInfo.packageName);
                }
            }

            // Now packagesSupportingCustomTabs contains all apps that can handle both VIEW intents
            // and service calls.
            if (packagesSupportingCustomTabs.isEmpty()) {
                sPackageNameToUse = null;
            } else if (packagesSupportingCustomTabs.size() == 1) {
                sPackageNameToUse = packagesSupportingCustomTabs.get(0);
            } else if (!android.text.TextUtils.isEmpty(defaultViewHandlerPackageName)
                    && !hasSpecializedHandlerIntents(context, activityIntent)
                    && packagesSupportingCustomTabs.contains(defaultViewHandlerPackageName)) {
                sPackageNameToUse = defaultViewHandlerPackageName;
            } else if (packagesSupportingCustomTabs.contains(STABLE_PACKAGE)) {
                sPackageNameToUse = STABLE_PACKAGE;
            } else if (packagesSupportingCustomTabs.contains(BETA_PACKAGE)) {
                sPackageNameToUse = BETA_PACKAGE;
            } else if (packagesSupportingCustomTabs.contains(DEV_PACKAGE)) {
                sPackageNameToUse = DEV_PACKAGE;
            }
            return sPackageNameToUse;
        }

        private static boolean hasSpecializedHandlerIntents(Context context, Intent intent) {
            try {
                PackageManager pm = context.getPackageManager();
                List<ResolveInfo> handlers = pm.queryIntentActivities(
                        intent,
                        PackageManager.GET_RESOLVED_FILTER);
                if (handlers == null || handlers.size() == 0) {
                    return false;
                }
                for (ResolveInfo resolveInfo : handlers) {
                    IntentFilter filter = resolveInfo.filter;
                    if (filter == null) continue;
                    if (filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0)
                        continue;
                    if (resolveInfo.activityInfo == null) continue;
                    return true;
                }
            } catch (RuntimeException e) {
                Log.e(TAG, "Runtime exception while getting specialized handlers");
            }
            return false;
        }
    }
}
