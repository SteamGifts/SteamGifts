package net.mabako.steamgifts.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DiscussionExtras implements Serializable, ICommentHolder {
    private String description;
    private String xsrfToken;
    private final List<Comment> loadedComments;

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
}
