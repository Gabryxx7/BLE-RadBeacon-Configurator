package com.unimelb.marinig.bletracker.Application;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

public class BLETrackerApplication extends Application {
    private RefWatcher refWatcher;

    @Override public void onCreate() {
        super.onCreate();
        Log.e("APPLICATIONBLE", "RUNNING");
        /*if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        refWatcher = LeakCanary.install(this);
        //JobManager.create(this).addJobCreator(new DemoJobCreator());
        // Normal app init code...
        */
    }

    public static RefWatcher getRefWatcher(Context context) {
        BLETrackerApplication application = (BLETrackerApplication) context.getApplicationContext();
        return application.refWatcher;
    }

}