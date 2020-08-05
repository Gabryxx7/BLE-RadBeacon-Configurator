package com.unimelb.marinig.bletracker.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.text.TextUtils;

import com.unimelb.marinig.bletracker.Activities.MainActivity;
import com.unimelb.marinig.bletracker.Logger.TrackerLog;
import com.unimelb.marinig.bletracker.R;

//Found here: https://stackoverflow.com/questions/37880432/single-notification-for-multiple-foreground-services-using-startforeground-cal
//Foreground notification and services example: https://github.com/commonsguy/cw-omnibus/tree/v8.4/Notifications/Foreground
//Some more info: https://stackoverflow.com/questions/18029431/how-to-correctly-handle-startforegrounds-two-notifications

/**
 * Why do we use notification??
 * A started service can use the startForeground API to put the service in a foreground state, where the system considers it to be something the user is actively aware of and thus not a candidate for killing when low on memory.
 * By default services are background, meaning that if the system needs to kill them to reclaim more memory (such as to display a large page in a web browser), they can be killed without too much harm.
 * More here: https://developer.android.com/reference/android/app/Service
 **/
public class ServiceNotificationManager {

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.local_service_started;
    //Be careful with the id as it is substituted with subsequential calls to startForeground
    //https://stackoverflow.com/questions/17394115/multiple-call-to-startforeground
    private int mNotificationID = 99;

    private NotificationManager mNotificationManager;
    private Notification.Builder mNotificationBuilder;
    private Notification mNotification;
    private String mNotificationText;

    public ServiceNotificationManager(Context context, String channel_id, String channelName, int id, String text){
        if(!isNotificationChannelEnabled(context, channel_id)){
            createNotificationChannel(context, channel_id, channelName);
        }
        mNotificationText = text;
        mNotificationID = id;
        //CharSequence text = context.getText(R.string.local_service_started);
        // The PendingIntent to launch our activity if the user selects this notification
        Intent activIntent = new Intent(context.getApplicationContext(), MainActivity.class);
        activIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); //https://stackoverflow.com/questions/11551195/intent-from-notification-does-not-have-extras
        activIntent.putExtra("openFragment", "test");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotification = new NotificationCompat.Builder(context, channel_id)
                .setContentTitle(text)
                .setSmallIcon(R.drawable.ic_dashboard_white_24dp)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .build();
        TrackerLog.d("NotificationManager", "higher");

        mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
    }

    public Notification getNotification() {
        return mNotification;
    }

    public void notify(int pNotificationId) {
        mNotificationManager.notify(pNotificationId, mNotification);
    }

    //This is only if we want to use the same notification, but everytime we call startForeground what happens is that if we use the same ID the old service
    //associated witht this ID to this notification gets now subsittuted with the new ID
    public int getNotificationId() {
        return mNotificationID;
    }

    //IDK if it works
    public void cancelNotification(){
        // Cancel the persistent notification.
        //mNotificationManager.cancel(NOTIFICATION);
    }

    public void notify(int id, Notification notif){
        //mNotificationManager.notify();
    }

    public static void createNotificationChannel(Context context, String channelId, String channelName){
        TrackerLog.e("NotificationManager", "CREATING NOTIFICATION CHANNEL");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }

    public static boolean isNotificationChannelEnabled(Context context, @Nullable String channelId){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(!TextUtils.isEmpty(channelId)) {
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel channel = manager.getNotificationChannel(channelId);
                if(channel == null)
                    return false;
                return channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
            }
            return false;
        } else {
            return NotificationManagerCompat.from(context).areNotificationsEnabled();
        }
    }
}