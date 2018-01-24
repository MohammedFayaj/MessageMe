package sample.callme.com.callme;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


public class AppsUsageActivity extends AppCompatActivity {
    private static final String TAG = "AppsUsageActivity";
    private ProgressBar progressBar;
    UsageStatsManager mUsageStatsManager;
    UsageListAdapter mUsageListAdapter;
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    static Context mContext;

    final static String USAGE_STATS_SERVICE_NAME = "usagestats";

    public static final List<String> SYSTEM_PACKAGES = Arrays.asList(
            "android",
            "com.android.launcher3",
            "com.android.vending",
            "com.android.packageinstaller",
            "com.android.settings",
            "com.android.systemui",
            "com.google.android.gms",
            "com.android.launcher",
            "com.android.packageinstaller",
            "com.android.voicedialer",
            "com.android.deskclock",
            "com.sec.android.app.clockpackage",
            "com.android.server.telecom"
    );
    public static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance();

    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appusage);

        mUsageStatsManager = (UsageStatsManager) this
                .getSystemService(Context.USAGE_STATS_SERVICE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar_appusage);
        setSupportActionBar(toolbar);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("APPS USAGE");
        }
        mContext = this;
        context = getApplicationContext();
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_app_usage);

        progressBar = (ProgressBar) findViewById(R.id.stats_progress_bar);
        //mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        Period period = Period.fromInt(prefs.getInt(Utils.STATS_PERIOD, 1));

        refreshList(period);
    }

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
//        super.onCreate(savedInstanceState, persistentState);
//
//    }


    public void onResume() {
        super.onResume();


    }

    private void stopLoadTask() {


        if (loadTask != null && loadTask.getStatus() == AsyncTask.Status.RUNNING) {
            loadTask.cancel(false);
        }


    }

    private class LoadStatsTask extends AsyncTask<Period, Void, List<ItemUsageStats>> {

        Period period;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // listView.setAdapter(null);

            System.out.println("hereee it is");

            mRecyclerView.setAdapter(null);
            progressBar.setVisibility(View.VISIBLE);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        protected List<ItemUsageStats> doInBackground(Period... params) {
            period = params[0];

            try {
                List<UsageStats> usageStatsList = getUsageStatistics(period);
                List<ItemUsageStats> statsList = updateAppsList(usageStatsList);
                return statsList;

            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }


        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(List<ItemUsageStats> result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.GONE);


            setRecylerAdapter(result);


        }


    }

    private void setRecylerAdapter(List<ItemUsageStats> result) {
        mUsageListAdapter = new UsageListAdapter(context, result);

        mRecyclerView.setAdapter(mUsageListAdapter);
        // mUsageListAdapter.setCustomUsageStatsList(result);
        //  mUsageListAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.clear();
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_stats, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
            case R.id.menu_item_refresh:
                Period period = Period.fromInt(prefs.getInt(Utils.STATS_PERIOD, 1));
                refreshList(period);
                return true;
            case R.id.choose_submenu_day:
                prefs.edit().putInt(Utils.STATS_PERIOD, Period.DAY.asInt()).apply();
                refreshList(Period.DAY);
                return true;

            case R.id.choose_submenu_week:
                prefs.edit().putInt(Utils.STATS_PERIOD, Period.WEEK.asInt()).apply();
                refreshList(Period.WEEK);
                return true;

            case R.id.choose_submenu_month:
                prefs.edit().putInt(Utils.STATS_PERIOD, Period.MONTH.asInt()).apply();
                refreshList(Period.MONTH);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    LoadStatsTask loadTask;

    private void refreshList(Period period) {
        Log.d(TAG, "Stats - refreshList()");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && !permissionGranted()) {
            return;
        }

        loadTask = new LoadStatsTask();
        loadTask.execute(period);


    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean permissionGranted() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        AppOpsManager appOps = (AppOpsManager) getApplicationContext().getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), getApplicationContext().getPackageName());

        if (mode == AppOpsManager.MODE_ALLOWED) {
            prefs.edit().putBoolean(Utils.STATS_PERMISSION_GRANTED, true).apply();
            return true;
        } else {

            openDialog();
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void openDialog() {


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                launchUsPermission(mContext);
            }
        });

    }


    protected void launchUsPermission(final Context context) {


        // LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //View mUsPermission = inflater.inflate(R.layout.us_permission, null);
        //  mUsPermission.refreshDrawableState();
        new AlertDialog.Builder(context)
                .setTitle(R.string.us_permission_title)
                .setMessage("Requires the new Usage Stats permission in order to get the apps usage")
                .setIcon(R.mipmap.ic_launcher)
//                .setView(mUsPermission)
                .setPositiveButton(R.string.us_permission_settings_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                try {
                                    context.startActivity(new Intent(
                                            android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS));
                                } catch (ActivityNotFoundException ane) {

                                }

                            }
                        })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        progressBar.setVisibility(View.GONE);

                        dialog.dismiss();

                    }
                })
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public List<ItemUsageStats> updateAppsList(List<UsageStats> usageStatsList) {
        removeSystemApps(usageStatsList);

        List<ItemUsageStats> customUsageStatsList = new ArrayList<>();


        PackageManager pm = context.getApplicationContext().getPackageManager();

        for (UsageStats stat : usageStatsList) {
            try {
               Log.d(TAG, stat.getPackageName());

                String packageName = stat.getPackageName();
                PackageInfo pi = pm.getPackageInfo(packageName, 0);
                ItemUsageStats customUsageStats = new ItemUsageStats();

                customUsageStats.packageName = packageName;
                customUsageStats.appIcon = pi.applicationInfo.loadIcon(pm);
                customUsageStats.title = pi.applicationInfo.loadLabel(pm).toString();
                //  se.setInstallDate(pi.firstInstallTime);

                customUsageStats.time = ((long) stat.getTotalTimeInForeground() / (1000 * 60));

                customUsageStatsList.add(customUsageStats);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, e.toString());
            }
        }

        Collections.sort(customUsageStatsList, new Comparator<ItemUsageStats>() {
            @Override
            public int compare(ItemUsageStats fe, ItemUsageStats se) {
                return (int) se.time - (int) fe.time;
            }
        });


        return customUsageStatsList;

    }

    private static class LastTimeLaunchedComparatorDesc implements Comparator<UsageStats> {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public int compare(UsageStats left, UsageStats right) {
            return Long.compare(right.getLastTimeUsed(), left.getLastTimeUsed());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public List<UsageStats> getUsageStatistics(Period period) {
//        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.YEAR, -1);
//
//        List<UsageStats> queryUsageStats = mUsageStatsManager
//                .queryUsageStats(intervalType, cal.getTimeInMillis(),
//                        System.currentTimeMillis());

        long from = getTimeFrom(period);
        long till = getTimeTill(period);
        int intervalType = getIntervalType(period);

        Log.d(TAG, "from " + DATE_FORMAT.format(new Date(from)) + " to " + DATE_FORMAT.format(new Date(till)));

        List<UsageStats> queryUsageStats = mUsageStatsManager.queryUsageStats(intervalType, from, till);


        if (queryUsageStats.size() == 0) {
            return null;
        }
        return queryUsageStats;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void removeSystemApps(List<UsageStats> stats) {
        Iterator<UsageStats> it = stats.iterator();


        while (it.hasNext()) {
            UsageStats next = it.next();
            if (SYSTEM_PACKAGES.contains(next.getPackageName())) {
                it.remove();
            }
        }
    }


    private long getTimeFrom(Period period) {
        Calendar from = Calendar.getInstance();

        from.set(Calendar.HOUR_OF_DAY, 0);
        from.set(Calendar.MINUTE, 0);
        from.set(Calendar.SECOND, 0);
        from.set(Calendar.MILLISECOND, 0);

        switch (period) {
            case DAY:
                break;
            case YESTERDAY:
                from.add(Calendar.DATE, -1);
                break;
            case WEEK:
                from.add(Calendar.DATE, -7);
                break;
            case MONTH:
                from.add(Calendar.MONTH, -1);
                break;
        }

        return from.getTimeInMillis();
    }

    private long getTimeTill(Period period) {
        Calendar till = Calendar.getInstance();

        switch (period) {
            case DAY:
                break;
            case YESTERDAY:
                till.set(Calendar.HOUR_OF_DAY, 0);
                till.set(Calendar.MINUTE, 0);
                till.set(Calendar.SECOND, 0);
                till.set(Calendar.MILLISECOND, 0);
                break;
            case WEEK:
                break;
            case MONTH:
                break;
        }

        return till.getTimeInMillis();
    }

    private int getIntervalType(Period period) {
        int intervalType = 0;

        switch (period) {
            case DAY:
                intervalType = UsageStatsManager.INTERVAL_DAILY;
                break;
            case YESTERDAY:
                intervalType = UsageStatsManager.INTERVAL_DAILY;
                break;
            case WEEK:
                intervalType = UsageStatsManager.INTERVAL_WEEKLY;
                break;
            case MONTH:
                intervalType = UsageStatsManager.INTERVAL_MONTHLY;
                break;
//            case BEST:
//                intervalType = UsageStatsManager.INTERVAL_BEST;
//                break;
        }

        return intervalType;
    }
}

