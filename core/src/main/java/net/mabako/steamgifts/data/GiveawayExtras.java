package net.mabako.steamgifts.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GiveawayExtras implements Serializable, ICommentHolder {
    private static final long serialVersionUID = 2559783080850462760L;
    private String title;
    private String description;
    private String xsrfToken;
    private boolean entered;
    private String timeRemaining;
    private final List<Comment> loadedComments;
    private String errorMessage;
    private boolean enterable;

    public GiveawayExtras() {
        loadedComments = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
    public List<Comment> getComments() {
        return loadedComments;
    }

    @Override
    public void addComment(Comment comment) {
        loadedComments.add(comment);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isEnterable() {
        return enterable;
    }

    public void setEnterable(boolean enterable) {
        this.enterable = enterable;
    }
}
