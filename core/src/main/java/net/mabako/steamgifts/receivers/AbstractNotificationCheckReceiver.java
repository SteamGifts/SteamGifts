package net.mabako.steamgifts.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import java.util.List;

public abstract class AbstractNotificationCheckReceiver extends BroadcastReceiver {
    private static final String DEFAULT_PREF_NOTIFICATIONS_ENABLED = "preference_notifications";

    /**
     * The preference wherein all notification services store their stuff.
     */
    protected static final String PREFS_NOTIFICATIONS_SERVICE = "notification-service";

    /**
     * Number of notifications we display at most.
     */
    protected static final int MAX_DISPLAYED_NOTIFICATIONS = 5;

    public enum NotificationId {
        MESSAGES, WON, NO_TYPE
    }

    /**
     * Check if we should run the network task.
     *
     * @param tag     tag to be used for logging
     * @param context context of the broadcast receiver's onReceive
     * @return true if we should execute the network task, false otherwise
     */
    protected static boolean shouldRunNetworkTask(final String tag, final Context context) {
        boolean notificationsEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DEFAULT_PREF_NOTIFICATIONS_ENABLED, true);
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

    /**
     * Display a notification for a single item.
     *
     * @param context        receiver context
     * @param notificationId the notification id to replace (should be unique per class)
     * @param iconResource   icon to display along with the notification
     * @param title          title
     * @param content        text to display in the notification
     * @param viewIntent     what happens when clicking the notification
     * @param deleteIntent   what happens when dismissing the notification
     */
    protected static void showNotification(Context context, NotificationId notificationId, @DrawableRes int iconResource, String title, CharSequence content, PendingIntent viewIntent, PendingIntent deleteIntent) {
        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(iconResource)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content)) /* 4.1+ */
                .setContentIntent(viewIntent)
                .setDeleteIntent(deleteIntent)
                .setAutoCancel(true)
                .build();

        showNotification(context, notificationId, notification);
    }

    /**
     * Display a notification for multiple items.
     *
     * @param context        receiver context
     * @param notificationId the notification id to replace (should be unique per class)
     * @param iconResource   icon to display along with the notification
     * @param title          title
     * @param content        texts to display in the notification
     * @param viewIntent     what happens when clicking the notification
     * @param deleteIntent   what happens when dismissing the notification
     */

    protected static void showNotification(Context context, NotificationId notificationId, @DrawableRes int iconResource, String title, List<CharSequence> content, PendingIntent viewIntent, PendingIntent deleteIntent) {
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        for (CharSequence c : content)
            inboxStyle.addLine(c);

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(iconResource)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                .setContentTitle(title)
                .setContentText(content.get(0))
                .setStyle(inboxStyle) /* 4.1+ */
                .setNumber(SteamGiftsUserData.getCurrent(context).getMessageNotification())
                .setContentIntent(viewIntent)
                .setDeleteIntent(deleteIntent)
                .setAutoCancel(true)
                .build();

        showNotification(context, notificationId, notification);
    }

    /**
     * Show the built notification in the systray.
     *
     * @param context        receiver context
     * @param notificationId notification id to use for this notification
     * @param notification   notification to display
     */
    private static void showNotification(Context context, NotificationId notificationId, Notification notification) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(notificationId.ordinal(), notification);
    }
}
