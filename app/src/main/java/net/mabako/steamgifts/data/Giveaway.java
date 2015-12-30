package net.mabako.steamgifts.data;

import java.io.Serializable;
import java.util.List;

public class Giveaway implements Serializable {
    private String title;
    private String giveawayId;
    private Type type;
    private int gameId;
    private String creator;
    private int entries;
    private int commentCount;
    private int copies;
    private int points;
    private String timeRemaining;
    private String timeRemainingLong;

    public Giveaway(String title, String giveawayId, Type type, int gameId, String creator, int entries, int commentCount, int copies, int points, String timeRemaining, String timeRemainingLong) {
        this.title = title;
        this.giveawayId = giveawayId;
        this.type = type;
        this.gameId = gameId;
        this.creator = creator;
        this.entries = entries;
        this.commentCount = commentCount;
        this.copies = copies;
        this.points = points;
        this.timeRemaining = timeRemaining.replace(" remaining", "");
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

    @Override
    public boolean equals(Object o) {
        if(o == null || !(o instanceof Giveaway))
            return false;
        return ((Giveaway) o).giveawayId == giveawayId;
    }

    @Override
    public String toString() {
        return "[GA "+ giveawayId + ", " + gameId + "]";
    }

    public enum Type {
        APP, SUB
    }
}
