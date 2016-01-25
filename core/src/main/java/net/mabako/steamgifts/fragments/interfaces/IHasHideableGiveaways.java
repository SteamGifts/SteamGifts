package net.mabako.steamgifts.fragments.interfaces;

public interface IHasHideableGiveaways {
    void onHideGame(int internalGameId, boolean propagate, String gameTitle);
}
