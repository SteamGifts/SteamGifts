package net.mabako.steamgifts.data;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;

import java.io.Serializable;

public class Giveaway implements Serializable, IEndlessAdaptable {
    public static final int VIEW_LAYOUT = R.layout.giveaway_item;

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
    private final String timeCreated;
    private boolean entered = false;

    public Giveaway(String title, String giveawayId, String name, Type type, int gameId, String creator, int entries, int commentCount, int copies, int points, String timeRemaining, String timeRemainingLong, String timeCreated) {
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
        this.timeCreated = timeCreated;
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

    public boolean isEntered() {
        return entered;
    }

    public void setEntered(boolean entered) {
        this.entered = entered;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    @Override
    public int hashCode() {
        return giveawayId.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Giveaway))
            return false;
        return giveawayId.equals(((Giveaway) o).giveawayId);
    }

    @Override
    public String toString() {
        return "[GA " + giveawayId + ", " + gameId + "]";
    }

    public void setTimeRemaining(String timeRemaining) {
        this.timeRemaining = timeRemaining.replace(" remaining", "");
        ;
    }

    @Override
    public int getLayout() {
        return VIEW_LAYOUT;
    }

    public enum Type {
        APP, SUB
    }
}
