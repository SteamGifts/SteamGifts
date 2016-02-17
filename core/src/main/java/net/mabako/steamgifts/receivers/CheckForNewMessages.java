package net.mabako.steamgifts.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;

import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.activities.UrlHandlingActivity;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.adapters.viewholder.StringUtils;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.fragments.interfaces.ILoadItemsListener;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;
import net.mabako.steamgifts.tasks.LoadMessagesTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Check for new messages at <a href="http://www.steamgifts.com/messages">steamgifts.com/messages</a>
 * and display a notification if any exists.
 */
public class CheckForNewMessages extends AbstractCheckReceiver {
    private static final String DEFAULT_PREF_NOTIFICATIONS_ENABLED = "preference_notifications";

    private static final String PREFS_NOTIFICATIONS_SERVICE = "notification-service";
    private static final String PREF_KEY_LAST_SHOWN_NOTIFICATION = "last-shown-notification";
    private static final String PREF_KEY_LAST_DISMISSED_NOTIFICATION = "last-dismissed-notification";

    private static final String ACTION_DELETE = "delete";
    private static final String EXTRA_COMMENT_ID = "comment-id";

    private static final int NOTIFICATION_ID = 1234;

    /**
     * Number of Comments we display at most.
     */
    private static final int MAX_DISPLAYED_COMMENTS = 5;

