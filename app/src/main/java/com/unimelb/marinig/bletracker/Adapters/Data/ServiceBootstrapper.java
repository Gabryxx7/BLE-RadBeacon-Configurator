package com.unimelb.marinig.bletracker.Adapters.Data;

import android.content.Context;
import android.content.Intent;

/**
 * This is an helper class used by ServiceContent, it simply provides the standard methods that every
 * ServiceContent object should have in its Bootstrapper in order to start or stop a service
 */

public class ServiceBootstrapper {

    public void startService(Context context, Intent intent){
        context.startService(intent);
    }
    public void stopService(Context context, Intent intent){
        context.startService(intent);
    }

    public void startService(){}
    public void stopService(){}

    public boolean isRunning(){return true;}
}