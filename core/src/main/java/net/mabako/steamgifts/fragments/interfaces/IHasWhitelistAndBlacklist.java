package net.mabako.steamgifts.fragments.interfaces;

import net.mabako.steamgifts.data.BasicUser;

public interface IHasWhitelistAndBlacklist {
    enum What {
        WHITELIST,
        BLACKLIST
    }

    public void requestUserListed(BasicUser user, What what, boolean adding);

    public void onUserWhitelistOrBlacklistUpdated(BasicUser user, What what, boolean added);
}
