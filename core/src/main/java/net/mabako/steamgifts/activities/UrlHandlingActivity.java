package net.mabako.steamgifts.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import net.mabako.sgtools.SGToolsDetailFragment;
import net.mabako.steamgifts.data.BasicDiscussion;
import net.mabako.steamgifts.data.BasicGiveaway;
import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.fragments.DetailFragment;
import net.mabako.steamgifts.fragments.DiscussionDetailFragment;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;
import net.mabako.steamgifts.fragments.UserDetailFragment;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Handle all URL related shenanigans.
 */
public class UrlHandlingActivity extends CommonActivity {
    private static final String TAG = UrlHandlingActivity.class.getSimpleName();

    private static final Pattern
            youtubePattern = Pattern.compile("^https?://[\\.\\w]*youtube\\.\\w+/.*"),
            youtu_bePattern = Pattern.compile("^https?://[\\.\\w]*youtu\\.be/([A-Za-z0-9\\-_]+)(\\?.*|).*");

    @Nullable
    public static Intent getIntentForUri(@NonNull Context context, @NonNull Uri uri) {
        Log.v(TAG, uri.toString());
        List<String> pathSegments = uri.getPathSegments();
        if ("www.steamgifts.com".equals(uri.getHost()) || "steamgifts.com".equals(uri.getHost())) {
            Log.v(TAG, "Parsing path segment " + uri.getPath());

            if (pathSegments.size() == 0 || ("/giveaways/search".equals(uri.getPath()))) {
                // TODO parse query params?
                return new Intent(context, MainActivity.class);
            } else if (pathSegments.size() >= 2) {
                // Can't really do anything reasonable without at least two path segments
                // Giveaways!
                if ("giveaway".equals(pathSegments.get(0))) {
                    String giveawayId = pathSegments.get(1);
                    // Giveaway Ids are always 5 chars long.
                    if (giveawayId.length() == 5) {
                        Intent intent = new Intent(context, DetailActivity.class);
                        intent.putExtra(GiveawayDetailFragment.ARG_GIVEAWAY, new BasicGiveaway(giveawayId));
                        intent.putExtra(DetailFragment.ARG_COMMENT_CONTEXT, DetailFragment.CommentContextInfo.fromUri(uri));
                        return intent;
                    }
                } else if ("discussion".equals(pathSegments.get(0))) {
                    String discussionId = pathSegments.get(1);
                    // Discussion Ids are always 5 chars long.
                    if (discussionId.length() == 5) {
                        Intent intent = new Intent(context, DetailActivity.class);
                        intent.putExtra(DiscussionDetailFragment.ARG_DISCUSSION, new BasicDiscussion(discussionId));
                        intent.putExtra(DetailFragment.ARG_COMMENT_CONTEXT, DetailFragment.CommentContextInfo.fromUri(uri));
                        return intent;
                    }
                } else if ("user".equals(pathSegments.get(0))) {
                    String user = pathSegments.get(1);
                    if("id".equals(user)) {
                        // Follow a redirect from /user/id/012435
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra(WebViewActivity.ARG_URL, uri.toString());
                        return intent;
                    } else {
                        Intent intent = new Intent(context, DetailActivity.class);
                        intent.putExtra(UserDetailFragment.ARG_USER, user);
                        return intent;
                    }
                } else if ("go".equals(pathSegments.get(0)) && "comment".equals(pathSegments.get(1)) && pathSegments.size() == 3) {
                    Intent intent = new Intent(context, WebViewActivity.class);
                    intent.putExtra(WebViewActivity.ARG_URL, uri.toString());
                    return intent;
                }
            }
        } else if ("www.sgtools.info".equals(uri.getHost()) || "sgtools.info".equals(uri.getHost())) {
            if (pathSegments.size() >= 2) {
                if ("giveaways".equals(pathSegments.get(0))) {
                    try {
                        UUID uuid = UUID.fromString(pathSegments.get(1));

                        Intent intent = new Intent(context, DetailActivity.class);
                        intent.putExtra(SGToolsDetailFragment.ARG_UUID, uuid);
                        return intent;

                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                }
            }
        } else {
            if (youtubePattern.matcher(uri.toString()).matches() || youtu_bePattern.matcher(uri.toString()).matches()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(uri);
                return intent;
            }
        }
        return null;
    }

    @NonNull
    public static IntentDelegate getIntentForUri(@NonNull Context context, @NonNull Uri uri, boolean returnWebIntentIfNoneMatching) {
        return getIntentForUri(context, uri, returnWebIntentIfNoneMatching, false);
    }

    @NonNull
    public static IntentDelegate getIntentForUri(@NonNull final Context context, @NonNull Uri uri, boolean returnWebIntentIfNoneMatching, boolean noBackStack) {
        Intent intent = getIntentForUri(context, uri);

        if (intent == null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            String type = sp.getString("preference_external_browser", "default");
            if ("default".equals(type))
                type = ChromeTabsDelegate.isCustomTabsSupported(context) ? "chrome-tab" : "webview";

            if ("external".equals(type)) {
                return new RealIntentDelegate(new Intent(Intent.ACTION_VIEW, uri));
            } else if ("chrome-tab".equals(type) && ChromeTabsDelegate.isCustomTabsSupported(context)) {
                return new ChromeTabsDelegate(uri);
            } else if ("webview".equals(type)) {
                // normal webview.
                intent = new Intent(context, WebViewActivity.class);
                intent.putExtra(WebViewActivity.ARG_URL, uri.toString());

                if (noBackStack)
                    intent.putExtra(WebViewActivity.ARG_NO_BACK_STACK, true);

                return new RealIntentDelegate(intent);
            } else
                throw new IllegalStateException("Unknown preference_external_browser=" + type);
        } else {
            return new RealIntentDelegate(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start whatever intent we were launching
        Intent intentToStart = getIntentForUri(this, getIntent().getData());
        if (intentToStart != null) {
            startActivity(intentToStart);
        } else {
            // Fallback for opening an unknown url.
            startActivity(new Intent(this, MainActivity.class));
        }

        finish();
    }

    /**
     * Get the Intent for opening a comment permalink.
     *
     * @param comment the comment to open
     */
    public static Intent getPermalinkUri(@NonNull Context context, @NonNull Comment comment) {
        return getIntentForUri(context, Uri.parse("https://www.steamgifts.com/" + comment.getLink()));
    }

    public interface IntentDelegate {
        void start(@NonNull Activity activity);
    }

    private static class RealIntentDelegate implements IntentDelegate {
        @NonNull
        private final Intent intent;

        public RealIntentDelegate(@NonNull Intent intent) {
            this.intent = intent;
        }

        @Override
        public void start(@NonNull Activity activity) {
            activity.startActivityForResult(intent, MainActivity.REQUEST_LOGIN_PASSIVE);
        }
    }
}
