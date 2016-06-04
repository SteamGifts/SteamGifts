package net.mabako.steamgifts.fragments.util;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Discussion;
import net.mabako.steamgifts.data.DiscussionExtras;

import java.io.Serializable;

public class DiscussionDetailsCard implements IEndlessAdaptable, Serializable {
    private static final long serialVersionUID = -6316766705848863209L;
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
