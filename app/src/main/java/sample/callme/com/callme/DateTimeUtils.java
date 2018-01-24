package sample.callme.com.callme;

import android.annotation.SuppressLint;
import android.content.Context;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;



public class DateTimeUtils {

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";
    @SuppressLint("SimpleDateFormat") // not for show human purpose
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_PATTERN);

    private DateTimeUtils() {
    }

    public static String secondsToTime(long input, Context context) {
        String result = null;

        if (input <= 0) {  // less than minute
            result = "< 1 " + context.getResources().getString(R.string.minutes);
        } else if ((input < 60)) {  // from minute to hour
//            long mins = seconds / 60;
            result = String.valueOf(input) + " " + context.getResources().getString(R.string.minutes);
        }

        else if (input >= 60) {  // more than hour

            Long days = input / (24 * 60);
            Long hours = (input % (24 * 60)) / 60;
            Long minutes = (input % (24 * 60)) % 60;

            if (days == 0) {


                    result = String.valueOf(hours) + " " + context.getResources().getString(R.string.hours)
                            + " " + String.valueOf(minutes) + " " + context.getResources().getString(R.string.minutes);

            } else {
                result = String.valueOf(days) + " " +"Days " +hours+" " + context.getResources().getString(R.string.hours)
                        + " " + String.valueOf(minutes) + " " + context.getResources().getString(R.string.minutes);
            }
        }
            return result;


        }
}
