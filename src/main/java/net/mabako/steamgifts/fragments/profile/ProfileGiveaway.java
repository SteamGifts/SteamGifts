package net.mabako.steamgifts.fragments.profile;

import net.mabako.steamgifts.data.Giveaway;

/**
 * It is presumably interesting to note that albeit this extends the 'Giveaway' class, not all of
 * its properties are likely to be set. For example, you can neither hide nor unhide any such
 * giveaway, as there's no SG game id (different from the store app ids).
 */
public class ProfileGiveaway extends Giveaway {
    private boolean deleted;

    public ProfileGiveaway(String giveawayId) {
        super(giveawayId);
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
        if (deleted)
            setTimeRemaining("Deleted");
    }

    @Override
    public boolean isOpen() {
        return deleted ? false : super.isOpen();
    }

    @Override
    public boolean isEntered() {
        return deleted ? false : super.isEntered();
    }
}
