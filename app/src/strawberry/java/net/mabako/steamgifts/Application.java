package net.mabako.steamgifts;

import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import net.mabako.steamgifts.fragments.BetaNotificationDialogFragment;

public class Application extends ApplicationTemplate {
    public static final String PREF_BETA = "beta-app";
    public static final String PREF_KEY_NOTIFICATION_SHOWN = "notification-shown";

    @Override
    public boolean isBetaBuild() {
        return true;
    }

    @Override
    public void showBetaNotification(AppCompatActivity parentActivity, boolean onlyOnFirstLaunch) {
        if (onlyOnFirstLaunch) {
            SharedPreferences sp = getSharedPreferences(PREF_BETA, MODE_PRIVATE);
            if (sp.getBoolean(PREF_KEY_NOTIFICATION_SHOWN, false)) {
                // do not show the fragment
                return;
            }
        }

        FragmentManager fm = parentActivity.getSupportFragmentManager();

        BetaNotificationDialogFragment dialog = new BetaNotificationDialogFragment();
        dialog.setCancelable(false);
        dialog.show(fm, dialog.getClass().getSimpleName());
    }

    @Override
    public String getAppVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAppVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    @Override
    public String getFlavor() {
        return BuildConfig.FLAVOR;
    }
}