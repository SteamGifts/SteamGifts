package net.mabako.steamgifts.data;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;

import java.io.Serializable;

public class Discussion implements Serializable, IEndlessAdaptable {
    public static final int VIEW_LAYOUT = R.layout.discussion_item;

    private final String discussionId;
    private final String title;
    private final String name;
    private final String creator;
    private final String timeCreated;
    private final String creatorAvatar;

    private boolean locked;

    public Discussion(String discussionId, String title, String name, String creator, String timeCreated, String avatar) {
        this.discussionId = discussionId;
        this.title = title;
        this.name = name;
        this.creator = creator;
        this.timeCreated = timeCreated;
        this.creatorAvatar = avatar;
    }

    public String getDiscussionId() {
        return discussionId;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getCreator() {
        return creator;
    }

    public String getCreatorAvatar() {
        return creatorAvatar;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
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
