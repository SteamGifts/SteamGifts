package net.mabako.steamgifts.data;

import java.io.Serializable;

public class Giveaway implements Serializable {
    private final String title;
    private final String giveawayId;
    private final String name;
    private final Type type;
    private final int gameId;
    private final String creator;
    private final int entries;
    private final int commentCount;
    private final int copies;
    private final int points;
    private String timeRemaining;
    private final String timeRemainingLong;
    private boolean entered = false;

    public Giveaway(String title, String giveawayId, String name, Type type, int gameId, String creator, int entries, int commentCount, int copies, int points, String timeRemaining, String timeRemainingLong) {
        this.title = title;
        this.giveawayId = giveawayId;
        this.name = name;
        this.type = type;
        this.gameId = gameId;
        this.creator = creator;
        this.entries = entries;
        this.commentCount = commentCount;
        this.copies = copies;
        this.points = points;
        setTimeRemaining(timeRemaining);
        this.timeRemainingLong = timeRemainingLong;
    }

    public String getTitle() {
        return title;
    }

    public int getGameId() {
        return gameId;
    }

    public String getGiveawayId() {
        return giveawayId;
    }

    public String getName() {
        return name;
    }

    public String getCreator() {
        return creator;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public int getEntries() {
        return entries;
    }

    public int getCopies() {
        return copies;
    }

    public Type getType() {
        return type;
    }

    public int getPoints() {
        return points;
    }

    public String getTimeRemaining() {
        return timeRemaining;
    }

    public String getTimeRemainingLong() {
        return timeRemainingLong;
    }

    @Override
    public int hashCode() {
        return giveawayId.hashCode();
    }

    public boolean isEntered() {
        return entered;
    }

    public void setEntered(boolean entered) {
        this.entered = entered;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || !(o instanceof Giveaway))
            return false;
        return giveawayId.equals(((Giveaway) o).giveawayId);
    }

    @Override
    public String toString() {
        return "[GA "+ giveawayId + ", " + gameId + "]";
    }

    public void setTimeRemaining(String timeRemaining) {
        this.timeRemaining = timeRemaining.replace(" remaining", "");;
    }

    public enum Type {
        APP, SUB
    }
}
