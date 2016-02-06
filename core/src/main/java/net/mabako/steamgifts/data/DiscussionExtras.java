package net.mabako.steamgifts.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DiscussionExtras implements Serializable, ICommentHolder, IImageHolder {
    private static final long serialVersionUID = 5303211073144498554L;
    private String description;
    private String xsrfToken;
    private final List<Comment> loadedComments;
    private Poll poll;

    private List<Image> attachedImages;

    public DiscussionExtras() {
        loadedComments = new ArrayList<>();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getXsrfToken() {
        return xsrfToken;
    }

    public void setXsrfToken(String xsrfToken) {
        this.xsrfToken = xsrfToken;
    }

    @Override
    public List<Comment> getComments() {
        return loadedComments;
    }

    @Override
    public void addComment(Comment comment) {
        loadedComments.add(comment);
    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    public boolean hasPoll() {
        return poll != null;
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
}
