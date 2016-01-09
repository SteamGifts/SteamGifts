package net.mabako.steamgifts.web;

import android.app.Activity;
import android.util.Log;

import net.mabako.steamgifts.tasks.Utils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class SteamGiftsUserData {
    private static final String TAG = SteamGiftsUserData.class.getSimpleName();
    private static SteamGiftsUserData current = new SteamGiftsUserData();
    private String sessionId;
    private String name;
    private String imageUrl;

    private transient int points = 0;
    private transient int level = 0;

    private static List<IPointUpdateNotification> pointUpdateHandlers = new ArrayList<>();

    public static void addUpdateHandler(IPointUpdateNotification handler) {
        pointUpdateHandlers.add(handler);
    }

    public static void removeUpdateHandler(IPointUpdateNotification handler) {
        pointUpdateHandlers.remove(handler);
    }

    public static SteamGiftsUserData getCurrent() {
        return current;
    }

    public static void extract(Document document) {
        Log.d(TAG, "Parsing user data...");

        Elements navbar = document.select(".nav__button-container");

        Element userContainer = navbar.last().select("a").first();
        String link = userContainer.attr("href");
        Log.d(TAG, "Link to profile: " + link);

        if (link.startsWith("/user/")) {
            current.setName(link.substring(6));

            // fetch the image
            String style = userContainer.select("div").first().attr("style");
            style = Utils.extractAvatar(style);
            Log.v(TAG, "User Avatar: " + style);
            current.setImageUrl(style);

            // points
            Element accountContainer = navbar.select("a[href=/account]").first();
            current.setPoints(Integer.parseInt(accountContainer.select(".nav__points").text()));

            // Level
            float level = Float.parseFloat(accountContainer.select("span").last().attr("title"));
            current.setLevel((int) level);
        }
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
        Log.v(TAG, "Setting current user name to " + name);
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
}
