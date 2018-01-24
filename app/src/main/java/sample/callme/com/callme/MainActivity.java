package sample.callme.com.callme;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.DialogFragment;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, WakeScreenListener {

    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 2;


    private Spinner activeCountDaysTextView;
    private Spinner activeHoursCountTextView;
    public TextView activeStartHourTextView;
    public TextView activeEndHourTextView;
    private ImageView arrowUpHourImageView;
    private ImageView arrowDownHourImageView;
    private ImageView arrowUpDayImageView;
    private ImageView arrowDownDayImageView;
    private TimePickerDialog timePicker;
    private ImageView contactChooser;
    private SwitchCompat notificationServiceSwtich;
    private Button refreshButton;
    private TextView outGoingMessages;
    private TextView appUsageTime;
    Calendar beginCal = Calendar.getInstance();
    Calendar endCal = Calendar.getInstance();

    private AppDataAdapter appDataAdapter;

    private String hours[] = {"0.05","0.1","0.3","2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
    private String days[] = {"2", "3", "4", "5", "6", "7", "8", "9", "10"};

    public TextView mContactSelectedCount;
    public List<SelectedContactDetails> listofSelectedContacts = new ArrayList<>();

    public static final int RQS_PICK_CONTACT = 1;

    private ImageView pickContactTextView;

    private Database mDB = null;

    private int dayActiveCount = 0, hoursActiveCount = 0;
    ScreenWakeReceiver mScreenStateReceiver;

    private int timetoCheckinActivity = 0;
    public static String COUNT_DOWN_TIME = "count_down_time";

    private ListView mAppsDataList;
    private List<UsageStats> mAppData;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDB = new Database(MainActivity.this);

        // check for runtime permissions available for user to access the contacts
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {

            if (mDB.getAllContacts() == null || mDB.getAllContacts().size() == 0) {
                //getContactsList(MainActivity.this);

                LoaderTask loadTask = new LoaderTask();
                loadTask.execute();
            }

        } else {
            requestContactPermissions();

        }
        initializeViews();


    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!isAccessGranted()) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }

        mScreenStateReceiver = new ScreenWakeReceiver(this);

        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenStateReceiver, screenStateFilter);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initializeViews() {
        activeCountDaysTextView = (Spinner) findViewById(R.id.tv_count_active_days);
        activeHoursCountTextView = (Spinner) findViewById(R.id.tv_count_active_hours);
        activeStartHourTextView = (TextView) findViewById(R.id.tv_time_start);
        activeEndHourTextView = (TextView) findViewById(R.id.tv_time_end);

        arrowUpDayImageView = (ImageView) findViewById(R.id.iv_arrow_up_days);
        arrowDownDayImageView = (ImageView) findViewById(R.id.iv_arrow_down_days);
        arrowUpHourImageView = (ImageView) findViewById(R.id.iv_arrow_up);
        arrowDownHourImageView = (ImageView) findViewById(R.id.iv_arrow_down);

        pickContactTextView = (ImageView) findViewById(R.id.iv_pick_contact);
        mContactSelectedCount = (TextView) findViewById(R.id.tv_contacts_count);
        contactChooser = (ImageView) findViewById(R.id.iv_contact_choose);
        refreshButton = (Button) findViewById(R.id.app_usage);
        refreshButton.setOnClickListener(this);


        appUsageTime = (TextView) findViewById(R.id.tv_app_name);
        outGoingMessages = (TextView) findViewById(R.id.outgoing_messages_view);
        outGoingMessages.setOnClickListener(this);


        if (!Utils.getSwitchPreferences(MainActivity.this, Utils.NOTIFICATION_SERVICE_SWITCH_KEY)) {
            mContactSelectedCount.setText("0");
        } else {
            mContactSelectedCount.setText(mDB.getSelectedContacts().size() + "");
        }

        notificationServiceSwtich = (SwitchCompat) findViewById(R.id.notification_service_switch);
        notificationServiceSwtich.setChecked(Utils.getSwitchPreferences(MainActivity.this, Utils.NOTIFICATION_SERVICE_SWITCH_KEY));
        notificationServiceSwtich.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Utils.setSwitchPreferences(MainActivity.this, Utils.NOTIFICATION_SERVICE_SWITCH_KEY, isChecked);
                activeStartHourTextView.setText(Utils.getInitialTimePreferences(MainActivity.this));
                activeEndHourTextView.setText(Utils.getInitialEndTimePreferences(MainActivity.this));
                activeHoursCountTextView.setSelection(Utils.getActiveHoursPreferences(MainActivity.this));
                activeCountDaysTextView.setSelection(Utils.getDayActivePreferences(MainActivity.this));
//                updateEndTime(integerFromString(hours[activeHoursCountTextView.getSelectedItemPosition()]));
                if (!isChecked) {
                    mContactSelectedCount.setText("0");
                    pickContactTextView.setOnClickListener(null);

                } else {
                    updateCount();
                    pickContactTextView.setOnClickListener(MainActivity.this);
                }
            }
        });

