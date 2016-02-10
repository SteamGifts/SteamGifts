package net.mabako.steamgifts.data;

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
     * <p>We assume the string passed in fits either of the following:
     * <ul>
     * <li>"Today, 3:40pm"</li>
     * <li>"Tomorrow, 3:40am"</li>
     * <li>"January 26, 2016, 3:40am"</li>
     * </ul>
     *
     * @param time the time string to parse
     */
    public CustomDateTime(String time) {
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
            beginning = false;
        } catch (ParseException e) {
            Log.w(Giveaway.class.getSimpleName(), "Unable to handle date " + time + " // " + realTime, e);
            throw new RuntimeException(e);
        }
    }

    public String toString() {
        return "?";
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
