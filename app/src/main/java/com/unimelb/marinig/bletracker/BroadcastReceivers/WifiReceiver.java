package com.unimelb.marinig.bletracker.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

public class WifiReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent intent) {
        String action  = intent.getAction();
        if(action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)){
            Log.d("WifiReceiver", ">>>>SUPPLICANT_STATE_CHANGED_ACTION<<<<<<");
            SupplicantState supl_state=((SupplicantState)intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE));
            switch(supl_state){
                case ASSOCIATED:Log.i("SupplicantState", "ASSOCIATED");
                    break;
                case ASSOCIATING:Log.i("SupplicantState", "ASSOCIATING");
                    break;
                case AUTHENTICATING:Log.i("SupplicantState", "Authenticating...");
                    break;
                case COMPLETED:Log.i("SupplicantState", "Connected");
                    break;
                case DISCONNECTED:Log.i("SupplicantState", "Disconnected");
                    break;
                case DORMANT:Log.i("SupplicantState", "DORMANT");
                    break;
                case FOUR_WAY_HANDSHAKE:Log.i("SupplicantState", "FOUR_WAY_HANDSHAKE");
                    break;
                case GROUP_HANDSHAKE:Log.i("SupplicantState", "GROUP_HANDSHAKE");
                    break;
                case INACTIVE:Log.i("SupplicantState", "INACTIVE");
                    break;
                case INTERFACE_DISABLED:Log.i("SupplicantState", "INTERFACE_DISABLED");
                    break;
                case INVALID:Log.i("SupplicantState", "INVALID");
                    break;
                case SCANNING:Log.i("SupplicantState", "SCANNING");
                    break;
                case UNINITIALIZED:Log.i("SupplicantState", "UNINITIALIZED");
                    break;
                default:Log.i("SupplicantState", "Unknown");
                    break;

            }
            int supl_error=intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
            if(supl_error==WifiManager.ERROR_AUTHENTICATING){
                Log.i("ERROR_AUTHENTICATING", "ERROR_AUTHENTICATING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        }
    }
}