    private static final String TAG = CheckForNewMessages.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            Log.v(TAG, "Checking for new messages...");
            if (shouldRunNetworkTask(TAG, context, DEFAULT_PREF_NOTIFICATIONS_ENABLED)) {
                new LoadMessagesTask(new Check(context), context, 1).execute();
            }
        } else if (ACTION_DELETE.equals(action)) {
            // If we explicitly dismiss this notification, we want to stop this message from re-appearing ever.
            String lastDismissedId = intent.getStringExtra(EXTRA_COMMENT_ID);
            if (TextUtils.isEmpty(lastDismissedId)) {
                Log.w(TAG, "Trying to execute " + ACTION_DELETE + " without a comment id");
                return;
            }

            setLastDismissedCommentId(context, lastDismissedId);
        } else {
            Log.w(TAG, "Trying to execute action " + action);
        }
    }

    public static void setLastDismissedCommentId(@NonNull Context context, @NonNull String commentId) {
        Log.v(TAG, "Marking message " + commentId + " as dismissed");
        context.getSharedPreferences(PREFS_NOTIFICATIONS_SERVICE, Context.MODE_PRIVATE).edit().putString(PREF_KEY_LAST_DISMISSED_NOTIFICATION, commentId).apply();
    }

    private static class Check implements ILoadItemsListener {
        private final Context context;
        private String lastCommentId;

        public Check(Context context) {
            this.context = context;
        }

        /**
         * Callback for {@link LoadMessagesTask}
         */
        @Override
        public void addItems(List<? extends IEndlessAdaptable> items, boolean clearExistingItems, String xsrfToken) {
            if (items == null || items.size() == 0) {
                Log.d(TAG, "got no messages -at all-");
                return;
            }

            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NOTIFICATIONS_SERVICE, Context.MODE_PRIVATE);
            String lastDismissedId = sharedPreferences.getString(PREF_KEY_LAST_DISMISSED_NOTIFICATION, "meow");

            List<Comment> mostRecentComments = new ArrayList<>(MAX_DISPLAYED_COMMENTS);
            for (IEndlessAdaptable adaptable : items) {
                if (adaptable instanceof Comment) {
                    Comment comment = (Comment) adaptable;
                    if (!comment.isHighlighted())
                        // This comment isn't new.
                        break;

                    if (lastDismissedId.equals(comment.getPermalinkId()))
                        // we've dismissed this comment before, explicitly
                        break;

                    mostRecentComments.add(comment);
                    if (mostRecentComments.size() == MAX_DISPLAYED_COMMENTS)
                        break;
                }
            }

            if (mostRecentComments.isEmpty()) {
                Log.v(TAG, "Got no unread messages, or we've dismissed the last message we could see");
            } else {
                String lastShownId = sharedPreferences.getString(PREF_KEY_LAST_SHOWN_NOTIFICATION, null);

                // While this same comment may appear in a later notification, we're establishing that it may not be the first comment again.

                // This is simply so you're not being notified about the same message(s) over and over again, and if it's still unread when a new
                // message is pushed, we may show it again.

                // Contrary, dismissing a message will never show it to you again. "Ignoring" a message here will simply show it stacked
                // with other new messages in the future.
                Comment firstComment = mostRecentComments.get(0);
                if (lastShownId != null && lastShownId.equals(firstComment.getPermalinkId())) {
                    // We've shown a notification for the very same comment before, so do nothing...
                    Log.d(TAG, "Most recent comment has the same comment id as the last comment");
                    return;
                }
                this.lastCommentId = mostRecentComments.get(0).getPermalinkId();

                // Save the last comment id
                sharedPreferences.edit().putString(PREF_KEY_LAST_SHOWN_NOTIFICATION, firstComment.getPermalinkId()).apply();

                // Do we show a single (expanded) content or a bunch of comments?
                if (mostRecentComments.size() == 1) {
                    showSingleCommentNotification(mostRecentComments.get(0));
                } else {
                    showMultipleCommentNotifications(mostRecentComments);
                }
            }
        }

        /**
         * If only a single unread message exists, show only the single message.
         *
         * @param comment the comment to show
         */
        private void showSingleCommentNotification(Comment comment) {
            Notification notification = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.sgwhite)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                    .setContentTitle(String.format(context.getString(R.string.notification_user_replied_to_you), comment.getAuthor()))
                    .setContentText(formatComment(comment, false))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(formatComment(comment, false))) /* 4.1+ */
                    .setContentIntent(getViewMessageIntent(comment))
                    .setDeleteIntent(getDeleteIntent())
                    .setAutoCancel(true)
                    .build();

            showNotification(notification);
        }

        /**
         * If multiple un-read messages are shown which we've not seen previously, show them all.
         *
         * @param comments the comments to display
         */
        private void showMultipleCommentNotifications(List<Comment> comments) {
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            for (Comment comment : comments)
                inboxStyle.addLine(formatComment(comment, true));

            Notification notification = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.sgwhite)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                    .setContentTitle(String.format(context.getString(R.string.notification_new_messages), SteamGiftsUserData.getCurrent(context).getMessageNotification()))
                    .setContentText(formatComment(comments.get(0), true))
                    .setStyle(inboxStyle) /* 4.1+ */
                    .setNumber(SteamGiftsUserData.getCurrent(context).getMessageNotification())
                    .setContentIntent(getViewMessagesIntent())
                    .setDeleteIntent(getDeleteIntent())
                    .setAutoCancel(true)
                    .build();

            showNotification(notification);
        }

        /**
         * Returns the comment's content, and, optionally, the author's name
         *
         * @param comment     comment to display the content of
         * @param includeName whether or not to include the author's name
         * @return text to display in the notification
         */
        @NonNull
        private CharSequence formatComment(Comment comment, boolean includeName) {
            String content = StringUtils.fromHtml(context, comment.getContent()).toString();
            if (TextUtils.isEmpty(content) && comment.getAttachedImages() != null && comment.getAttachedImages().size() > 0) {
                content = context.getString(R.string.notification_has_attached_image);
            }

            if (includeName && comment.getAuthor() != null) {
                SpannableString sb = new SpannableString(String.format("%s  %s", comment.getAuthor(), content));
                sb.setSpan(new StyleSpan(Typeface.BOLD), 0, comment.getAuthor().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                return sb;
            } else {
                return content;
            }
        }

        /**
         * Show the built notification in the systray.
         *
         * @param notification notification to display
         */
        private void showNotification(Notification notification) {
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notification);
        }

        /**
         * Return an intent for dismissing all messages, i.e. not showing them in future anymore.
         *
         * @return intent for dismissing all messages.
         */
        private PendingIntent getDeleteIntent() {
            if (TextUtils.isEmpty(lastCommentId))
                Log.w(TAG, "Calling getDeleteIntent without a comment id");

            Intent intent = new Intent(context, CheckForNewMessages.class);
            intent.setAction(ACTION_DELETE);
            intent.putExtra(EXTRA_COMMENT_ID, lastCommentId);

            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        /**
         * Return an intent for viewing all messages.
         *
         * @return intent for viewing all messages
         */
        private PendingIntent getViewMessagesIntent() {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra(DetailActivity.ARG_NOTIFICATIONS, true);

            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        /**
         * Return an intent for viewing a single message.
         *
         * @param comment the comment to view
         * @return intent for viewing a single message
         */
        private PendingIntent getViewMessageIntent(Comment comment) {
            Intent intent = UrlHandlingActivity.getPermalinkUri(context, comment);
            intent.putExtra(DetailActivity.ARG_MARK_CONTEXT_READ, true);

            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }
    }
}
