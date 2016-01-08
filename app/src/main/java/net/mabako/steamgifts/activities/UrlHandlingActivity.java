package net.mabako.steamgifts.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import net.mabako.steamgifts.data.BasicGiveaway;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;

import java.util.List;

/**
 * Handle all URL related shenanigans.
 */
public class UrlHandlingActivity extends CommonActivity {
    private static final String TAG = UrlHandlingActivity.class.getSimpleName();

    public static Intent getIntentForUri(Context context, Uri uri) {
        Log.d(TAG, uri.toString());
        if ("www.steamgifts.com".equals(uri.getHost()) || "steamgifts.com".equals(uri.getHost())) {
            List<String> pathSegments = uri.getPathSegments();

            // Can't really do anything reasonable without at least two path segments
            if (pathSegments.size() >= 2) {
                // Giveaways!
                if ("giveaway".equals(pathSegments.get(0))) {
                    String giveawayId = pathSegments.get(1);
                    // Giveaway Ids are always 5 chars long.
                    if (giveawayId.length() == 5) {
                        Intent intent = new Intent(context, DetailActivity.class);
                        intent.putExtra(GiveawayDetailFragment.ARG_GIVEAWAY, new BasicGiveaway(giveawayId));
                        return intent;
                    }
                }
            }
        }
        return null;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start whatever intent we were launching
        Intent intentToStart = getIntentForUri(this, getIntent().getData());
        if (intentToStart != null) {
            startActivity(intentToStart);
        } else {
            // TODO launch in browser or something
        }

        finish();
    }
}
