package net.mabako.steamgifts.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

public abstract class AbstractCheckReceiver extends BroadcastReceiver {
    /**
     * Check if we should run the network task.
     *
     * @param tag            tag to be used for logging
     * @param context        context of the broadcast receiver's onReceive
     * @param preferenceName name of the preference to check for enabled notifications
     * @return true if we should execute the network task, false otherwise
     */
    protected static boolean shouldRunNetworkTask(final String tag, final Context context, final String preferenceName) {
        boolean notificationsEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(preferenceName, true);
        if (!notificationsEnabled) {
            Log.v(tag, "Notifications disabled");
            return false;
        }

        SteamGiftsUserData userData = SteamGiftsUserData.getCurrent(context);
        if (!userData.isLoggedIn()) {
            Log.v(tag, "Not checking for remote data, no session info available");
            return false;
        }

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected() || activeNetworkInfo.getType() != ConnectivityManager.TYPE_WIFI) {
            Log.v(tag, "Not checking for messages due to network info: " + activeNetworkInfo);
            return false;
        }

        return true;
    }
}
