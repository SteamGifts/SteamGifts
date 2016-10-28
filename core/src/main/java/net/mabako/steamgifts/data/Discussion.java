package net.mabako.steamgifts.data;

import android.content.Context;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;

import java.util.Calendar;

public class Discussion extends BasicDiscussion implements IEndlessAdaptable {
    public static final int VIEW_LAYOUT = R.layout.discussion_item;

    private String title;
    private String name;
    private String creator;
    private CustomDateTime createdTime;
    private String creatorAvatar;

    private boolean locked, poll;

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

    public Calendar getCreatedTime() {
        return createdTime != null ? createdTime.getCalendar() : null;
    }

    public String getRelativeCreatedTime(Context context) {
        return createdTime != null ? createdTime.toString(context) : null;
    }

    public void setCreatedTime(int timestamp) {
        createdTime = new CustomDateTime(timestamp, false);
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

    public boolean isPoll() {
        return poll;
    }

    public void setPoll(boolean poll) {
        this.poll = poll;
    }

    @Override
    public int getLayout() {
        return VIEW_LAYOUT;
    }
}
