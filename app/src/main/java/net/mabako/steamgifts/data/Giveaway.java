package net.mabako.steamgifts.data;

public class Giveaway {
    private String title;
    private String giveawayId;
    private Type type;
    private int gameId;
    private String creator;
    private int entries;
    private int comments;
    private int copies;
    private int points;

    public Giveaway(String title, String giveawayId, Type type, int gameId, String creator, int entries, int comments, int copies, int points) {
        this.title = title;
        this.giveawayId = giveawayId;
        this.type = type;
        this.gameId = gameId;
        this.creator = creator;
        this.entries = entries;
        this.comments = comments;
        this.copies = copies;
        this.points = points;
    }

    public String getTitle() {
        return title;
    }

    public int getGameId() {
        return gameId;
    }

    public String getCreator() {
        return creator;
    }

    public int getComments() {
        return comments;
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
