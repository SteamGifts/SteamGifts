package net.mabako;

import android.os.Build;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface Constants {
    /**
     * Important user roles we want to display. Does not include "Member", "Bundler" or "Developer".
     */
    List<String> IMPORTANT_USER_ROLES = Collections.unmodifiableList(Arrays.asList("Admin", "Super Mod", "Moderator", "Support"));

    /**
     * User agent to be used for Jsoup connections, which is a generic Chrome build.
     */
    String JSOUP_USER_AGENT = "Mozilla/5.0 (Linux; Android " + Build.VERSION.RELEASE + ") AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.0 Safari/537.36";

    /**
     * <p>Timeout to be used for Jsoup connections in milliseconds.</p>
     * <p>The default is 3 seconds.</p>
     */
    int JSOUP_TIMEOUT = 10000;
}
