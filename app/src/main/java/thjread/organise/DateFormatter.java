package thjread.organise;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {
    private static final long ONE_MINUTE = 60 * 1000;
    private static final long ONE_HOUR = 60 * ONE_MINUTE;
    private static final long ONE_DAY = 24 * ONE_HOUR;

    static SimpleDateFormat dateFormat;
    static SimpleDateFormat yearDateFormat;

    static private Calendar calendarToStartOfDay(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    static final String[] weekdays = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
            "Saturday"};

    static public int days(Date d) {
        Calendar now = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        date.setTime(d);
        calendarToStartOfDay(now);
        calendarToStartOfDay(date);
        long diff = date.getTimeInMillis() - now.getTimeInMillis();
        return Math.round(diff/ONE_DAY);
    }

    static public String format(Date d, boolean dayStyle) {
        Calendar now = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        date.setTime(d);
        int days = days(d);

        String string;

        if (days == -1) {
            string = "Yesterday";
        } else if (days == 0) {
            string = "Today";
        } else if (days == 1) {
            string = "Tomorrow";
        } else if (days < 7 && days > 0) {
            string = "";
            if (dayStyle) string += Integer.toString(days) + " days (";
            string += weekdays[date.get(Calendar.DAY_OF_WEEK)-1];
            if (dayStyle) string += ")";
        } else {
            if (dateFormat == null) {
                dateFormat = new SimpleDateFormat("EEE d MMM", Locale.getDefault());
            }
            if (yearDateFormat == null) {
                yearDateFormat = new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault());
            }

            string = "";
            if (days <= 14 && days > 0 && dayStyle) {
                string += Integer.toString(days) + " days (";
            }

            if (now.get(Calendar.YEAR) == date.get(Calendar.YEAR)) {
                string += dateFormat.format(d);
            } else {
                string += yearDateFormat.format(d);
            }

            if (days <= 14 && days > 0 && dayStyle) string += ")";
        }

        return string;
    }
}
