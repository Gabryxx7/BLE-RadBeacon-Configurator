package com.unimelb.marinig.bletracker.Model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.unimelb.marinig.bletracker.Events.DeviceDetailsUpdatedEvent;
import com.unimelb.marinig.bletracker.Events.SettingsUpdatedEvent;
import com.unimelb.marinig.bletracker.Logger.TrackerLog;
import com.unimelb.marinig.bletracker.Utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

public class TrackerPreferences {
    static final String TAG = "TrackerPreferences";
    public static final String PACKAGE_NAME = "com.unimelb.marinig.bletracker";
    private SharedPreferences mPreferences;

    public static class ScanType{
        public static final int SCAN_CONTINUOUS = 0;
        public static final int SCAN_PERIOD_TIME_WAIT = 1;
    }

    public static class FileCloseCondition{
        public static final int TIME = 0;
        public static final int SIZE = 1;
        public static final int RECORDS = 2;
    }

    public static class DeployMode{
        public static final int DEBUG = 0;
        public static final int RELEASE = 1;
    }

    public static class Ordering{
        public static final int ORDER_NAME = 0;
        public static final int ORDER_RSSI = 1;
        public static final int ORDER_MAC = 2;
        public static final int ORDER_MAJOR = 3;
        public static final int ORDER_MINOR = 4;
        public static final int ORDER_TYPE = 5;
        public static final int ORDER_TIMESCAN = 6;
    }

    public static class Settings{
        public static final String VERSION = "VERSION";
        public static final String APP_VERSION = "APP_VERSION";
        public static final String SERVICES_WATCHDOG_ENABLE = "SERVICES_WATCHDOG_ENABLE";
        public static final String SERVICES_WATCHDOG_TIME = "SERVICES_WATCHDOG_TIME";
        public static final String ENABLE_LOG_VIEW ="ENABLE_LOG_VIEW";
        public static final String DEPLOY_MODE = "DEPLOY_MODE";
        public static final String SCAN_UUIDS = "SCAN_UUIDS";
        public static final String SCAN_EDDYSTONE_NAMESPACES = "SCAN_EDDYSTONE_NAMESPACES";
        public static final String FILTER_EDDYSTONE = "FILTER_EDDYSTONE";
        public static final String FILTER_PROJECT_BEACONS = "FILTER_PROJECT_BEACONS";
        public static final String FILTER_IBEACONS = "FILTER_IBEACONS";
        public static final String FILTER_CONFIGURABLE_RADBEACONS = "FILTER_CONFIGURABLE_RADBEACONS";
        public static final String RECORDS_STORE_LOCAL_DATA = "RECORDS_STORE_LOCAL_DATA";
        public static final String FILE_STORE_LOCAL = "FILE_STORE_LOCAL";
        public static final String SENSOR_STORE_LOCAL_DATA = "SENSOR_STORE_LOCAL_DATA";
        public static final String SCAN_PERIOD_TIME = "SCAN_PERIOD_TIME";
        public static final String SCAN_WAIT_TIME = "SCAN_WAIT_TIME";
        public static final String DEVICE_ID = "DEVICE_ID";
        public static final String MAX_BLE_RETRY = "MAX_BLE_RETRY";
        public static final String RECORDS_UPLOAD_CHUNK_SIZE = "RECORDS_UPLOAD_CHUNK_SIZE";
        public static final String TIME_BEFORE_UNAVAILABLE = "TIME_BEFORE_UNAVAILABLE";
        public static final String AVAILABILITY_CHECK_TIMER = "AVAILABILITY_CHECK_TIMER";
        public static final String SCAN_MODE = "SCAN_MODE";
        public static final String SCAN_MATCH_MODE = "SCAN_MATCH_MODE";
        public static final String SCAN_CALLBACK_TYPE = "SCAN_CALLBACK_TYPE";
        public static final String SCAN_NUM_MATCHES = "SCAN_NUM_MATCHES";
        public static final String SCAN_TYPE = "SCAN_TYPE";
        public static final String SCAN_ONLY_PROJECT_BEACONS = "SCAN_ONLY_PROJECT_BEACONS";
        public static final String SCAN_ANDROID7_TIMEOUT_RESTART = "SCAN_ANDROID7_TIMEOUT_RESTART";
        public static final String SERVER_NOTIFICATION_TIME = "SERVER_NOTIFICATION_TIME";
        public static final String NOTIFICATION_CHANNEL_ID = "NOTIFICATION_CHANNEL_ID";
        public static final String NOTIFICATION_CHANNEL_NAME = "NOTIFICATION_CHANNEL_NAME";
        public static final String NOTIFICATION_SERVER_CHANNEL_NAME = "NOTIFICATION_SERVER_CHANNEL_NAME";
        public static final String NOTIFICATION_SERVER_CHANNEL_ID = "NOTIFICATION_SERVER_CHANNEL_ID";
        public static final String VIEW_UPDATE_TIME = "VIEW_UPDATE_TIME";
        public static final String LAST_SETTINGS_UPDATE_TIME = "LAST_SETTINGS_UPDATE_TIME";
        public static final String LAST_SETTINGS_CHECK_TIME = "LAST_SETTINGS_CHECK_TIME";
        public static final String LAST_DEVICES_LIST_UPDATE_TIME = "LAST_DEVICES_LIST_UPDATE_TIME";

