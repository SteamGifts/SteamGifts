package net.mabako.steamgifts.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.util.Log;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
     * A list of dates we have to parse relatively to the current day.
     */
    private static final String[] relativeDates = new String[]{"Yesterday", "Today", "Tomorrow"};

    /**
     * The date this current instance points to.
     */
    private final Calendar calendar;

    /**
     * True for 'begins in', false for 'xx remaining' or 'ended xx ago'
     */
    private final boolean beginning;

    /**
     * <p>Set the time to an absolute date. We assume either of the following formats:
     * <ul>
     * <li>"Today, 3:40pm"</li>
     * <li>"Tomorrow, 3:40am"</li>
     * <li>"January 26, 2016, 3:40am"</li>
     * </ul>
     *
     * @param time      the time string to parse
     * @param beginning true if this date is the date of the beginning, and not the end. Giveaways do not have an 'end date' if they're not open yet
     */
    public CustomDateTime(@NonNull final String time, boolean beginning) {
        this.beginning = beginning;
        String realTime = time;

        for (int daysOffset = 0; daysOffset < relativeDates.length; ++daysOffset) {
            if (time.startsWith(relativeDates[daysOffset] + ", ")) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, daysOffset - 1);
                realTime = time.replace(relativeDates[daysOffset], new SimpleDateFormat("MMMM d, yyyy", Locale.US).format(calendar.getTime()));
                break;
            }
        }

        try {
            Date date = new SimpleDateFormat("MMMM d, yyyy, h:mma", Locale.US).parse(realTime);
            calendar = Calendar.getInstance();
            calendar.setTime(date);
        } catch (ParseException e) {
            Log.w(Giveaway.class.getSimpleName(), "Unable to handle date " + time + " // " + realTime, e);
            throw new RuntimeException(e);
        }
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
        String inWords = String.format("%d %s%s", timeDiff, unit, timeDiff == 1 ? "" : "s");
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
