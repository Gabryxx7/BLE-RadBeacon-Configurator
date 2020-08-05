package com.unimelb.marinig.bletracker.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import android.util.Log;

import com.unimelb.marinig.bletracker.Logger.TrackerLog;
import com.unimelb.marinig.bletracker.Services.BLEScanner;

public class ServiceRestarterBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BroadcastRestarter";
    @Override
    public void onReceive(Context context, Intent intent) {
        TrackerLog.e(TAG, "Got intent " +intent.getAction() +", restarting!");
        Log.e(TAG, "Message  Received Broadcast!!");
        ContextCompat.startForegroundService(context, new Intent(context, BLEScanner.class));
    }
}
