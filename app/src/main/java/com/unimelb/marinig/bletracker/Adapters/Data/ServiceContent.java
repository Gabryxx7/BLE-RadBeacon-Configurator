package com.unimelb.marinig.bletracker.Adapters.Data;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.unimelb.marinig.bletracker.Logger.TrackerLog;

import java.util.concurrent.Callable;

/**
 * So this is a cool trick that I came up with, not the best solution but is manageable!
 *
 * So this is a sort of Service Wrapper that I needed in order to create the list of services in the RecyclerView for the list of services.
 * I needed for some reasons:
 *  - I want to have a nice string to show in the view
 *  - Every service may be initialized in different ways so we want to be able to associate different methods to start and stop this service
 *
 *  But for the last point I would need to pass a function, and this function may take different parameters, reference other stuff and so on...
 *  So my solution is to create an abstract class with a two basic implemented methods (and maybe some abstract ones in the future), this methods are
 *  simply start and stop service, and by default they simply call context.startService(context,intent) or stopService.
 *  We need this as default so that we could start the service even if not overridden implementation of the ServiceBootstrapper is provided.
 *  Or alternatively, if no context is provided, and the second constructor is used, we assume they are implementing their own start/stop service
 *  Cause otherwise nothing happens )in fact, the start and stopService functions with no params in ServiceBoostrapper do absolutely nothing, they are supposed to be overridden!
 *
 *  So everytime a new ServiceContent is created, it gets a context (usually MainActivity.this), an intent which we can use to start or stop the service in case it uses
 *  the default implementation, a nice name for the service and the ServiceBootstrapper.
 *  Or alternatively, it gets a nice name and the ServiceBootstrapper, meaning that they will implement their own version of the start/stopService methods
 *  So they will take care of Context and Intent!
 */

public class ServiceContent{
    private Context mContext;
    //private Class<?> mClass;
    private Intent mIntent;
    private String mServiceName;
    private String mClassName;
    private ServiceBootstrapper mServiceBootstrapper;

    //I need to understand how to pass start and stop functions as parameters
    public ServiceContent(Context packageContext, Intent intent, String serviceName, String className, ServiceBootstrapper serviceBootstrapper){
        TrackerLog.d("ServiceContent", "Creating1 "+serviceName);
        mContext = packageContext;
        //mClass = cls;
        mIntent = intent;
        mServiceName = serviceName;
        mClassName = className;
        mServiceBootstrapper = serviceBootstrapper;
    }

    public ServiceContent(String serviceName, String className, ServiceBootstrapper serviceBootstrapper){
        TrackerLog.d("ServiceContent", "Creating2 "+serviceName);
        mServiceName = serviceName;
        mClassName = className;
        mServiceBootstrapper = serviceBootstrapper;
    }

    public String getServiceName(){
        return mServiceName;
    }

    public void startServiceContent(){
        TrackerLog.d(mServiceName, "isRunning: " + mServiceBootstrapper.isRunning());
        if(isRunning())
            return;

        if(mContext == null)
            mServiceBootstrapper.startService();
        else
            mServiceBootstrapper.startService(mContext, mIntent);

    }

    public void stopServiceContent(){
        if(!isRunning())
            return;

        if(mContext == null)
            mServiceBootstrapper.stopService();
        else
            mServiceBootstrapper.stopService(mContext, mIntent);
    }

    public boolean isRunning(){
        return mServiceBootstrapper.isRunning();
    }
}