//        mAppsDataList = (ListView) findViewById(R.id.apps_data_list);
//        mAppData = getGmailData();
//        appDataAdapter = new AppDataAdapter(mAppData);
//        mAppsDataList.setAdapter(appDataAdapter);

        arrowDownHourImageView.setOnClickListener(this);
        arrowDownDayImageView.setOnClickListener(this);
        arrowUpHourImageView.setOnClickListener(this);
        arrowUpDayImageView.setOnClickListener(this);
        activeStartHourTextView.setOnClickListener(this);
        activeEndHourTextView.setOnClickListener(this);
        pickContactTextView.setOnClickListener(this);
        contactChooser.setOnClickListener(this);
        activeStartHourTextView.setText(Utils.getInitialTimePreferences(MainActivity.this));
        activeEndHourTextView.setText(Utils.getInitialEndTimePreferences(MainActivity.this));
        activeHoursCountTextView.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, hours));

//        updateEndTime(integerFromString(hours[Utils.getActiveHoursPreferences(MainActivity.this)]));

        activeCountDaysTextView.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, days));

        activeHoursCountTextView.setSelection(Utils.getActiveHoursPreferences(MainActivity.this) != 0 ? Utils.getActiveHoursPreferences(MainActivity.this) : 0);
        activeCountDaysTextView.setSelection(Utils.getDayActivePreferences(MainActivity.this));

        activeCountDaysTextView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Utils.setDayActivePreferences(MainActivity.this, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        activeHoursCountTextView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Utils.setActiveHoursPreferences(MainActivity.this, activeHoursCountTextView.getSelectedItemPosition());
//                updateEndTime(integerFromString(hours[position]));
                hoursActiveCount = integerFromString(hours[position]);
                timetoCheckinActivity = hoursActiveCount * 60 * 60 * 1000;
                if (hoursActiveCount == 0) {
                    timetoCheckinActivity = (int) (floatFromString(hours[position])*60*60*1000);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
//                updateEndTime(integerFromString(hours[0]));
                    timetoCheckinActivity = (int) (floatFromString(hours[0])*60*60*1000);
            }
        });


        updateCount();


    }

    private void requestContactPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_CONTACTS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    private boolean isAccessGranted() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.KITKAT) {
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        applicationInfo.uid, applicationInfo.packageName);
            }
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        int id = v.getId();
        hoursActiveCount = integerFromString(hours[activeHoursCountTextView.getSelectedItemPosition()]);
        dayActiveCount = integerFromString(days[activeCountDaysTextView.getSelectedItemPosition()]);
        switch (id) {
            case R.id.outgoing_messages_view:
                startActivity(new Intent(MainActivity.this, ComposeMessageActivity.class));
                break;
            case R.id.app_usage:
                // mAppData = getGmailData();
                //appDataAdapter.notifyDataSetChanged();
                Intent intent = new Intent(MainActivity.this, AppsUsageActivity.class);
                startActivity(intent);

                break;
            case R.id.iv_arrow_up:
               /* hoursActiveCount--;
                if(hoursActiveCount < 2) {
                    hoursActiveCount = 2;
                }
                activeHoursCountTextView.setText(hoursActiveCount+"");
                break;*/
            case R.id.iv_arrow_down:
                activeHoursCountTextView.performClick();
               /* hoursActiveCount++;
                if(hoursActiveCount > 12) {
                    hoursActiveCount = 2;
                }
                activeHoursCountTextView.setText(hoursActiveCount+"");*/
                break;
            case R.id.iv_arrow_up_days:
                /*dayActiveCount--;
                if(dayActiveCount < 3) {
                    dayActiveCount = 3 ;
                }
                activeCountDaysTextView.setText(dayActiveCount+"");
                break;*/
            case R.id.iv_arrow_down_days:
                activeCountDaysTextView.performClick();
                /*dayActiveCount++;
                if(dayActiveCount > 10) {
                    dayActiveCount = 3 ;
                }
                activeCountDaysTextView.setText(dayActiveCount+"");*/
                break;
            case R.id.tv_time_start:
                timePicker = new TimePickerDialog(MainActivity.this, timeChange, Calendar.getInstance().getTime().getHours(),
                        Calendar.getInstance().getTime().getMinutes(), true);
                timePicker.show();

                break;
            case R.id.tv_time_end:
                timePicker = new TimePickerDialog(MainActivity.this, endTimeChange, Calendar.getInstance().getTime().getHours(),
                        Calendar.getInstance().getTime().getMinutes(), true);
                timePicker.show();
                break;
            case R.id.iv_contact_choose:
                Intent phonebookIntent = new Intent(MainActivity.this, ContactsActivity.class);
                startActivityForResult(phonebookIntent, RQS_PICK_CONTACT);
                break;
            case R.id.iv_pick_contact:
                if (mDB.getSelectedContacts() != null && mDB.getSelectedContacts().size() > 0) {

                    DialogFragment contactsList = SelectedContacts.getInstance(mDB.getSelectedContacts());
                    contactsList.show(getFragmentManager(), "CONTACTS LIST");
                    /*Intent listofAcquaintancesIntent = new Intent(MainActivity.this, SelectedContacts.class);
                    listofAcquaintancesIntent.putExtra(ContactsActivity.CONTACTS_LIST, (Serializable) listofSelectedContacts);
                    startActivity(listofAcquaintancesIntent);*/
                }

        }
    }

    private class LoaderTask extends AsyncTask<Void, Void, Boolean> {

        private ProgressDialog dlg;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dlg = new ProgressDialog(MainActivity.this);
            dlg.setMessage("Fetching the data...");
            dlg.setCancelable(false);
            dlg.show();
        }

        protected Boolean doInBackground(Void... params) {

            boolean fetchresult = getContactsList(getApplicationContext());

            if (fetchresult) {
                return true;
            }
            return false;
        }


        @Override
        protected void onPostExecute(Boolean result) {
            if (dlg != null) {
                if (dlg.isShowing())
                    dlg.dismiss();
            }
            super.onPostExecute(result);


        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
//                    mContactAdapter = new ContactsActivity.ContactsList(ContactsActivity.this, this);
//                    mContactList.setAdapter(mContactAdapter);
                    if (mDB.getAllContacts() == null || mDB.getAllContacts().size() == 0) {
//                        getContactsList(MainActivity.this);
                        LoaderTask loadTask = new LoaderTask();
                        loadTask.execute();
                    }


                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RQS_PICK_CONTACT) {
            if (resultCode == RESULT_OK) {
                int dataIntent = (mDB.getSelectedContacts() != null ? mDB.getSelectedContacts().size() : 0);

                if (dataIntent == 0) {
                    Toast.makeText(MainActivity.this, "No Contacts selected.", Toast.LENGTH_SHORT).show();
                    listofSelectedContacts.clear();
                }
                mContactSelectedCount.setText(String.valueOf(mDB.getSelectedContacts() != null ? mDB.getSelectedContacts().size() : 0));

            }
        }
    }

    public void updateCount() {
        mContactSelectedCount.setText(String.valueOf(mDB.getSelectedContacts() != null ? mDB.getSelectedContacts().size() : 0));
    }

    private OnTimeSetListener timeChange = new OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String session = "AM";
            if (hourOfDay > 12) {
                hourOfDay = hourOfDay - 12;
                session = "PM";
            }else if (hourOfDay == 12){
                session = "PM" ;
            }
            activeStartHourTextView.setText((hourOfDay < 10 ? "0" + hourOfDay : hourOfDay) + ":" + (minute < 10 ? "0" + minute : minute) + " " + session);
            Utils.setInitialTimePreferences(MainActivity.this, activeStartHourTextView.getText().toString());
