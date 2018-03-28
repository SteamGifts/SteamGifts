package net.mabako.steamgifts.persistentdata;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import net.mabako.steamgifts.tasks.Utils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class SteamGiftsUserData {
    private static final String TAG = SteamGiftsUserData.class.getSimpleName();

    private static final String PREF_KEY_SESSION_ID = "session-id";
    private static final String PREF_ACCOUNT = "account";
    private static final String PREF_KEY_USERNAME = "username";
    private static final String PREF_KEY_IMAGE = "image-url";

    private static SteamGiftsUserData current;
    private String sessionId;
    private String name;
    private String imageUrl;

    private transient int points = 0;
    private transient int level = 0;

    private transient int createdNotification, wonNotification, messageNotification;

    private static List<IPointUpdateNotification> pointUpdateHandlers = new ArrayList<>();

    public static void addUpdateHandler(IPointUpdateNotification handler) {
        pointUpdateHandlers.add(handler);
    }

    public static void removeUpdateHandler(IPointUpdateNotification handler) {
        pointUpdateHandlers.remove(handler);
    }

    public static synchronized SteamGiftsUserData getCurrent(@Nullable Context context) {
        if (current == null) {
            if (context == null) {
                // do not keep the data, though, so anything here is pretty much a no-op
                Log.w(TAG, "Instantiiating no-op SteamGiftsUserData");
                return new SteamGiftsUserData();
            }
            current = new SteamGiftsUserData();

            // Load session & username if possible
            SharedPreferences sp = context.getSharedPreferences(PREF_ACCOUNT, Context.MODE_PRIVATE);
            if (sp.contains(PREF_KEY_SESSION_ID) && sp.contains(PREF_KEY_USERNAME) && sp.getString(PREF_KEY_SESSION_ID, null) != null) {
                current.setSessionId(sp.getString(PREF_KEY_SESSION_ID, null));
                current.setName(sp.getString(PREF_KEY_USERNAME, null));
                current.setImageUrl(sp.getString(PREF_KEY_IMAGE, null));
            } else {
                SteamGiftsUserData.clear();
            }
        }
        return current;
    }

    public static void extract(@Nullable Context context, @Nullable Document document) {
        if (getCurrent(context) == null)
            return;

        if (document == null)
            return;

        Elements navbar = document.select(".nav__button-container");

        Element userContainer = navbar.last().select("a").first();
        String link = userContainer.attr("href");

        if (link.startsWith("/user/")) {
            current.setName(link.substring(6));

            // fetch the image
            String style = userContainer.select("div").first().attr("style");
            style = Utils.extractAvatar(style);
            current.setImageUrl(style);

            // points
            Element accountContainer = navbar.select("a[href=/account]").first();
            current.setPoints(Utils.parseInt(accountContainer.select(".nav__points").text()));

            // Level
            float level = Float.parseFloat(accountContainer.select("span").last().attr("title"));
            current.setLevel((int) level);

            // Notifications
            Elements notifications = navbar.select(".nav__button-container--notification");
            current.setCreatedNotification(getInt(notifications.select("a[href=/giveaways/created]").first().text()));
            current.setWonNotification(getInt(notifications.select("a[href=/giveaways/won]").first().text()));
            current.setMessageNotification(getInt(notifications.select("a[href=/messages]").first().text()));
        } else if (link.startsWith("/?login") && current.isLoggedIn()) {
            current = new SteamGiftsUserData();
            if (context != null)
                current.save(context);
        }
    }

    private static int getInt(String text) {
        text = text.trim();
        if (TextUtils.isEmpty(text))
            return 0;
        return Utils.parseInt(text.replace("+", ""));
    }

    public static void clear() {
        current = new SteamGiftsUserData();
    }

    public boolean isLoggedIn() {
        return sessionId != null && !sessionId.isEmpty();
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(final int points) {
        this.points = points;
        for (final IPointUpdateNotification handler : pointUpdateHandlers) {
            if (handler instanceof Activity) {
                ((Activity) handler).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handler.onUpdatePoints(points);
                    }
                });
            } else {
                handler.onUpdatePoints(points);
            }
        }

    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCreatedNotification() {
        return createdNotification;
    }

    public void setCreatedNotification(int createdNotification) {
        this.createdNotification = createdNotification;
    }

    public int getMessageNotification() {
        return messageNotification;
    }

    public void setMessageNotification(int messageNotification) {
        this.messageNotification = messageNotification;
    }

    public int getWonNotification() {
        return wonNotification;
    }

    public void setWonNotification(int wonNotification) {
        this.wonNotification = wonNotification;
    }

    public boolean hasNotifications() {
        return createdNotification != 0 || messageNotification != 0 || wonNotification != 0;
    }

    public void save(@NonNull Context context) {
        SharedPreferences.Editor spEditor = context.getSharedPreferences(PREF_ACCOUNT, Context.MODE_PRIVATE).edit();

        if (isLoggedIn()) {
            spEditor.putString(PREF_KEY_SESSION_ID, sessionId);
            spEditor.putString(PREF_KEY_USERNAME, name);
            spEditor.putString(PREF_KEY_IMAGE, imageUrl);
        } else {
            spEditor.remove(PREF_KEY_SESSION_ID);
            spEditor.remove(PREF_KEY_USERNAME);
            spEditor.remove(PREF_KEY_IMAGE);
        }
        spEditor.apply();

    }
}
