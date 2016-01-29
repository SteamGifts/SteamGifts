package net.mabako.steamgifts.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.adapters.viewholder.Utils;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.fragments.interfaces.ILoadItemsListener;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;
import net.mabako.steamgifts.tasks.LoadMessagesTask;

import java.util.ArrayList;
import java.util.List;

public class CheckForNewMessages extends BroadcastReceiver {
    private static final String PREF_NOTIFICATIONS_ENABLED = "preference_notifications";

    private static final int NOTIFICATION_ID = 1234;

    /**
     * Number of Comments we display at most.
     */
    private static final int MAX_DISPLAYED_COMMENTS = 5;

    private static final String TAG = CheckForNewMessages.class.getSimpleName();

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.v(TAG, "Checking for new messages...");

        boolean notificationsEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_NOTIFICATIONS_ENABLED, true);
        if (!notificationsEnabled) {
            Log.v(TAG, "Notifications disabled");
            return;
        }

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

        new LoadMessagesTask(new ILoadItemsListener() {
            @Override
            public void addItems(List<? extends IEndlessAdaptable> items, boolean clearExistingItems, String xsrfToken) {
                SteamGiftsUserData userData = SteamGiftsUserData.getCurrent();

                if (items == null || items.size() == 0) {
                    Log.d(TAG, "got no messages -at all-");
                    return;
                }

                List<Comment> mostRecentComments = new ArrayList<>(MAX_DISPLAYED_COMMENTS);
                for (IEndlessAdaptable adaptable : items) {
                    if (adaptable instanceof Comment) {
                        Comment comment = (Comment) adaptable;
                        if (!comment.isHighlighted())
                            // This comment isn't new.
                            break;

                        mostRecentComments.add(comment);
                        if (mostRecentComments.size() == MAX_DISPLAYED_COMMENTS)
                            break;
                    }
                }

                if (mostRecentComments.size() == 0) {
                    Log.v(TAG, "Got no unread messages?");
                } else if (mostRecentComments.size() == 1) {
                    showSingleCommentNotification(context, mostRecentComments.get(0));
                } else {
                    showMultipleCommentNotifications(context, mostRecentComments);
                }
            }
        }, 1).execute();
    }

    private void showSingleCommentNotification(Context context, Comment comment) {
        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.sgwhite)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                .setContentTitle(String.format(context.getString(R.string.notification_user_replied_to_you), comment.getAuthor()))
                .setContentText(formatString(context, comment, false))
                /* 4.1+ */
                .setStyle(new NotificationCompat.BigTextStyle().bigText(formatString(context, comment, false)))
                .build();

        showNotification(context, notification);
    }

    private void showMultipleCommentNotifications(Context context, List<Comment> comments) {
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        for (Comment comment : comments)
            inboxStyle.addLine(formatString(context, comment, true));

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.sgwhite)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                .setContentTitle(String.format(context.getString(R.string.notification_new_messages), SteamGiftsUserData.getCurrent().getMessageNotification()))
                .setContentText(formatString(context, comments.get(0), false))
                .setStyle(inboxStyle)
                .setNumber(SteamGiftsUserData.getCurrent().getMessageNotification())
                .build();

        showNotification(context, notification);
    }

    @NonNull
    private CharSequence formatString(Context context, Comment comment, boolean includeName) {
        String content = Utils.fromHtml(context, comment.getContent()).toString();
        if (includeName && comment.getAuthor() != null) {
            SpannableString sb = new SpannableString(String.format("%s  %s", comment.getAuthor(), content));
            sb.setSpan(new StyleSpan(Typeface.BOLD), 0, comment.getAuthor().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return sb;
        } else {
            return content;
        }
    }

    private void showNotification(Context context, Notification notification) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notification);
    }
}