        //Critical settings
        public static final String INITIALIZED = "INITIALIZED";

        //Beacon config
        public static final String BEACON_CONFIG_UUID = "BEACON_CONFIG_UUID";
        public static final String BEACON_CONFIG_ADV_RATE = "BEACON_CONFIG_ADV_RATE";
        public static final String BEACON_CONFIG_TX_POWER_INDEX = "BEACON_CONFIG_TX_POWER_INDEX";
        public static final String BEACON_CONFIG_MAJOR = "BEACON_CONFIG_MAJOR";
        public static final String BEACON_CONFIG_DEFAULT_PIN = "BEACON_CONFIG_DEFAULT_PIN";
    }

    public TrackerPreferences(Context context){
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public SharedPreferences getSharedPreferences() {
        return mPreferences;
    }

    public String getString(String key){
        return mPreferences.getString(key, "");
    }

    public float getFloat(String key){
        return mPreferences.getFloat(key, 0.0f);
    }

    public long getLong(String key){
        return mPreferences.getLong(key, 0L);
    }

    public int getInt(String key){
        return mPreferences.getInt(key, -1);
    }

    public boolean getBool(String key){
        return mPreferences.getBoolean(key, false);
    }

    public float getVersion(){
        return mPreferences.getFloat(Settings.VERSION, -2.0f);
    }

    public void setVersion(double ver){
        set(Settings.VERSION, (float) ver);
    }

    public void setVersion(float ver){
        set(Settings.VERSION, ver);
    }


    public String getDeviceId(Context context){
        String id = mPreferences.getString(Settings.DEVICE_ID, "");
        if(id == "" || id.toLowerCase().contains("unknown")){
            id = Utils.getDeviceId(context.getApplicationContext());
            set(Settings.DEVICE_ID, id);
            return id;
        }
        return id;
    }

    public void set(String key, Long value){
        mPreferences.edit().putLong(key, value).apply();
    }

    public void set(String key, long value){
        mPreferences.edit().putLong(key, value).apply();
    }

    public void set(String key, String value){
        mPreferences.edit().putString(key, value).apply();
    }

    public void set(String key, int value){
        mPreferences.edit().putInt(key, value).apply();
    }

    public void set(String key, Set<String> value){
        mPreferences.edit().putStringSet(key, value).apply();
    }

    public void set(String key, float value){
        mPreferences.edit().putFloat(key, value).apply();
    }

    public void set(String key, boolean value){
        mPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean isDebug(){
        return mPreferences.getInt(Settings.DEPLOY_MODE, 0) == DeployMode.DEBUG;
    }

    public void updateSharedPreferences(JSONObject json){
        TrackerLog.e(TAG, "Preferences updateSharedPreferences");
        try {

            if(json.has(Settings.RECORDS_STORE_LOCAL_DATA)) {
                set(Settings.RECORDS_STORE_LOCAL_DATA, json.getBoolean(Settings.RECORDS_STORE_LOCAL_DATA));
            }

            if(json.has(Settings.SENSOR_STORE_LOCAL_DATA)) {
                set(Settings.SENSOR_STORE_LOCAL_DATA, json.getBoolean(Settings.SENSOR_STORE_LOCAL_DATA));
            }

            if(json.has(Settings.FILE_STORE_LOCAL)) {
                set(Settings.FILE_STORE_LOCAL, json.getBoolean(Settings.FILE_STORE_LOCAL));
            }

            if(json.has(Settings.SCAN_PERIOD_TIME)) {
                set(Settings.SCAN_PERIOD_TIME, json.getLong(Settings.SCAN_PERIOD_TIME));
            }

            if(json.has(Settings.SCAN_WAIT_TIME)) {
                set(Settings.SCAN_WAIT_TIME,json.getLong(Settings.SCAN_WAIT_TIME));
            }

            if(json.has(Settings.MAX_BLE_RETRY)) {
                set(Settings.MAX_BLE_RETRY, json.getInt(Settings.MAX_BLE_RETRY));
            }

            if(json.has(Settings.RECORDS_UPLOAD_CHUNK_SIZE)) {
                set(Settings.RECORDS_UPLOAD_CHUNK_SIZE, json.getInt(Settings.RECORDS_UPLOAD_CHUNK_SIZE));
            }

            if(json.has(Settings.TIME_BEFORE_UNAVAILABLE)) {
                set(Settings.TIME_BEFORE_UNAVAILABLE,json.getLong(Settings.TIME_BEFORE_UNAVAILABLE));
            }

            if(json.has(Settings.AVAILABILITY_CHECK_TIMER)) {
                set(Settings.AVAILABILITY_CHECK_TIMER,json.getLong(Settings.AVAILABILITY_CHECK_TIMER));
            }

            if(json.has(Settings.SCAN_MODE)) {
                set(Settings.SCAN_MODE, json.getInt(Settings.SCAN_MODE));
            }

            if(json.has(Settings.SCAN_MATCH_MODE)) {
                set(Settings.SCAN_MATCH_MODE, json.getInt(Settings.SCAN_MATCH_MODE));
            }

            if(json.has(Settings.SCAN_CALLBACK_TYPE)) {
                set(Settings.SCAN_CALLBACK_TYPE, json.getInt(Settings.SCAN_CALLBACK_TYPE));
            }

            if(json.has(Settings.SCAN_NUM_MATCHES)) {
                set(Settings.SCAN_NUM_MATCHES, json.getInt(Settings.SCAN_NUM_MATCHES));
            }

            if(json.has(Settings.SCAN_TYPE)) {
                set(Settings.SCAN_TYPE, json.getInt(Settings.SCAN_TYPE));
            }

            if(json.has(Settings.SCAN_ONLY_PROJECT_BEACONS)) {
                set(Settings.SCAN_ONLY_PROJECT_BEACONS, json.getBoolean(Settings.SCAN_ONLY_PROJECT_BEACONS));
            }

            if(json.has(Settings.SCAN_ANDROID7_TIMEOUT_RESTART)) {
                set(Settings.SCAN_ANDROID7_TIMEOUT_RESTART,json.getLong(Settings.SCAN_ANDROID7_TIMEOUT_RESTART));
            }

            if(json.has(Settings.SERVER_NOTIFICATION_TIME)) {
                set(Settings.SERVER_NOTIFICATION_TIME,json.getLong(Settings.SERVER_NOTIFICATION_TIME));
            }

            if(json.has(Settings.SERVER_NOTIFICATION_TIME)) {
                set(Settings.SERVER_NOTIFICATION_TIME,json.getLong(Settings.SERVER_NOTIFICATION_TIME));
            }

            if(json.has(Settings.SERVICES_WATCHDOG_ENABLE)) {
                set(Settings.SERVICES_WATCHDOG_ENABLE, json.getBoolean(Settings.SERVICES_WATCHDOG_ENABLE));
            }

            if(json.has(Settings.SERVICES_WATCHDOG_TIME)) {
                set(Settings.SERVICES_WATCHDOG_TIME,json.getLong(Settings.SERVICES_WATCHDOG_TIME));
            }

            if(json.has(Settings.FILTER_EDDYSTONE)) {
                set(Settings.FILTER_EDDYSTONE, json.getBoolean(Settings.FILTER_EDDYSTONE));
            }

            if(json.has(Settings.FILTER_IBEACONS)) {
                set(Settings.FILTER_IBEACONS, json.getBoolean(Settings.FILTER_IBEACONS));
            }

            if(json.has(Settings.FILTER_PROJECT_BEACONS)) {
                set(Settings.FILTER_PROJECT_BEACONS, json.getBoolean(Settings.FILTER_PROJECT_BEACONS));
            }


            if(json.has(Settings.BEACON_CONFIG_UUID)) {
                set(Settings.BEACON_CONFIG_UUID, json.getString(Settings.BEACON_CONFIG_UUID));
            }

            if(json.has(Settings.BEACON_CONFIG_ADV_RATE)) {
                set(Settings.BEACON_CONFIG_ADV_RATE, json.getInt(Settings.BEACON_CONFIG_ADV_RATE));
            }

            if(json.has(Settings.BEACON_CONFIG_MAJOR)) {
                set(Settings.BEACON_CONFIG_MAJOR, json.getInt(Settings.BEACON_CONFIG_MAJOR));
            }

            if(json.has(Settings.BEACON_CONFIG_TX_POWER_INDEX)) {
                set(Settings.BEACON_CONFIG_TX_POWER_INDEX, json.getInt(Settings.BEACON_CONFIG_TX_POWER_INDEX));
            }

            if(json.has(Settings.BEACON_CONFIG_DEFAULT_PIN)) {
                set(Settings.BEACON_CONFIG_DEFAULT_PIN, json.getString(Settings.BEACON_CONFIG_DEFAULT_PIN));
            }

            if(json.has("version")) {
                setVersion(json.getDouble("version"));
            }

            set(Settings.LAST_SETTINGS_UPDATE_TIME, Calendar.getInstance().getTimeInMillis());

            TrackerLog.d(TAG, "Tracker Preferences UPDATED");
            EventBus.getDefault().post(new SettingsUpdatedEvent());
            set(Settings.INITIALIZED, true);
        }
        catch (JSONException e){
            TrackerLog.d(TAG, "ERROR in updating Tracker Preferences " +e.getMessage());
        }
        TrackerLog.d(TAG, "Got new Settings json: " +json.toString());
    }

    public void setDefaultParam(){
        set(Settings.APP_VERSION, -1);
    }

    public void readSettingsJSON(Context context){
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset(context.getApplicationContext(), "trackersettings.json"));
            updateSharedPreferences(obj);
        } catch (JSONException e) {
            TrackerLog.e(TAG, "Error in initializing TrackerPreferences " +e.getMessage());
            e.printStackTrace();
        }
    }

    public void init(Context context, boolean clear){
        setDefaultParam();
//        if(getVersion() <= 0 || clear){
        //If I am requesting a clear then I'll clear them and read the json
        if(clear){
            TrackerLog.e(TAG, "Clearing preferences");
            mPreferences.edit().clear().commit();
            readSettingsJSON(context);
        } //If I am not requesting a clear, I'll still read the json if the settings are not initialized (first time)
        else if(!mPreferences.contains(Settings.INITIALIZED)){
            readSettingsJSON(context);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = dateFormat.format(new Date(getLong(Settings.LAST_SETTINGS_UPDATE_TIME)));
        TrackerLog.e(TAG, "Using settings v" +Float.toString(getVersion()) +" - Downloaded date: " +date);
    }

    public String loadJSONFromAsset(Context context, String jsonFileName) {
        String json = null;
        try {
            InputStream is = context.getApplicationContext().getAssets().open(jsonFileName);

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }
}
