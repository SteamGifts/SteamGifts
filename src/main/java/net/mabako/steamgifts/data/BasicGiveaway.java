package net.mabako.steamgifts.data;

import android.content.Context;
import android.net.Uri;

import java.io.Serializable;

/**
 * A giveaway that can easily be passed around. Keep in mind this isn't optimal for displaying a
 * giveaway per se, but loading giveaways from
 * {@link net.mabako.steamgifts.activities.UrlHandlingActivity#getIntentForUri(Context, Uri)} gives
 * us no real content outside of the Giveaway Id.
 */
public class BasicGiveaway implements Serializable {
    private final String giveawayId;

    public BasicGiveaway(String giveawayId) {
        this.giveawayId = giveawayId;
    }

    public String getGiveawayId() {
        return giveawayId;
    }

    @Override
    public int hashCode() {
        if (giveawayId == null)
            return 0;
        return giveawayId.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof BasicGiveaway))
            return false;

        if (giveawayId == null)
            // Implement this in Giveaway to compare more attributes, but there's no real way to have "unique" GAs.
            return false;
        else
            return giveawayId.equals(((BasicGiveaway) o).giveawayId);
    }
}
