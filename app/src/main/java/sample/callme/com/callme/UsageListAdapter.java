package sample.callme.com.callme;

import android.annotation.TargetApi;
import android.app.usage.UsageStats;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



class UsageListAdapter extends RecyclerView.Adapter<UsageListAdapter.ViewHolder> {
    private List<ItemUsageStats> mCustomUsageStatsList = new ArrayList<>();

    private Context context;


    public UsageListAdapter(Context context, List<ItemUsageStats> result) {
        this.context=context;
        this.mCustomUsageStatsList = result;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mPackageName;
        private final TextView mLastTimeUsed;
        private final ImageView mAppIcon;

        public ViewHolder(View v) {
            super(v);
            mPackageName = (TextView) v.findViewById(R.id.textview_package_name);
            mLastTimeUsed = (TextView) v.findViewById(R.id.textview_last_time_used);
            mAppIcon = (ImageView) v.findViewById(R.id.app_icon);
        }

        public TextView getLastTimeUsed() {
            return mLastTimeUsed;
        }

        public TextView getPackageName() {
            return mPackageName;
        }

        public ImageView getAppIcon() {
            return mAppIcon;
        }
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_usage, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getPackageName().setText(
                mCustomUsageStatsList.get(position).title);
        long totalTimeUsed = mCustomUsageStatsList.get(position).time;
        viewHolder.getLastTimeUsed().setText(DateTimeUtils.secondsToTime(totalTimeUsed, context));

        viewHolder.getAppIcon().setImageDrawable(mCustomUsageStatsList.get(position).appIcon);
    }

    @Override
    public int getItemCount() {
        return mCustomUsageStatsList.size();
    }

    public void setCustomUsageStatsList(List<ItemUsageStats> customUsageStats) {
        this.mCustomUsageStatsList = customUsageStats;
        notifyDataSetChanged();
    }


}
