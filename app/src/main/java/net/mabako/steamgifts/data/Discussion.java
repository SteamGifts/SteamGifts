package net.mabako.steamgifts.data;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;

import java.io.Serializable;

public class Discussion implements Serializable, IEndlessAdaptable {
    public static final int VIEW_LAYOUT = R.layout.discussion_item;

    private final String discussionId;
    private final String name;

    public Discussion(String discussionId, String name) {
        this.discussionId = discussionId;
        this.name = name;
    }

    public String getDiscussionId() {
        return discussionId;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Discussion))
            return false;

        return ((Discussion) o).discussionId == discussionId;
    }

    @Override
    public int getLayout() {
        return VIEW_LAYOUT;
    }
}
