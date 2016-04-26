package net.mabako.steamgifts.receivers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.fragments.interfaces.ILoadItemsListener;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;
import net.mabako.steamgifts.tasks.LoadWonGameListTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Check for won newly won giveaways. This is, eventually, only triggered if {@link CheckForNewMessages} detects any won giveaways.
 */
public class CheckForWonGiveaways extends AbstractNotificationCheckReceiver {
    private static NotificationId NOTIFICATION_ID = NotificationId.WON;

    private static final String PREF_KEY_LAST_SHOWN_WON_GAME = "last-shown-won-game";
    private static final String PREF_KEY_LAST_DISMISSED_WON_GAMES = "last-dismissed-won-game";

    private static final String ACTION_DELETE = "delete";
    private static final String EXTRA_GIVEAWAY_IDS = "giveaway-ids";

    private static final String TAG = CheckForWonGiveaways.class.getSimpleName();

    /**
     * Number of Giveaways we display at most.
     */
    private static final int MAX_DISPLAYED_GIVEAWAYS = 5;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            Log.v(TAG, "Checking for newly won giveaways...");
            if (shouldRunNetworkTask(TAG, context)) {
                new LoadWonGameListTask(new Check(context), context, 1).execute();
            }
        } else if (ACTION_DELETE.equals(action)) {
            // If we explicitly dismiss this notification, we want to stop this message from re-appearing ever.
            List<String> lastDismissedId = intent.getStringArrayListExtra(EXTRA_GIVEAWAY_IDS);
            if (lastDismissedId == null || lastDismissedId.isEmpty()) {
                Log.w(TAG, "Trying to execute " + ACTION_DELETE + " without a single giveaway id");
                return;
            }

            setLastDismissedGiveawayIds(context, lastDismissedId);
        } else {
            Log.w(TAG, "Trying to execute action " + action);
        }
    }

    public static void setLastDismissedGiveawayIds(@NonNull Context context, @NonNull List<String> giveawayIds) {
        Log.v(TAG, "Marking won games as dismissed");
        context.getSharedPreferences(PREFS_NOTIFICATIONS_SERVICE, Context.MODE_PRIVATE).edit().putStringSet(PREF_KEY_LAST_DISMISSED_WON_GAMES, new HashSet<>(giveawayIds)).apply();
    }

    private static class Check implements ILoadItemsListener {
        private final Context context;
        private ArrayList<String> lastGiveawayIds;

        public Check(Context context) {
            this.context = context;
        }

        @Override
        public void addItems(List<? extends IEndlessAdaptable> items, boolean clearExistingItems, String xsrfToken) {
            if (items == null || items.size() == 0) {
                Log.d(TAG, "got no won games -at all-");
                return;
            }

            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NOTIFICATIONS_SERVICE, Context.MODE_PRIVATE);

            // Interestingly enough, the question with re-rolls etc is most likely that we can't reliably tell these giveaways will arrive in order.
            Set<String> knownWonGames = sharedPreferences.getStringSet(PREF_KEY_LAST_DISMISSED_WON_GAMES, new HashSet<String>());

            List<Giveaway> mostRecentWonGames = new ArrayList<>(MAX_DISPLAYED_GIVEAWAYS);
            for (IEndlessAdaptable item : items) {
                if (item instanceof Giveaway) {
                    Giveaway giveaway = (Giveaway) item;

                    // If we marked this giveaway as received yet, we don't want to show it.
                    if (!giveaway.isEntered())
                        continue;

                    if (knownWonGames.contains(giveaway.getGiveawayId()))
                        continue;

                    mostRecentWonGames.add(giveaway);
                    if (knownWonGames.size() == MAX_DISPLAYED_NOTIFICATIONS)
                        break;
                }
            }

            if (mostRecentWonGames.isEmpty()) {
                Log.v(TAG, "Got no new won games, or we've dismissed the last giveaways we could see");
            } else {
                String lastShownId = sharedPreferences.getString(PREF_KEY_LAST_SHOWN_WON_GAME, null);

                // While this same comment may appear in a later notification, we're establishing that it may not be the first comment again.

                // This is simply so you're not being notified about the same message(s) over and over again, and if it's still unread when a new
                // message is pushed, we may show it again.

                // Contrary, dismissing a message will never show it to you again. "Ignoring" a message here will simply show it stacked
                // with other new messages in the future.
                Giveaway firstGiveaway = mostRecentWonGames.get(0);
                if (lastShownId != null && lastShownId.equals(firstGiveaway.getGiveawayId())) {
                    // We've shown a notification for the very same comment before, so do nothing...
                    Log.d(TAG, "Most recent won game has the same id as the last shown won game");
                    return;
                }

                lastGiveawayIds = new ArrayList<>();
                for (IEndlessAdaptable item : items) {
                    if (item instanceof Giveaway)
                        lastGiveawayIds.add(((Giveaway) item).getGiveawayId());
                }

                // Save the last comment id
                sharedPreferences.edit().putString(PREF_KEY_LAST_SHOWN_WON_GAME, firstGiveaway.getGiveawayId()).apply();

                // Do we show a single (expanded) content or a bunch of comments?
                if (mostRecentWonGames.size() == 1) {
                    showNotification(context, NOTIFICATION_ID, R.drawable.ic_gift, String.format(context.getString(R.string.notification_won_game), mostRecentWonGames.get(0).getTitle()), mostRecentWonGames.get(0).getRelativeEndTime(context), getViewIntent(), getDeleteIntent());
                } else {
                    List<CharSequence> texts = new ArrayList<>(mostRecentWonGames.size());
                    for (Giveaway giveaway : mostRecentWonGames)
                        texts.add(giveaway.getTitle());

                    showNotification(context, NOTIFICATION_ID, R.drawable.ic_gift, String.format(context.getString(R.string.notification_won_games), SteamGiftsUserData.getCurrent(context).getWonNotification()), texts, getViewIntent(), getDeleteIntent());

                }

                Log.d(TAG, "Shown " + mostRecentWonGames.size() + " won games as notification");
            }
        }

        /**
         * Return an intent for viewing all won games.
         *
         * @return intent for viewing all won games
         */
        private PendingIntent getViewIntent() {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra(DetailActivity.ARG_NOTIFICATIONS, NOTIFICATION_ID);

            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        /**
         * Return an intent for dismissing all won games, i.e. not showing them in future anymore.
         *
         * @return intent for dismissing all won games.
         */
        private PendingIntent getDeleteIntent() {
            if (lastGiveawayIds == null)
                Log.w(TAG, "Calling getDeleteIntent without giveaway ids");

            Intent intent = new Intent(context, CheckForNewMessages.class);
            intent.setAction(ACTION_DELETE);
            intent.putStringArrayListExtra(EXTRA_GIVEAWAY_IDS, lastGiveawayIds);

            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }
    }
}