//            updateEndTime(hoursActiveCount);
        }
    };
private OnTimeSetListener endTimeChange = new OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String session = "AM";
            if (hourOfDay > 12) {
                hourOfDay = hourOfDay - 12;
                session = "PM";
            } else if (hourOfDay == 12){
                session = "PM" ;
            }
            activeEndHourTextView.setText((hourOfDay < 10 ? "0" + hourOfDay : hourOfDay) + ":" + (minute < 10 ? "0" + minute : minute) + " " + session);
            Utils.setInitialEndTimePreferences(MainActivity.this, activeEndHourTextView.getText().toString());
        }
    };

    private void updateEndTime(int activeHours) {
        String timeStart = activeStartHourTextView.getText().toString();
        int activeMinutes = 0;
        int startHour = splitHourFromMinute(timeStart);
        if (activeHours == 0) {
            activeMinutes = (int) (floatFromString(hours[activeHoursCountTextView.getSelectedItemPosition()])*60);
        }
        int endHour = startHour + activeHours;
        String daySession = splitSessionFromTime(timeStart);
        int minutes = splitMinuteFromTime(timeStart);
        if (minutes + activeMinutes > 59 ) {
          endHour++;
            minutes =minutes+activeMinutes -60 ;
        }
        else {
            minutes = minutes + activeMinutes;
        }
        if (endHour > 12) {
            endHour = endHour - 12;
            if (daySession.equalsIgnoreCase("AM")) {
                daySession = "PM";
            } else {
                daySession = "AM";
            }
        }

        activeEndHourTextView.setText((endHour < 10 ? "0" + endHour : endHour) + ":" + (minutes < 10 ? "0" + minutes : minutes) + " " + daySession);
    }

    private String splitSessionFromTime(String time) {
        String[] timeSplit = time.split(" ");
        return timeSplit[1];
    }


    private int splitMinuteFromTime(String time) {
        String[] timeMeasure = time.split(":");
        int minutes = integerFromString(timeMeasure[1].split(" ")[0]);

        return minutes;
    }

    private int splitHourFromMinute(String s) {
        String[] timeSplit = s.split(":");
        int hours = integerFromString(timeSplit[0]);

        return hours;
    }

    private int integerFromString(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private float floatFromString(String s) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    public boolean getContactsList(Context context) {


        ArrayList<Contact> contacts = new ArrayList<>();
//        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC ");
//        while (phones.moveToNext())
//        {
//            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
//            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//            contacts.add(new Contact(name,phoneNumber));
//            mDB.insertContactsfromPhone(new Contact(name,phoneNumber ,false));
//
//        }
//        phones.close();


        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC ");
        String lastnumber = "0";

        if (cur != null) {
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    String orignalnumber = null;
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]
                                {id}, null);
                        while (pCur.moveToNext()) {
                            orignalnumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            String number = orignalnumber.replaceAll("[^0-9\\+]", "");
                            //Log.e("lastnumber ", lastnumber);
                            //Log.e("number", number);

                            if (number.equals(lastnumber)) {

                            } else {
                                lastnumber = number;

                                Log.e("lastnumber ", lastnumber);
                                int type = pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                                switch (type) {
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                        Log.e("Not Inserted", "Not inserted");
                                        break;
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:

                                        mDB.insertContactsfromPhone(new Contact(name, lastnumber, false));
                                        break;
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                                        Log.e("Not Inserted", "Not inserted");
                                        break;
                                }

                            }



                    }
                    pCur.close();

                }

            }
        }
        return true;
    }


    @Override
    public void appActive() {
        KeyguardManager myKM = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        if( myKM.inKeyguardRestrictedInputMode()) {
            //it is locked
            //do not perform anything as teh device is still in locked mode and the countdown timer needs to run
            // as it is still there is no action from user
        } else {
            //it is not locked
            stopService(new Intent(MainActivity.this, CountDownService.class));
        }

    }


    @Override
    public void appinActive() {
        if (Utils.getSwitchPreferences(MainActivity.this , Utils.NOTIFICATION_SERVICE_SWITCH_KEY)) {
            Intent service = new Intent(MainActivity.this, CountDownService.class);
            service.putExtra(COUNT_DOWN_TIME, timetoCheckinActivity);
            startService(service);
        }
    }


    public class AppDataAdapter extends BaseAdapter {

        public AppDataAdapter(List<UsageStats> mAppdata) {
            this.mAppdata = mAppdata;
        }

        private List<UsageStats> mAppdata;

        @Override
        public int getCount() {
            return mAppdata.size();
        }

        @Override
        public UsageStats getItem(int position) {
            return mAppData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            UsageStats app = getItem(position);
            convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.app_usage_view, parent, false);
            TextView appInfoTextView = (TextView) convertView.findViewById(R.id.tv_app_info);
            TextView appUsageTextView = (TextView) convertView.findViewById(R.id.app_time_usage);
            ImageView appIcon = (ImageView) convertView.findViewById(R.id.app_drawable);

            appUsageTextView.setText("Usage :" + (float) (app.getTotalTimeInForeground() / 1000) + " seconds");
            if (getPackageName(app.getPackageName()) != null) {
                appIcon.setImageDrawable(getPackageIcon(app.getPackageName()));
            }
            appInfoTextView.setText(getPackageName(app.getPackageName()));

            return convertView;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public List<UsageStats> getGmailData() {
        final UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);// Context.USAGE_STATS_SERVICE);
        beginCal = Calendar.getInstance();
        beginCal.set(Calendar.DAY_OF_MONTH, beginCal.getTime().getDate() - 1);
        beginCal.set(Calendar.MONTH, beginCal.getTime().getMonth());
        beginCal.set(Calendar.YEAR, beginCal.getTime().getYear() + 1900);

        endCal = Calendar.getInstance();
        endCal.set(Calendar.DAY_OF_MONTH, endCal.getTime().getDate());
        endCal.set(Calendar.MONTH, endCal.getTime().getMonth());
        endCal.set(Calendar.YEAR, endCal.getTime().getYear() + 1900);

        appUsageTime.setText(" results for " + beginCal.getTime().toGMTString() + " - " + endCal.getTime().toGMTString());

        final List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginCal.getTimeInMillis(), endCal.getTimeInMillis());
        System.out.println("results for " + beginCal.getTime().toGMTString() + " - " + endCal.getTime().toGMTString());
        for (UsageStats app : queryUsageStats) {
            System.out.println("back ground usage of the app " + app.getPackageName() + " | " + (float) (app.getTotalTimeInForeground() / 1000));

        }
        return queryUsageStats;
    }

    public String getPackageName(String packageName) {
        PackageManager packageManager = getPackageManager();
        ApplicationInfo appInfo;
        try {
            appInfo = packageManager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            appInfo = null;
        }

        return appInfo != null ? (String) packageManager.getApplicationLabel(appInfo) : packageName;
    }

    public Drawable getPackageIcon(String packageName) {
        PackageManager packageManager = getPackageManager();
        ApplicationInfo appInfo;
        try {
            appInfo = packageManager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            appInfo = null;
        }

        return appInfo != null ? (Drawable) packageManager.getApplicationIcon(appInfo) : null;
    }


}
