package net.mabako.steamgifts.data;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;

import java.io.Serializable;

/**
 * Comment on a giveaway or discussion.
 */
public class Comment implements Serializable, IEndlessAdaptable {
    public static final int VIEW_LAYOUT = R.layout.comment;

    private final int id;
    private final String author;
    private final String timeAgo;
    private final String timeAgoLong;
    private final String content;
    private final String avatar;
    private final int depth;
    private final boolean op;

    private boolean deleted;

    public Comment(int id, String author, String timeAgo, String timeAgoLong, String content, int depth, String avatar, boolean isOp) {
        this.id = id;
        this.author = author;
        this.timeAgo = timeAgo;
        this.timeAgoLong = timeAgoLong;
        this.content = content;
        this.depth = depth;
        this.avatar = avatar;
        this.op = isOp;
    }

    public int getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public String getTimeAgoLong() {
        return timeAgoLong;
    }

    public String getContent() {
        return content;
    }

    public int getDepth() {
        return depth;
    }

    public String getAvatar() {
        return avatar;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isOp() {
        return op;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Comment))
            return false;

        return ((Comment) o).id == id;
    }

    @Override
    public int getLayout() {
        return VIEW_LAYOUT;
    }
}
