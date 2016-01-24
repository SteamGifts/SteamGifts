package net.mabako.steamgifts.fragments.util;

import android.support.v4.app.Fragment;

import net.mabako.steamgifts.fragments.GiveawayListFragment;
import net.mabako.steamgifts.fragments.SavedGiveawaysFragment;
import net.mabako.steamgifts.fragments.interfaces.IHasEnterableGiveaways;
import net.mabako.steamgifts.fragments.interfaces.IHasHideableGiveaways;

import java.util.ArrayList;
import java.util.List;

public final class GiveawayListFragmentStack {
    private static List<Fragment> fragments = new ArrayList<>();

    public static void addFragment(Fragment fragment) {
        if (!fragments.contains(fragment))
            fragments.add(fragment);
    }

    public static void removeFragment(Fragment fragment) {
        fragments.remove(fragment);
    }

    public static void onHideGame(int internalGameId) {
        for (Fragment fragment : fragments)
            if (fragment instanceof IHasHideableGiveaways)
                ((IHasHideableGiveaways) fragment).onHideGame(internalGameId, false, null);
    }

    public static void onShowGame(int internalGameId) {
        for (Fragment fragment : fragments)
            if (fragment instanceof GiveawayListFragment)
                ((GiveawayListFragment) fragment).onShowGame(internalGameId, false);
    }

    public static void onEnterLeaveResult(String giveawayId, String what, Boolean success) {
        for (Fragment fragment : fragments)
            if (fragment instanceof IHasEnterableGiveaways)
                ((IHasEnterableGiveaways) fragment).onEnterLeaveResult(giveawayId, what, success, false);
    }

    public static void onRemoveSavedGiveaway(String giveawayId) {
        for (Fragment fragment : fragments)
            if (fragment instanceof SavedGiveawaysFragment)
                ((SavedGiveawaysFragment) fragment).onRemoveSavedGiveaway(giveawayId);
    }
}
