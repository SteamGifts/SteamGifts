package net.mabako.steamgifts.fragments.interfaces;

public interface IHasHideableGiveaways {
    void onHideGame(long internalGameId, boolean propagate, String gameTitle);
}
