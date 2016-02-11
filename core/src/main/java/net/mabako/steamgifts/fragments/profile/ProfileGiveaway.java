package net.mabako.steamgifts.fragments.profile;

import android.content.Context;

import net.mabako.steamgifts.data.Giveaway;

/**
 * It is presumably interesting to note that albeit this extends the 'Giveaway' class, not all of
 * its properties are likely to be set. For example, you can neither hide nor unhide any such
 * giveaway, as there's no SG game id (different from the store app ids).
 */
public class ProfileGiveaway extends Giveaway {
    private static final long serialVersionUID = 5980737736788961021L;
    private boolean deleted;

    public ProfileGiveaway(String giveawayId) {
        super(giveawayId);
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public boolean isOpen() {
        return !deleted && super.isOpen();
    }

    @Override
    public boolean isEntered() {
        return !deleted && super.isEntered();
    }

    @Override
    public String getRelativeEndTime(Context context) {
        if (deleted)
            return "Deleted";
        return super.getRelativeEndTime(context);
    }
}
