package net.mabako.steamgifts.data;

import net.mabako.Constants;

import java.io.Serializable;

public class User extends BasicUser implements Serializable {
    private static final long serialVersionUID = -313348528668961481L;
    private String url, role;

    private int level, created, won, comments, positiveFeedback, negativeFeedback;
    private String createdAmount, wonAmount;

    private boolean loaded = false, whitelisted, blacklisted;

    public User() {

    }

    public User(String name) {
        setName(name);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public String getCreatedAmount() {
        return createdAmount;
    }

    public void setCreatedAmount(String createdAmount) {
        this.createdAmount = createdAmount;
    }

    public int getWon() {
        return won;
    }

    public void setWon(int won) {
        this.won = won;
    }

    public String getWonAmount() {
        return wonAmount;
    }

    public void setWonAmount(String wonAmount) {
        this.wonAmount = wonAmount;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public boolean isWhitelisted() {
        return whitelisted;
    }

    public void setWhitelisted(boolean whitelisted) {
        this.whitelisted = whitelisted;
    }

    public boolean isBlacklisted() {
        return blacklisted;
    }

    public void setBlacklisted(boolean blacklisted) {
        this.blacklisted = blacklisted;
    }

    public int getPositiveFeedback() {
        return positiveFeedback;
    }

    public void setPositiveFeedback(int positiveFeedback) {
        this.positiveFeedback = positiveFeedback;
    }

    public int getNegativeFeedback() {
        return negativeFeedback;
    }

    public void setNegativeFeedback(int negativeFeedback) {
        this.negativeFeedback = negativeFeedback;
    }

    public String getRole() {
        return role;
    }

    /**
     * Set the role, if it is a worthwhile role to display.
     *
     * @param role role to display
     */
    public void setRole(String role) {
        if (Constants.IMPORTANT_USER_ROLES.contains(role))
            this.role = role;
    }
}
