package net.mabako.steamgifts.fragments.util;

import net.mabako.steamgifts.fragments.GiveawayListFragment;

import java.util.ArrayList;
import java.util.List;

public final class GiveawayListFragmentStack {
    private static List<GiveawayListFragment> fragments = new ArrayList<>();

    public static void addFragment(GiveawayListFragment fragment) {
        fragments.add(fragment);
    }

    public static void removeFragment(GiveawayListFragment fragment) {
        fragments.remove(fragment);
    }

    public static void onHideGame(int internalGameId) {
        for (GiveawayListFragment fragment : fragments)
            fragment.onHideGame(internalGameId, false);
    }

    public static void onEnterLeaveResult(String giveawayId, String what, Boolean success) {
        for (GiveawayListFragment fragment : fragments)
            fragment.onEnterLeaveResult(giveawayId, what, success, false);
    }
}
