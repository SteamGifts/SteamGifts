package net.mabako.steamgifts.fragments.util;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.data.GiveawayExtras;

import java.io.Serializable;

public class GiveawayDetailsCard implements IEndlessAdaptable, Serializable {
    private static final long serialVersionUID = -2605313593624182118L;
    public static final int VIEW_LAYOUT = R.layout.giveaway_detail_card;

    private Giveaway giveaway;
    private GiveawayExtras extras;

    @Override
    public int getLayout() {
        return VIEW_LAYOUT;
    }

    public Giveaway getGiveaway() {
        return giveaway;
    }

    public void setGiveaway(Giveaway giveaway) {
        this.giveaway = giveaway;
    }

    public GiveawayExtras getExtras() {
        return extras;
    }

    public void setExtras(GiveawayExtras extras) {
        this.extras = extras;
    }
}
