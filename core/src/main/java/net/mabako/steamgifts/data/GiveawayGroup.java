package net.mabako.steamgifts.data;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;

import java.io.Serializable;

public class GiveawayGroup implements IEndlessAdaptable, Serializable {
    public static final int VIEW_LAYOUT = R.layout.giveaway_group_item;
    private static final long serialVersionUID = 6889558816716859611L;

    private final String id;
    private final String title;
    private final String avatar;

    public GiveawayGroup(String id, String title, String avatar) {
        this.id = id;
        this.title = title;
        this.avatar = avatar;
    }

    public String getTitle() {
        return title;
    }

    public String getAvatar() {
        return avatar;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof GiveawayGroup))
            return false;
        return ((GiveawayGroup) o).id == id;
    }

    @Override
    public int getLayout() {
        return VIEW_LAYOUT;
    }
}
