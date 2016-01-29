package net.mabako.steamgifts.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

public class CheckForNewMessages extends BroadcastReceiver {
    private static final String PREF_NOTIFICATIONS_ENABLED = "preference_notifications";

    private static final String TAG = CheckForNewMessages.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "Checking for new messages...");

        boolean notificationsEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_NOTIFICATIONS_ENABLED, true);
        if (!notificationsEnabled)
            return;

        SteamGiftsUserData userData = SteamGiftsUserData.getCurrent();
        if (!userData.isLoggedIn()) {
            Log.v(TAG, "Not checking for messages, no session info available");
            return;
        }

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected() && activeNetworkInfo.getType() != ConnectivityManager.TYPE_WIFI) {
            Log.v(TAG, "Not checking for messages due to network info: " + activeNetworkInfo);
            return;
        }
    }
}
