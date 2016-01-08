package net.mabako.steamgifts.data;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;

public class Discussion extends BasicDiscussion implements IEndlessAdaptable {
    public static final int VIEW_LAYOUT = R.layout.discussion_item;

    private String title;
    private String name;
    private String creator;
    private String timeCreated;
    private String creatorAvatar;

    private boolean locked;

    public Discussion(String discussionId) {
        super(discussionId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getCreatorAvatar() {
        return creatorAvatar;
    }

    public void setCreatorAvatar(String creatorAvatar) {
        this.creatorAvatar = creatorAvatar;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public int getLayout() {
        return VIEW_LAYOUT;
    }
}
