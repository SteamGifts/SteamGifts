package net.mabako.steamgifts.fragments;

/**
 * Update notification for giveaway "entered" status changing.
 */
public interface IGiveawayUpdateNotification {
    void onUpdateGiveawayStatus(String giveawayId, boolean entered);
}
