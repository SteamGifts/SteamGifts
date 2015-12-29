package net.mabako.steamgifts.web;

import android.content.SharedPreferences;
import android.util.Log;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.Serializable;

public class WebUserData {
    private static final String TAG = WebUserData.class.getSimpleName();
    private static WebUserData current = new WebUserData();
    private String sessionId;
    private String name;
    private String imageUrl;

    private transient int points = 0;
    private transient int level = 0;

    public static WebUserData getCurrent() {
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
            style = style.replace("background-image:url(", "").replace(");", "").replace("_medium", "_full");
            Log.v(TAG, "User Avatar: " + style);
            current.setImageUrl(style);
        }
    }

    public static void clear() {
        current = new WebUserData();
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
        Log.v(TAG, "Setting current user name to " + current.name);
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
