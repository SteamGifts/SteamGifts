package net.mabako.steamgifts.data;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.web.SteamGiftsUserData;

public class Giveaway extends BasicGiveaway implements IEndlessAdaptable {
    public static final int VIEW_LAYOUT = R.layout.giveaway_item;

    private String title;
    private String name;
    private Game.Type type = Game.Type.APP;
    private int gameId = Game.NO_APP_ID;
    private String creator;
    private int entries;
    private int copies;
    private int points;
    private String timeRemaining;
    private String timeCreated;
    private boolean entered = false;

    private boolean whitelist, group;
    private int level;
    private boolean isPrivate;

    /**
     * Id used (exclusively?) for filtering games.
     */
    private int internalGameId;

    public Giveaway(String giveawayId) {
        super(giveawayId);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public int getEntries() {
        return entries;
    }

    public void setEntries(int entries) {
        this.entries = entries;
    }

    public int getCopies() {
        return copies;
    }

    public void setCopies(int copies) {
        this.copies = copies;
    }

    public Game.Type getType() {
        return type;
    }

    public void setType(Game.Type type) {
        this.type = type;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(String timeRemaining) {
        this.timeRemaining = timeRemaining.replace(" remaining", "");
    }

    public boolean isOpen() {
        return timeRemaining == null || !timeRemaining.endsWith("ago");
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

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }

    public boolean isWhitelist() {
        return whitelist;
    }

    public void setWhitelist(boolean whitelist) {
        this.whitelist = whitelist;
    }

    public boolean isGroup() {
        return group;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isLevelPositive() {
        return level > 0 && SteamGiftsUserData.getCurrent().getLevel() >= level;
    }

    public boolean isLevelNegative() {
        return level > 0 && !isLevelPositive();
    }

    @Override
    public String toString() {
        return "[GA " + getGiveawayId() + ", " + gameId + "]";
    }

    public int getInternalGameId() {
        return internalGameId;
    }

    public void setInternalGameId(int internalGameId) {
        this.internalGameId = internalGameId;
    }

    @Override
    public int getLayout() {
        return VIEW_LAYOUT;
    }

    @Override
    public boolean equals(Object o) {
        if (getGiveawayId() == null && o instanceof Giveaway) {
            Giveaway g = (Giveaway) o;

            // Compare some random attributes
            return title.equals(g.title) && timeRemaining.equals(g.timeRemaining) && timeCreated.equals(g.timeCreated);
        } else
            return super.equals(o);
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
}
