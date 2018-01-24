package sample.callme.com.callme;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

/**
 * Created by rahul on 10/24/17.
 */

public class CountDownService extends Service {

    private int time ;
    private CountDownTimer timer;
    private List<Contact> listofContactsSelected ;
    private Database mDB;


    @Override
    public void onCreate() {
        super.onCreate();
        mDB = new Database(CountDownService.this);
        listofContactsSelected = mDB.getSelectedContacts();
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        time = intent.getIntExtra(MainActivity.COUNT_DOWN_TIME,0);
        if(time != 0) {
            startCountDownTimer(time);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if( timer != null)
        timer.cancel();
        timer = null;
    }

    private void startCountDownTimer(final int time){
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new CountDownTimer(time,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("CountDownService.java",millisUntilFinished/1000 +" : time finished");
            }

            @Override
            public void onFinish() {
                //send sms to selectedContaccts
                listofContactsSelected = mDB.getSelectedContacts();
                Log.d("Between time specified",inBetweentheSpecifiedTime()+"");
                if (inBetweentheSpecifiedTime()) {
                    sendSMStoContacts(listofContactsSelected);
                    startCountDownTimer(time);
                }
            }
        };

        timer.start();
    }


    private void sendSMStoContacts(List<Contact> contactSelcted) {
        SmsManager smsManager = SmsManager.getDefault();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            for (Contact contact : contactSelcted) {
                smsManager.sendTextMessage(contact.getPhoneNumber(), null, formatMessage(), null, null);
            }
            Toast.makeText(CountDownService.this, "SMS sent.",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(CountDownService.this, "Please enable Send SMS permissions to perform this action .",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private String formatMessage() {

        return "Dear Family /Friends ,\n"+Utils.getOutGoingMessage(CountDownService.this) +"\n Thanks , \n" +
                Utils.getSignature(CountDownService.this);
    }


    private boolean inBetweentheSpecifiedTime(){
        String[] startTime =timeinHrs(Utils.getInitialTimePreferences(CountDownService.this));
        String[] endTime = timeinHrs(Utils.getInitialEndTimePreferences(CountDownService.this));

        Calendar startCalendar = Calendar.getInstance();
        String timeFormat = startTime[1].substring(3,5);
        int hourOfDay = Integer.parseInt(startTime[0]);
        if (timeFormat.equalsIgnoreCase("PM")) {
            if (hourOfDay != 12) {
                hourOfDay = hourOfDay + 12;
            }
        }
        else {
            if (hourOfDay == 12) {
                hourOfDay = 0;
            }
        }
        startCalendar.set(Calendar.HOUR_OF_DAY , hourOfDay);
        startCalendar.set(Calendar.MINUTE , Integer.parseInt(startTime[1].substring(0,2)));

        Calendar endCalendar = Calendar.getInstance();
        String endFormat = endTime[1].substring(3,5);
        int endhourOfDay = Integer.parseInt(endTime[0]);
        if (endFormat.equalsIgnoreCase("PM")) {
            if (endhourOfDay != 12) {
                endhourOfDay = hourOfDay + 12;
            }
        }
        else {
            if (endhourOfDay == 12) {
                endhourOfDay = 0;
            }
        }
        endCalendar.set(Calendar.HOUR_OF_DAY , endhourOfDay);
        endCalendar.set(Calendar.MINUTE , Integer.parseInt(endTime[1].substring(0,2)));

        Calendar currentTime = Calendar.getInstance();
        currentTime.set(Calendar.HOUR_OF_DAY,currentTime.get(Calendar.HOUR_OF_DAY));
        currentTime.set(Calendar.MINUTE ,currentTime.get(Calendar.MINUTE));

        if (startCalendar.getTimeInMillis() < currentTime.getTimeInMillis()
                && endCalendar.getTimeInMillis() > currentTime.getTimeInMillis()) {
                return true ;
        }

        return false ;
    }

    private String[] timeinHrs(String time) {

        return  time.split(":");


    }

}

