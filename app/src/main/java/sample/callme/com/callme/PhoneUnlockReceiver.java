package sample.callme.com.callme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by skumbam on 12/26/17.
 */

public class PhoneUnlockReceiver extends BroadcastReceiver {

    private static final String TAG = PhoneUnlockReceiver.class.getSimpleName() ;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
            Log.d(TAG, "Phone unlocked");
            context.stopService(new Intent(context, CountDownService.class));
        }else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
            Log.d(TAG, "Phone locked");
        }
    }
}
