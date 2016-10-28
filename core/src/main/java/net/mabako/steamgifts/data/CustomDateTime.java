package net.mabako.steamgifts.data;

import android.content.Context;
import android.text.format.DateUtils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Locale;


/**
 * Helpers for relative time, which is in the form of "1 second", "3 weeks", and so on.
 * <ol>
 * <li>If it is a giveaway that has already ended, this is usually in the form of "Ended XX YY ago".</li>
 * <li>If it is a giveaway that is currently running, this is in the form of "XX YY remaining"</li>
 * <li>If it is a giveaway that is starting soon, this is in the form of "Begins in XX YY"</li>
 * </ol>
 */
public class CustomDateTime implements Serializable {
    private static final long serialVersionUID = -1927137928995548949L;

    /**
     * The date this current instance points to.
     */
    private final Calendar calendar;

    /**
     * True for 'begins in', false for 'xx remaining' or 'ended xx ago'
     */
    private final boolean beginning;

    /**
     * Set the time to an absolute date.
     *
     * @param timestamp the unix timestamp
     * @param beginning true if this date is the date of the beginning, and not the end. Giveaways do not have an 'end date' if they're not open yet
     */
    public CustomDateTime(int timestamp, boolean beginning) {
        this.beginning = beginning;

        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(1000L * timestamp);
    }

    public String toString(Context context) {
        final long realTimeDiff = (Calendar.getInstance().getTimeInMillis() - calendar.getTimeInMillis()) / 1000;
        long timeDiff = Math.abs(realTimeDiff);

        if (timeDiff < 60)
            return toString(timeDiff, realTimeDiff, "second");
        timeDiff /= 60;

        if (timeDiff < 60)
            return toString(timeDiff, realTimeDiff, "minute");
        timeDiff /= 60;

        if (timeDiff < 24)
            return toString(timeDiff, realTimeDiff, "hour");
        timeDiff /= 24;

        if (timeDiff <= 7)
            return toString(timeDiff, realTimeDiff, "day");

        if (timeDiff <= 30)
            return toString(timeDiff / 7, realTimeDiff, "week");

        return DateUtils.formatDateTime(context, calendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
    }

    private String toString(long timeDiff, long realTimeDiff, String unit) {
        String inWords = String.format(Locale.US, "%d %s%s", timeDiff, unit, timeDiff == 1 ? "" : "s");
        if (beginning && realTimeDiff > 0)
            // Giveaway already began, but we don't really know when it does actually -end-.
            return "Began already";
        else if (beginning)
            return "Begins in " + inWords;
        else if (realTimeDiff > 0)
            return inWords + " ago";
        else
            return inWords;
    }

    public boolean isBeginning() {
        return beginning;
    }

    public boolean isInTheFuture() {
        return !beginning && Calendar.getInstance().before(calendar);
    }

    public Calendar getCalendar() {
        return calendar;
    }
}
