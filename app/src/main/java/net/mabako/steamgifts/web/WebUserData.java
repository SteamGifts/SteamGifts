package net.mabako.steamgifts.web;

import android.util.Log;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.Serializable;

public class WebUserData implements Serializable {
    private static final String TAG = WebUserData.class.getSimpleName();
    private static WebUserData current = new WebUserData();
    private String sessionId;
    private String name;

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
            current.name = link.substring(6);
            Log.v(TAG, "Setting current user name to " + current.name);
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
}
