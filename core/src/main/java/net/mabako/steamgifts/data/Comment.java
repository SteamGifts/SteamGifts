package net.mabako.steamgifts.data;

import android.content.Context;

import net.mabako.Constants;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Comment on a giveaway or discussion.
 */
public class Comment implements Serializable, IEndlessAdaptable, IImageHolder {
    private static final long serialVersionUID = -7333245576601696951L;
    public static final int VIEW_LAYOUT = R.layout.comment;

    private final int id;
    private final String author;
    private CustomDateTime createdTime;
    private String content;
    private final String avatar;
    private int depth;
    private final boolean op;
    private String authorRole;

    private boolean deleted, highlighted;
    private String permalinkId, editableContent;

    private List<Image> attachedImages;

    public Comment(int id, String author, int depth, String avatar, boolean isOp) {
        this.id = id;
        this.author = author;
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

    public Calendar getCreatedTime() {
        return createdTime.getCalendar();
    }

    public String getRelativeCreatedTime(Context context) {
        return createdTime.toString(context);
    }

    public void setCreatedTime(String time) {
        createdTime = new CustomDateTime(time, false);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
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

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public boolean isOp() {
        return op;
    }

    public String getPermalinkId() {
        return permalinkId;
    }

    public void setPermalinkId(String permalinkId) {
        this.permalinkId = permalinkId;
    }

    public String getEditableContent() {
        return editableContent;
    }

    public void setEditableContent(String editableContent) {
        this.editableContent = editableContent;
    }

    public String getAuthorRole() {
        return authorRole;
    }

    public void setAuthorRole(String authorRole) {
        if (Constants.IMPORTANT_USER_ROLES.contains(authorRole))
            this.authorRole = authorRole;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Comment))
            return false;

        if (id == 0) {
            if (permalinkId != null && !"".equals(permalinkId))
                return permalinkId.equals(((Comment) o).permalinkId);
            else
                return false;
        }
        return ((Comment) o).id == id;
    }

    @Override
    public synchronized void attachImage(Image image) {
        if (attachedImages == null)
            attachedImages = new ArrayList<>();

        attachedImages.add(image);
    }

    @Override
    public List<Image> getAttachedImages() {
        return attachedImages;
    }

    @Override
    public int getLayout() {
        return VIEW_LAYOUT;
    }
}
