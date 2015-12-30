package net.mabako.steamgifts.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GiveawayExtras implements Serializable, ICommentHolder {
    private String description;
    private String xsrfToken;
    private boolean entered;
    private String timeRemaining;
    private List<Comment> loadedComments;

    public GiveawayExtras() {
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

    public boolean isEntered() {
        return entered;
    }

    public void setEntered(boolean entered) {
        this.entered = entered;
    }

    public String getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(String timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    @Override
    public Comment getComment(int position) {
        return loadedComments.get(position);
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
