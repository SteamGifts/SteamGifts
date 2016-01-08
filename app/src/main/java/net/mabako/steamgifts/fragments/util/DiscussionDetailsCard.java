package net.mabako.steamgifts.fragments.util;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.data.Discussion;
import net.mabako.steamgifts.data.DiscussionExtras;

public class DiscussionDetailsCard implements IEndlessAdaptable {
    public static final int VIEW_LAYOUT = R.layout.discussion_detail_card;

    private Discussion discussion;
    private DiscussionExtras extras;

    @Override
    public int getLayout() {
        return VIEW_LAYOUT;
    }

    public Discussion getDiscussion() {
        return discussion;
    }

    public void setDiscussion(Discussion discussion) {
        this.discussion = discussion;
    }

    public DiscussionExtras getExtras() {
        return extras;
    }

    public void setExtras(DiscussionExtras extras) {
        this.extras = extras;
    }
}
