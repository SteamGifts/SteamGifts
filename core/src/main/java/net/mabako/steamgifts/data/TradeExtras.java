package net.mabako.steamgifts.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TradeExtras implements Serializable, ICommentHolder, IImageHolder {
    private static final long serialVersionUID = -6192310508762956015L;
    private String description;
    private String xsrfToken;
    private final List<Comment> loadedComments;
    private Poll poll;

    private List<Image> attachedImages;

    public TradeExtras() {
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