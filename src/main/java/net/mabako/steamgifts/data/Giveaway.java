package net.mabako.steamgifts.data;

import android.util.Log;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Giveaway extends BasicGiveaway implements IEndlessAdaptable {
    private static final long serialVersionUID = 1356878822345232771L;
    private static final String[] relativeDates = new String[]{"Yesterday", "Today", "Tomorrow"};
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
    private Calendar endTime;
    private boolean entered = false;

    private boolean whitelist, group;
    private int level;
    private boolean isPrivate, regionRestricted;

    /**
     * Id used (exclusively?) for filtering games.
     */
    private int internalGameId;

    public Giveaway() {
        super(null);
    }

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
        this.timeRemaining = timeRemaining != null ? timeRemaining.replace(" remaining", "") : null;
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

    public boolean isRegionRestricted() {
        return regionRestricted;
    }

    public void setRegionRestricted(boolean regionRestricted) {
        this.regionRestricted = regionRestricted;
    }

    public Calendar getEndTime() {
        return endTime;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }

    /**
     * <p>We assume the string passed in fits either of the following:
     * <ul>
     * <li>"Today, 3:40pm"</li>
     * <li>"Tomorrow, 3:40am"</li>
     * <li>"January 26, 2016, 3:40am"</li>
     * </ul>
     *
     * @param endTime when this giveaway presumably ends
     */
    public void setEndTime(final String endTime) {
        String realTime = endTime;

        for (int daysOffset = 0; daysOffset < relativeDates.length; ++daysOffset) {
            if (endTime.startsWith(relativeDates[daysOffset] + ", ")) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, daysOffset - 1);
                realTime = endTime.replace(relativeDates[daysOffset], new SimpleDateFormat("MMMM d, yyyy", Locale.US).format(calendar.getTime()));
                break;
            }
        }

        try {
            Date date = new SimpleDateFormat("MMMM d, yyyy, h:mma", Locale.US).parse(realTime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            setEndTime(calendar);
        } catch (ParseException e) {
            Log.w(Giveaway.class.getSimpleName(), "Unable to handle date " + endTime + " // " + realTime, e);
        }
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
