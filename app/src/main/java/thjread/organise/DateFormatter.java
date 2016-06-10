package thjread.organise;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

    static public String format(Date d) {
        Calendar now = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        date.setTime(d);
        calendarToStartOfDay(now);
        calendarToStartOfDay(date);
        long diff = date.getTimeInMillis() - now.getTimeInMillis();
        int days = Math.round(diff/ONE_DAY);

        String string;

        if (days == -1) {
            string = "Yesterday";
        } else if (days == 0) {
            string = "Today";
        } else if (days == 1) {
            string = "Tomorrow";
        } else if (days < 7 && days > 0) {
            string = weekdays[date.get(Calendar.DAY_OF_WEEK)-1];
        } else {
            if (dateFormat == null) {
                dateFormat = new SimpleDateFormat("EEE dd MMM");
            }
            if (yearDateFormat == null) {
                yearDateFormat = new SimpleDateFormat("EEE dd MMM yyyy");
            }

            if (now.get(Calendar.YEAR) == date.get(Calendar.YEAR)) {
                string = dateFormat.format(d);
            } else {
                string = yearDateFormat.format(d);
            }
        }

        return string;
    }
}
