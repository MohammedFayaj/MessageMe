package sample.callme.com.callme;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by rahul on 10/18/17.
 */

public class Utils {

    public static final String STATS_PERIOD = "stats_peroid";
    public static  String SHARED_PREFERENCES = "CALLME_PREFERENCES";
    public static String NOTIFICATION_SERVICE_SWITCH_KEY = "notification_service_switch_key";
    public static String ACTIVE_HOURS_COUNT = "active_hours_count";
    public static String ACTIVE_TIME_START = "active_time_start";
    public static String ACTIVE_END_TIME_START = "active_end_time_start";
    public static String ACTIVE_DAY_COUNT = "active_day_count";
    public static String OUT_GOING_MESSAGE = "out_going_message";
    public static String SIGNATURE = "signature";
    public static String IS_CONTACTS_SAVED = "CONTACTSSAVED";
    public static final String STATS_PERMISSION_GRANTED = "stats permission granted";



    public static SharedPreferences getSharedPrefs(Context mContext) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES,Context.MODE_PRIVATE);
        return sharedPreferences;
    }


    public static void setSwitchPreferences(Context context, String key ,boolean value) {
        getSharedPrefs(context).edit()
                .putBoolean(key,value)
        .commit();
    }

    public static boolean getSwitchPreferences(Context context ,String key) {
        return  getSharedPrefs(context)
                .getBoolean(key,false);

    }

    public static void setActiveHoursPreferences(Context context,int value) {
        if(getSwitchPreferences(context ,NOTIFICATION_SERVICE_SWITCH_KEY)) {
            getSharedPrefs(context).edit()
                    .putInt(ACTIVE_HOURS_COUNT, value)
                    .commit();
        }
    }

    public static int getActiveHoursPreferences(Context context) {
        return  getSwitchPreferences(context,NOTIFICATION_SERVICE_SWITCH_KEY) ?getSharedPrefs(context)
                .getInt(ACTIVE_HOURS_COUNT,0) : 0;

    }

    public static void setInitialTimePreferences(Context context ,String value) {
        getSharedPrefs(context).edit()
                .putString(ACTIVE_TIME_START,value)
                .commit();
    }

    public static String getInitialTimePreferences(Context context ) {
        return   getSwitchPreferences(context,NOTIFICATION_SERVICE_SWITCH_KEY)  ? getSharedPrefs(context)
                .getString(ACTIVE_TIME_START,"06:00 AM") : "06:00 AM" ;

    }
    public static void setInitialEndTimePreferences(Context context ,String value) {
        getSharedPrefs(context).edit()
                .putString(ACTIVE_END_TIME_START,value)
                .commit();
    }

    public static String getInitialEndTimePreferences(Context context ) {
        return   getSwitchPreferences(context,NOTIFICATION_SERVICE_SWITCH_KEY)  ? getSharedPrefs(context)
                .getString(ACTIVE_END_TIME_START,"06:00 AM") : "06:00 AM" ;

    }
     public static void setDayActivePreferences(Context context,int value) {
        if(getSwitchPreferences(context,NOTIFICATION_SERVICE_SWITCH_KEY)) {
            getSharedPrefs(context).edit()
                    .putInt(ACTIVE_DAY_COUNT, value)
                    .commit();
        }
    }

    public static int getDayActivePreferences(Context context ) {
        return  getSwitchPreferences(context,NOTIFICATION_SERVICE_SWITCH_KEY)  ? getSharedPrefs(context)
                .getInt(ACTIVE_DAY_COUNT,0) : 0;

    }

    public static void setOutGoingMessage(Context context ,String message){
        getSharedPrefs(context).edit().
                putString(OUT_GOING_MESSAGE , message)
                .commit();
    }

    public static String getOutGoingMessage(Context context ) {
        return getSharedPrefs(context).getString(OUT_GOING_MESSAGE ,"");
    }

    public static void setSignature(Context context ,String message){
        getSharedPrefs(context).edit().
                putString(SIGNATURE , message)
                .commit();
    }

    public static String getSignature(Context context ) {
        return getSharedPrefs(context).getString(SIGNATURE ,"Rahul .");
    }
    public static void IsContactsSaved(Context context ,String message){
        getSharedPrefs(context).edit().
                putString(IS_CONTACTS_SAVED , message)
                .commit();
    }
}
