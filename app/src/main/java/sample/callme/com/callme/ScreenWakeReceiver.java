package sample.callme.com.callme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by rahul on 10/24/17.
 */

public class ScreenWakeReceiver extends BroadcastReceiver{

    WakeScreenListener wakeScreenListener;

    public ScreenWakeReceiver(WakeScreenListener wakeScreenListener) {
        this.wakeScreenListener = wakeScreenListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.i("Check","Screen went OFF");
            wakeScreenListener.appinActive();
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.i("Check","Screen went ON");
            wakeScreenListener.appActive();
        }
    }
}
