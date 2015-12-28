package net.mabako.steamgifts.data;

public class Giveaway {
    private String title;
    private String giveawayId;
    private int gameId;
    private String creator;
    private int entries;
    private int comments;

    public Giveaway(String title, String giveawayId, int gameId, String creator, int entries, int comments) {
        this.title = title;
        this.giveawayId = giveawayId;
        this.gameId = gameId;
        this.creator = creator;
        this.entries = entries;
        this.comments = comments;
    }

    public String getTitle() {
        return title;
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
}
