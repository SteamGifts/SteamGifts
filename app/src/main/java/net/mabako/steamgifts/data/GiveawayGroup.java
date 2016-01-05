package net.mabako.steamgifts.data;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;

/**
 * Created by mabako on 05.01.2016.
 */
public class GiveawayGroup implements IEndlessAdaptable {
    public static final int VIEW_LAYOUT = R.layout.giveaway_group_item;

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
