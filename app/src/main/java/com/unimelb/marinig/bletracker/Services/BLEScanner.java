package com.unimelb.marinig.bletracker.Services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.unimelb.marinig.bletracker.Callbacks.RadBeaconLockCallback;
import com.unimelb.marinig.bletracker.Callbacks.RadBeaconLockGattCallback;
import com.unimelb.marinig.bletracker.Callbacks.RadBeaconReadCallback;
import com.unimelb.marinig.bletracker.Callbacks.RadBeaconReadGattCallback;
import com.unimelb.marinig.bletracker.Callbacks.RadBeaconResetCallback;
import com.unimelb.marinig.bletracker.Callbacks.RadBeaconResetGattCallback;
import com.unimelb.marinig.bletracker.Callbacks.RadBeaconUpdateCallback;
import com.unimelb.marinig.bletracker.Callbacks.RadBeaconUpdateGattCallback;
import com.unimelb.marinig.bletracker.Callbacks.RadBeaconUpdatePINCallback;
import com.unimelb.marinig.bletracker.Callbacks.RadBeaconUpdatePinGattCallback;
import com.unimelb.marinig.bletracker.Events.BatchConfigurationUpdate;
import com.unimelb.marinig.bletracker.Events.ClearDeviceListEvent;
import com.unimelb.marinig.bletracker.Events.DeviceListFragmentEvent;
import com.unimelb.marinig.bletracker.Events.NewDeviceScannedEvent;
import com.unimelb.marinig.bletracker.Events.ScannerUpdateEvent;
import com.unimelb.marinig.bletracker.Events.ReadBeaconEvent;
import com.unimelb.marinig.bletracker.Events.RequestDeviceList;
import com.unimelb.marinig.bletracker.Events.ResetBleScannerEvent;
import com.unimelb.marinig.bletracker.Events.ScanStartEvent;
import com.unimelb.marinig.bletracker.Events.ScanStopEvent;
import com.unimelb.marinig.bletracker.Events.SettingsUpdatedEvent;
import com.unimelb.marinig.bletracker.Events.UpdateBeaconEvent;
import com.unimelb.marinig.bletracker.Events.UpdatedDeviceListAvailable;
import com.unimelb.marinig.bletracker.Events.UpdatedScannedDeviceEvent;
import com.unimelb.marinig.bletracker.Logger.TrackerLog;
import com.unimelb.marinig.bletracker.Model.BeaconConfig;
import com.unimelb.marinig.bletracker.Model.BeaconConfigurator;
import com.unimelb.marinig.bletracker.Model.BeaconConfigurator.BatchConfigState;
import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;
import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord.GattStatus;
import com.unimelb.marinig.bletracker.Model.TrackerPreferences;
import com.unimelb.marinig.bletracker.Model.TrackerPreferences.Settings;
import com.unimelb.marinig.bletracker.Utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;


//Beacons list: https://github.com/mozilla/connected-devices-experiments/issues/32
public class BLEScanner extends Service{
    private final String TAG = "BLE_SCANNER";

    public static final int NOTIFICATION_ID = 1103;
    private ServiceNotificationManager mNotification;
    public static volatile boolean isServiceRunning = false;
    public static volatile boolean mIsScanning = false;
    public static volatile boolean mNeedToRestart = true;

    private TrackerPreferences mPreferences;

    private static int RAD_GATT_ACTION_TIMEOUT_SECONDS = 60;

    private static int mSCAN_TYPE = -1;
    private static int mSCAN_MODE = -1;
    private static int mSCAN_MATCH_MODE = -1;
    private static int mSCAN_CALLBACK_TYPE = -1;
    private static int mSCAN_NUM_MATCHES = -1;

    private static boolean mIsListVisible;

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private int BLEScannerRestarts = 0;
    private long mScanCycle = 0;
    private long mScanStartTimestamp = 0l;
    private long mFirstCycleStartTime = 0l;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;

    private static Long mLastUpdateTime = -1L;

    public Long timestamp_scan_latest_global = 0L;


    private static volatile List<ScannedDeviceRecord> mDeviceList;
    private static volatile BeaconConfigurator mBeaconConfigurator;
    private static volatile HashMap<String, ScannedDeviceRecord> mConfiguredBeacons = new HashMap<>();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BatchConfigurationUpdate event){
        if(event.fromScanner){
            return;
        }
        Log.e("BATCHCONFIG", "RECEIVED BATCH config Event ");
        if(mBeaconConfigurator == null){
            if(mConfiguredBeacons == null){
                mConfiguredBeacons = new HashMap<>();
            }else {
                mConfiguredBeacons.clear();
            }
            mBeaconConfigurator = event.configurator;
            mBeaconConfigurator.start();
            EventBus.getDefault().post(new BatchConfigurationUpdate(true, mBeaconConfigurator));
        }
        else {
            mBeaconConfigurator = event.configurator;
        }

        if(!mBeaconConfigurator.hasState(BatchConfigState.STARTED)){
            mBeaconConfigurator.stop();
            mBeaconConfigurator = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ClearDeviceListEvent event){
        this.clearDeviceList();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SettingsUpdatedEvent event){
        this.updateBLESettings();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ResetBleScannerEvent event){
        this.restartBle();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DeviceListFragmentEvent event){
        this.mIsListVisible = event.mIsVisible;
        if(this.mIsListVisible){
            EventBus.getDefault().post(new ScannerUpdateEvent(mDeviceList, mIsScanning));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RequestDeviceList event){
        TrackerLog.e(TAG, "RequestDeviceList received");
        EventBus.getDefault().post(new UpdatedDeviceListAvailable(mDeviceList, event.sendTo));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ReadBeaconEvent event){
        readBeacon(event.beaconToRead);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UpdateBeaconEvent event){
        //If the beacon has been already update OR if the beacon has not been read yet, DO NOT UPDATE IT
        if(event.beaconToUpdate.hasGattStatus(GattStatus.READ)) {
            updateBeacon(event.beaconToUpdate, event.beaconPIN);
        }
    }

    private Runnable mStartAdapterRunnable = new Runnable() {
        @Override
        public void run() {
            TrackerLog.e(TAG, "Bluetooth adapter state: " + mBluetoothAdapter.getState());
            if (mBluetoothAdapter.isEnabled() || mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                if (mSCAN_TYPE == TrackerPreferences.ScanType.SCAN_CONTINUOUS) {
                    mHandler.post(mScanRunnable); //Start Scanning
                    mHandler.postDelayed(mRestartRunnable, mPreferences.getLong(Settings.SCAN_ANDROID7_TIMEOUT_RESTART)); //Restart the Scanner in n seconds
                } else {
                    mScanCycle = 0;
                    mHandler.post(mStartScanRunnable);
                }
            }
            else{
                mBluetoothAdapter.enable();
                TrackerLog.e(TAG, "Adapter OFF, retrying in 500ms");
                mHandler.postDelayed(mStartAdapterRunnable, 500);
            }
        }
    };


    private Runnable mScanRunnable = new Runnable() {
        @Override
        public void run() {
            scanBLE(true, ++mScanCycle, mScanCallback, mLeScanCallback);
            TrackerLog.d(TAG, "Runnable starting Continuous BLE SCAN");
        }
    };


    private Runnable mRestartRunnable= new Runnable() {
        @Override
        public void run() {
            restartBle();
        }
    };

    private Runnable mStartScanRunnable = new Runnable() {
        @Override
        public void run() {
            TrackerLog.d(TAG, "Runnable starting Period BLE SCAN");
            scanBLE(true, ++mScanCycle, mScanCallback, mLeScanCallback);
            mHandler.postDelayed(mStopScanRunnable, mPreferences.getLong(Settings.SCAN_PERIOD_TIME));
        }
    };

    private Runnable mStopScanRunnable = new Runnable() {
        @Override
        public void run() {
            TrackerLog.d(TAG, "Runnable stopping Wait BLE SCAN");
            scanBLE(false, mScanCycle, mScanCallback, mLeScanCallback);
            mHandler.postDelayed(mStartScanRunnable, mPreferences.getLong(Settings.SCAN_WAIT_TIME));
        }
    };

    //Scan callback for API < 21 (KitKat and older), callback for mBluetoothAdapter.startLeScan(leScanCallback)
    private final MyLeScanCallback mLeScanCallback = new MyLeScanCallback(this);
    public static class MyLeScanCallback implements BluetoothAdapter.LeScanCallback {
        private final WeakReference<BLEScanner> mBLEScanner;

        public MyLeScanCallback(BLEScanner bleScanner) {
            mBLEScanner = new WeakReference<BLEScanner>(bleScanner);
        }
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecordBytes) {
            TrackerLog.d("OLD_BLE_SCANNER", "ON LE SCAN");
            if(mBLEScanner.get() != null){
                if(!mBLEScanner.get().isServiceRunning) {
                    return;
                }
                mBLEScanner.get().mHandler.post(new ProcessScanResultRunnable(mBLEScanner.get(), device, rssi, scanRecordBytes, null));
            }
        }
    }


    //Scan callback for API >= 21 (Lollipop and newer), callback for mBluetoothLeScanner.startScan(scanCallback)
    private final MyScanCallback mScanCallback = new MyScanCallback(this);
    public static class MyScanCallback extends ScanCallback {
        private final WeakReference<BLEScanner> mBLEScanner;

        public MyScanCallback(BLEScanner bleScanner) {
            mBLEScanner = new WeakReference<BLEScanner>(bleScanner);
        }
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if(mBLEScanner.get() != null) {
                if (!mBLEScanner.get().isServiceRunning) {
                    return;
                }
                mBLEScanner.get().mHandler.post(new ProcessScanResultRunnable(mBLEScanner.get(), result.getDevice(), result.getRssi(), result.getScanRecord().getBytes(), result.getScanRecord()));
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            TrackerLog.d("NEW_BLE_SCANNER", "Got a new batch List<ScanResult>!");
//            //results.forEach(result -> insertScanResult(result)); //Damn Android, cannot use lambda expressions!
//            for(ScanResult result : results){
//                insertScanResult(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
//            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            TrackerLog.d("NEW_BLE_SCANNER", "Scan Failed! Error code: " + errorCode);
            if(errorCode == 2){
                mBLEScanner.get().mBluetoothAdapter.disable();
            }

            if(mBLEScanner.get().BLEScannerRestarts < mBLEScanner.get().mPreferences.getInt(Settings.MAX_BLE_RETRY)) {
                mBLEScanner.get().mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        TrackerLog.d(mBLEScanner.get().TAG, "Restarting BLE Scanner, tentative n: " + (++mBLEScanner.get().BLEScannerRestarts));
                        mBLEScanner.get().restartBle();
                    }
                }, 500);
            }
            else{
                TrackerLog.d(mBLEScanner.get().TAG, "MAX BLE retry tentatives");
            }
        }
    }


    private final MyGattCallback mGattCallback = new MyGattCallback(this);
    public static class MyGattCallback extends BluetoothGattCallback {
        private final WeakReference<BLEScanner> mBLEScanner;

        public MyGattCallback(BLEScanner bleScanner) {
            mBLEScanner = new WeakReference<BLEScanner>(bleScanner);
        }


    }

    public static final class ProcessScanResultRunnable implements Runnable {
        private final WeakReference<BLEScanner> mBLEScanner;
        private final BluetoothDevice mDevice;
        private final int mRssi;
        private final ScanRecord mScanRecord;
        private final byte[] mScanRecordBytes;

        public ProcessScanResultRunnable(BLEScanner blescanner, BluetoothDevice device, int rssi, byte[] scanRecordBytes, ScanRecord scanRecord) {
            mBLEScanner = new WeakReference<BLEScanner>(blescanner);
            mDevice = device;
            mRssi = rssi;
            mScanRecordBytes = scanRecordBytes;
            mScanRecord = scanRecord;
        }

        @Override
        public void run() {
             mBLEScanner.get().insertScanResult(mDevice, mRssi, mScanRecord, mScanRecordBytes);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isServiceRunning = false;

        mPreferences = new TrackerPreferences(this);
        mNotification = new ServiceNotificationManager(this, mPreferences.getString(TrackerPreferences.Settings.NOTIFICATION_CHANNEL_ID),
                mPreferences.getString(TrackerPreferences.Settings.NOTIFICATION_SERVER_CHANNEL_NAME),
                NOTIFICATION_ID,
                "BLEScanner");
        mDeviceList = new ArrayList<>();


        //https://hellsoft.se/bluetooth-low-energy-on-android-part-1-1aa8bf60717dl

    }

    private void scanBLE(final boolean enable, long scanCycle, ScanCallback scanCallback, BluetoothAdapter.LeScanCallback leScanCallback) {
        final boolean useNewerVersion = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        TrackerLog.d(TAG, "SCANBLE");
        if (enable) {
            //Either we clean the list or we set everything as unavailable
            //mDeviceList.clear();
            for(ScannedDeviceRecord device : mDeviceList){
                device.setAvailable(false);
            }
            if (useNewerVersion) {
                //TrackerLog.d("BLE_SCANNER", "Using newer BluetoothLeScanner");
                //mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
                ScanFilter.Builder builder = new ScanFilter.Builder();
                Vector<ScanFilter> filters = new Vector<ScanFilter>();
                filters.add(builder.build());
                ScanSettings.Builder builderScanSetting = new ScanSettings.Builder();
                builderScanSetting.setScanMode(mSCAN_MODE);
                builderScanSetting.setMatchMode(mSCAN_MATCH_MODE);
                builderScanSetting.setCallbackType(mSCAN_CALLBACK_TYPE);
                builderScanSetting.setNumOfMatches(mSCAN_NUM_MATCHES);

                builderScanSetting.setReportDelay(0);
                mBluetoothLeScanner.startScan(filters, builderScanSetting.build(), scanCallback);
            } else {
                //TrackerLog.d("BLE_SCANNER", "Using older BluetoothAdapter.startLeScan");
                mBluetoothAdapter.startLeScan(leScanCallback);
            }
            TrackerLog.d(TAG, "Start Scan: " + scanCycle);
            mScanStartTimestamp = Calendar.getInstance().getTimeInMillis();
            if(scanCycle <= 1)
                mFirstCycleStartTime = Calendar.getInstance().getTimeInMillis();
            BLEScannerRestarts = 0;
            mIsScanning = true;
            EventBus.getDefault().post(new ScanStartEvent(scanCycle, mFirstCycleStartTime, mScanStartTimestamp));
        } else {
            if (useNewerVersion) {
                if(mBluetoothAdapter.isEnabled() || mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    mBluetoothLeScanner.flushPendingScanResults(scanCallback);
                    mBluetoothLeScanner.stopScan(scanCallback);
                }
            } else {
                mBluetoothAdapter.stopLeScan(leScanCallback);
            }
            mIsScanning = false;
            EventBus.getDefault().post(new ScanStopEvent(scanCycle, mFirstCycleStartTime, mScanStartTimestamp, Calendar.getInstance().getTimeInMillis()));
            TrackerLog.d(TAG, "Stop Scan: " +scanCycle);
            mDeviceList.clear();
        }
    }

    private void insertScanResult(BluetoothDevice device, int rssi, ScanRecord scanRecord, byte[] scanRecordBytes){
        if(!isServiceRunning) {
            return;
        }
        ScannedDeviceRecord record = addNewItem(device, rssi, scanRecord, scanRecordBytes, timestamp_scan_latest_global);
        if (record != null) {
            if(record.isIbeacon() && record.isRadBeaconConfigurable && !record.hasGattStatus(GattStatus.READ) && !record.hasGattStatus(GattStatus.READING) && !record.hasGattStatus(GattStatus.UPDATING)) {
                Log.e(TAG, "READING Beacon " + record.getMinor());
                readBeacon(record);
            }
            //Batch config
//            Log.e("BATCHCONFIG", "Add Item, batch config: " + mBatchConfigState.toString());
            if(mBeaconConfigurator != null) {
                if (mBeaconConfigurator.hasState(BatchConfigState.STARTED) && !mBeaconConfigurator.hasState(BatchConfigState.WAITING) && !mBeaconConfigurator.hasState(BatchConfigState.PAUSED) && !mBeaconConfigurator.hasState(BatchConfigState.UPDATING)) {
                    if (record.isIbeacon() && record.isRadBeaconConfigurable && !mConfiguredBeacons.containsKey(record.getMac_beacon())) {
                        if (record.hasGattStatus(GattStatus.READ)) {  //NEVER UPDATE A NON_READ BEACON
                            Log.e("BATCHCONFIG", "Trying to configure Beacon " + record.getMinor());
                            ScannedDeviceRecord cloned = record.clone();
                            Log.e("BATCHCONFIG", "Need to ask " + mBeaconConfigurator.needToAsk());
                            if(!mBeaconConfigurator.needToAsk()) {
                                BeaconConfig config = mBeaconConfigurator.next(cloned.getMac_beacon());

                                if (config != null) {
                                    Integer advRate = config.getAdvRate();
                                    Integer txPowerIndex = config.getTxPowerIndex();
                                    Integer minor = config.getMinor();
                                    Log.e("BATCHCONFIG", record.getMinor() + ": Config = " + config + ", [" + record.getMinor() + ", " + ScannedDeviceRecord.DOT_TRANSMIT_POWER_VALUES[record.getRadBeaconTransmitPowerIndex()] + ", " + record.getAdvertisingRate() + "]");
                                    if (advRate != null) {
                                        cloned.setAdvertisingRate((byte) ((int) advRate));
                                    }
                                    if (txPowerIndex != null) {
                                        cloned.setTransmitPowerIndex((byte) ((int) txPowerIndex));
                                    }
                                    if (minor != null) {
                                        cloned.setMinor((int) minor);
                                        cloned.setRadName("RBDot" + cloned.getMinor());
                                        cloned.setName("RBDot" + cloned.getMinor());
                                    }
                                    cloned.setUUIDString(mPreferences.getString(TrackerPreferences.Settings.BEACON_CONFIG_UUID));

                                    mBeaconConfigurator.addState(BatchConfigState.UPDATING);
                                    Log.e("BATCHCONFIG", "Adding state UPDATING");
                                    EventBus.getDefault().post(new BatchConfigurationUpdate(true, mBeaconConfigurator, cloned));
                                    mConfiguredBeacons.put(record.getMac_beacon(),record);
                                    updateBeacon(cloned, record, mPreferences.getString(Settings.BEACON_CONFIG_DEFAULT_PIN), new RadBeaconUpdateCallback() {
                                        private void setResultsWithUpdatedBeacon(ScannedDeviceRecord beacon) {
                                            beacon.isBatchUpdated = true;
                                            EventBus.getDefault().post(new BatchConfigurationUpdate(true, mBeaconConfigurator, beacon));
                                            EventBus.getDefault().post(new UpdatedScannedDeviceEvent(beacon, true));
                                        }

                                        public void onBeaconConnectFailed(ScannedDeviceRecord beacon) {
                                            super.onBeaconConnectFailed(beacon);
                                            Log.e("BATCHCONFIG", "Batch config for beacon: " + beacon.getMinor() + " FAILED");
                                            mBeaconConfigurator.removeState(BatchConfigState.UPDATING);
                                            Log.e("BATCHCONFIG", "Removing state UPDATING");
                                            beacon.removeGattStatus(GattStatus.UPDATING);
                                            TrackerLog.e(TAG, "Beacon " + beacon.getMinor() + " Update FAILED");
                                            mConfiguredBeacons.remove(beacon.getMac_beacon()); //retry
                                            EventBus.getDefault().post(new BatchConfigurationUpdate(true, mBeaconConfigurator));
                                        }

                                        public void onBeaconUpdate(ScannedDeviceRecord beacon, int beaconPIN) {
                                            super.onBeaconUpdate(beacon, beaconPIN);
                                            Log.e("BATCHCONFIG", "Batch config for beacon: " + beacon.getMinor() + " COMPLETED");
                                            mBeaconConfigurator.addCompletedConfig(beacon, config);
                                            mBeaconConfigurator.removeState(BatchConfigState.UPDATING);
                                            Log.e("BATCHCONFIG", "Removing state UPDATING");
                                            beacon.removeGattStatus(GattStatus.UPDATING);
                                            beacon.addGattStatus(GattStatus.UPDATED);
                                            TrackerLog.e(TAG, "Beacon " + beacon.getMinor() + " Update SUCCEDED: " + beacon.getAdvertisingInterval() + " " + beacon.getAdvertisingRate());
                                            Log.d("RadBeaconManager", "Beacon " + beacon.getMajor() + "." + beacon.getMinor() + " Update SUCCEDED: " + ScannedDeviceRecord.DOT_TRANSMIT_POWER_VALUES[beacon.getRadBeaconTransmitPowerIndex()] + " " + beacon.getAdvertisingRate());

                                            setResultsWithUpdatedBeacon(beacon);
                                        }
                                    });
                                }
                            }
                            else{
                                mBeaconConfigurator.addState(BatchConfigState.WAITING);
                                Log.e("BATCHCONFIG", "WAITING!");
                                EventBus.getDefault().post(new BatchConfigurationUpdate(true, mBeaconConfigurator, record));
                            }
                        } else {
                            if (!mConfiguredBeacons.containsKey(record.getMac_beacon()) && !record.hasGattStatus(GattStatus.READ) && !record.hasGattStatus(GattStatus.READING) && !record.hasGattStatus(GattStatus.UPDATING)) {
                                Log.e("BATCHCONFIG", "READING Beacon " + record.getMinor());
                                readBeacon(record);
                            }
                        }
                    } else if (mConfiguredBeacons != null && mConfiguredBeacons.containsKey(record.getMac_beacon())) {
//                        Log.e("BATCHCONFIG", "Beacon already configured");
                    }
                } else {
                    if (mBeaconConfigurator.hasState(BatchConfigState.UPDATING)) {
//                        Log.e("BATCHCONFIG", "Batch config is UPDATING, waiting...");
                    } else if (mBeaconConfigurator.hasState(BatchConfigState.PAUSED)) {
//                        Log.e("BATCHCONFIG", "Batch config is PAUSED, waiting...");
                    }
                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(intent != null && intent.hasExtra("stop")){
            TrackerLog.e(TAG, "Stopping intent received");
            mNeedToRestart = false;
            stopSelf();
        }
        else {
            startScanner();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if(mNeedToRestart) {
            Intent broadcastIntent = new Intent("com.unimelb.marinig.bletracker.BroadcastMessage.RestartServices");
            sendBroadcast(broadcastIntent);
            TrackerLog.e(TAG, "Restart broadcast sent");
        }
        mNeedToRestart = true;
        stopScanner();
        TrackerLog.e(TAG, "BLEScanner Destroyed");
        //BLETrackerApplication.getRefWatcher(this).watch(this);;
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        TrackerLog.e(TAG, "BLEScanner task removed");
        //stopScanner();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onLowMemory() {
        TrackerLog.e(TAG, "BLEScanner Low memory");
        //stopScanner();
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        TrackerLog.e(TAG, "BLEScanner Trim memory");
        //stopScanner();
        super.onTrimMemory(level);
    }

    public void startScanner(){
        if(!isServiceRunning) {
            mNeedToRestart = true;
            TrackerLog.d(TAG, "BLEScanner ServiceContent started");
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
            mHandlerThread = new HandlerThread("BLE_BleScannerThread");
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper());
            mHandler.removeCallbacks(mScanRunnable);
            mHandler.removeCallbacks(mRestartRunnable);
            mHandler.removeCallbacks(mStartScanRunnable);
            mHandler.removeCallbacks(mStopScanRunnable);
            isServiceRunning = true;
            mSCAN_TYPE = mPreferences.getInt(Settings.SCAN_TYPE);
            mSCAN_MODE = mPreferences.getInt(Settings.SCAN_MODE);
            mSCAN_MATCH_MODE = mPreferences.getInt(Settings.SCAN_MATCH_MODE);
            mSCAN_CALLBACK_TYPE = mPreferences.getInt(Settings.SCAN_CALLBACK_TYPE);
            mSCAN_NUM_MATCHES = mPreferences.getInt(Settings.SCAN_NUM_MATCHES);

            //The BLE scan service was one of the main reasons I made all of this stuff!
            //I wanted to start or stop it whenever I wanted, and here we go! Since we don't pass it Context, nor Intent
            //I needed a custom implementation to start or stop the service!
            mBluetoothAdapter = ((BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

            /*if (!mBluetoothAdapter.isEnabled() || mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF || mBluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON) {
                Log.e(TAG, "BLUETOOTH OFF");
                mBluetoothAdapter.enable();
            }*/

            //In case the adapter is not on yet
            mHandler.post(mStartAdapterRunnable);

            startForeground(mNotification.getNotificationId(), mNotification.getNotification());
        }
    }

    public void stopScanner(){
        if(isServiceRunning) {
            isServiceRunning = false;
            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this);
            }
            mHandler.removeCallbacks(mScanRunnable);
            mHandler.removeCallbacks(mRestartRunnable);
            mHandler.removeCallbacks(mStartScanRunnable);
            mHandler.removeCallbacks(mStopScanRunnable);
            mHandlerThread.interrupt();
            mHandlerThread.quitSafely();
            scanBLE(false, mScanCycle, mScanCallback, mLeScanCallback);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public ScannedDeviceRecord addNewItem(BluetoothDevice device, int rssi, ScanRecord scanRecord, byte[] scanRecordBytes, Long timeStamp_scan_start_global) {
        ScannedDeviceRecord record = null;
        if(mDeviceList == null){
            mDeviceList = new ArrayList<>();
        }

        //Check if the device was already in the list and in case, update it
        for(int i = 0; i < mDeviceList.size(); i++) {
            if(mDeviceList.get(i).getMac_beacon().equals(device.getAddress())) {
            //Old version that was also checking the device type in case of multiple eddystones
            //if(mDeviceList.get(i).getMac_beacon().equals(device.getAddress()) && mDeviceList.get(i).getDeviceType() == Utils.getFrameType(scanRecordBytes)) {
                mDeviceList.get(i).updateDeviceData(device.getName(), rssi, scanRecord, scanRecordBytes, timestamp_scan_latest_global);
                mDeviceList.get(i).setAvailable(true);
                record = mDeviceList.get(i);

                if(mIsListVisible){
                    EventBus.getDefault().post(new UpdatedScannedDeviceEvent(record));
                }
                //mDeviceList.sort(mComparator);
                return record;
            }
        }

        //If the device WAS NOT in the list, create a new one
        //Then check if

        record = new ScannedDeviceRecord(device, rssi, scanRecord, scanRecordBytes, mPreferences.getDeviceId(this), timestamp_scan_latest_global);
        boolean addToList = false;
        //If the filter is enabled, then check the criteria and if they match, add it to the list
        if(mPreferences.getBool(Settings.SCAN_ONLY_PROJECT_BEACONS)){
            if(record.isIbeacon()){ //CAREFUL!! REMOVE THIS WHEN NOT NEEDED USED ONLY FOR CONFIGURATION
//            if(record.isIbeacon() && Utils.isUUIDInList(record.getUUID(), mPreferences.getString(Settings.SCAN_UUIDS))){
                addToList = true;
            }
        }
        else{ //If the filter is disabled, just add it to the list
            addToList = true;
        }

        if(addToList){
            mDeviceList.add(record);
            if (mIsListVisible) {
                EventBus.getDefault().post(new NewDeviceScannedEvent(record));
            }
            return record;
        }
        return null;
        //mDeviceList.sort(mComparator);
    }


    public void exportDeltas(Boolean isDelta_csv_isBeacon){
        Utils.exportDeltasCsv(mDeviceList, getApplicationContext(), isDelta_csv_isBeacon);
    }

    public void clearDeviceList() {
        mDeviceList.clear();
    }

    public void updateBLESettings(){
        boolean needToRestart = false;

        if(mSCAN_TYPE != mPreferences.getInt(Settings.SCAN_TYPE)){
            mSCAN_TYPE = mPreferences.getInt(Settings.SCAN_TYPE);
            needToRestart = true;
        }

        if(mSCAN_CALLBACK_TYPE != mPreferences.getInt(Settings.SCAN_CALLBACK_TYPE)){
            mSCAN_CALLBACK_TYPE = mPreferences.getInt(Settings.SCAN_CALLBACK_TYPE);
            needToRestart = true;
        }

        if(mSCAN_MATCH_MODE != mPreferences.getInt(Settings.SCAN_MATCH_MODE)){
            mSCAN_MATCH_MODE = mPreferences.getInt(Settings.SCAN_MATCH_MODE);
            needToRestart = true;
        }

        if(mSCAN_MODE != mPreferences.getInt(Settings.SCAN_MODE)){
            mSCAN_MODE = mPreferences.getInt(Settings.SCAN_MODE);
            needToRestart = true;
        }

        if(mSCAN_NUM_MATCHES != mPreferences.getInt(Settings.SCAN_NUM_MATCHES)){
            mSCAN_NUM_MATCHES = mPreferences.getInt(Settings.SCAN_NUM_MATCHES);
            needToRestart = true;
        }

        if(needToRestart){
            TrackerLog.e(TAG, "NEED TO RESTART, restarting BLEScanner");
            this.restartBle();
        }
    }

    private void restartBle(){
        TrackerLog.d(TAG, "Restarting BLEScanner");

        mHandler.removeCallbacks(mScanRunnable);
        mHandler.removeCallbacks(mRestartRunnable);
        mHandler.removeCallbacks(mStartScanRunnable);
        mHandler.removeCallbacks(mStopScanRunnable);
        scanBLE(false, mScanCycle, mScanCallback, mLeScanCallback);

        mSCAN_TYPE = mPreferences.getInt(Settings.SCAN_TYPE);
        mSCAN_MODE = mPreferences.getInt(Settings.SCAN_MODE);
        mSCAN_MATCH_MODE = mPreferences.getInt(Settings.SCAN_MATCH_MODE);
        mSCAN_CALLBACK_TYPE = mPreferences.getInt(Settings.SCAN_CALLBACK_TYPE);
        mSCAN_NUM_MATCHES = mPreferences.getInt(Settings.SCAN_NUM_MATCHES);

        mHandler.post(mStartAdapterRunnable);
    }



    public void lockBeacon(ScannedDeviceRecord beacon, String paramString, RadBeaconLockCallback beaconLockCallback) {
        BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(beacon.getMac_beacon());
        RadBeaconLockGattCallback radBeaconLockGattCallback = new RadBeaconLockGattCallback(bluetoothDevice, beacon, paramString, beaconLockCallback);
        BluetoothGatt bluetoothGatt = bluetoothDevice.connectGatt(getApplicationContext(), true, radBeaconLockGattCallback);
        radBeaconLockGattCallback.startCancelTimer(RAD_GATT_ACTION_TIMEOUT_SECONDS, bluetoothGatt, beaconLockCallback);
    }

    public void readBeacon(ScannedDeviceRecord beacon){
        readBeacon(beacon, new RadBeaconReadCallback() {
            public void onBeaconConnectFailed(ScannedDeviceRecord beacon) {
                super.onBeaconConnectFailed(beacon);
                beacon.removeGattStatus(GattStatus.READING);
                TrackerLog.e(TAG, "Beacon " + beacon.getMinor() + " Read FAILED");
                Log.e("BATCHCONFIG", "READ FAILED " + BeaconConfigurator.beaconToString(beacon));
                EventBus.getDefault().post(new UpdatedScannedDeviceEvent(beacon, true));
            }

            public void onBeaconRead(ScannedDeviceRecord beacon) {
                super.onBeaconRead(beacon);
                beacon.removeGattStatus(GattStatus.READING);
                beacon.addGattStatus(GattStatus.READ);
                TrackerLog.e(TAG, "Beacon " + beacon.getMinor() + " Read SUCCEDED: " +beacon.getAdvertisingInterval() + " " +beacon.getAdvertisingRate());
                Log.e("BATCHCONFIG", "READ COMPLETED " + BeaconConfigurator.beaconToString(beacon));
                Log.d("RadBeaconManager", "Beacon " + beacon.getMinor() + " Read SUCCEDED: " +ScannedDeviceRecord.DOT_TRANSMIT_POWER_VALUES[beacon.getRadBeaconTransmitPowerIndex()] + " " +beacon.getAdvertisingRate());
                EventBus.getDefault().post(new UpdatedScannedDeviceEvent(beacon, true));
                //Trying to update it with the new values
//                beacon.setMajor(3);
//                if(!beacon.isGattUpdated() && beacon.isGattRead()) {
//                    mHandler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            EventBus.getDefault().post(new UpdateBeaconEvent(beacon, "00000000"));
//                        }
//                    },1000);
//                }
            }
        });
    }

    public void readBeacon(ScannedDeviceRecord beacon, RadBeaconReadCallback beaconReadCallback) {
        Log.e("BATCHCONFIG", "READING " + BeaconConfigurator.beaconToString(beacon));
        beacon.removeGattStatus(GattStatus.COMPLETED);
        beacon.addGattStatus(GattStatus.READING);
        EventBus.getDefault().post(new UpdatedScannedDeviceEvent(beacon, true));
        RadBeaconReadGattCallback radBeaconReadGattCallback = new RadBeaconReadGattCallback(beacon.device, beacon, beaconReadCallback);
        BluetoothGatt bluetoothGatt = beacon.device.connectGatt(getApplicationContext(), false, radBeaconReadGattCallback);
        radBeaconReadGattCallback.startCancelTimer(RAD_GATT_ACTION_TIMEOUT_SECONDS, bluetoothGatt, beaconReadCallback);
    }

    public void resetBeacon(ScannedDeviceRecord beacon, String paramString, RadBeaconResetCallback beaconResetCallback) {
        BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(beacon.getMac_beacon());
        RadBeaconResetGattCallback radBeaconResetGattCallback = new RadBeaconResetGattCallback(bluetoothDevice, beacon, paramString, beaconResetCallback);
        BluetoothGatt bluetoothGatt = bluetoothDevice.connectGatt(getApplicationContext(), true, radBeaconResetGattCallback);
        radBeaconResetGattCallback.startCancelTimer(RAD_GATT_ACTION_TIMEOUT_SECONDS, bluetoothGatt, beaconResetCallback);
    }

    public void updateBeacon(ScannedDeviceRecord beacon, String pBeaconPin){
        //Don't forget to CLONE, otherwise the phone might read again the beacon and will override the updated details
        //You need a copy that WILL NOT be modified while in this thread
        updateBeacon(beacon.clone(), beacon, pBeaconPin, new RadBeaconUpdateCallback() {
            private void setResultsWithUpdatedBeacon(ScannedDeviceRecord beacon) {
                EventBus.getDefault().post(new UpdatedScannedDeviceEvent(beacon, true));
                //Do Something with the new result
            }

            public void onBeaconConnectFailed(ScannedDeviceRecord beacon) {
                super.onBeaconConnectFailed(beacon);
                beacon.removeGattStatus(GattStatus.UPDATING);
                TrackerLog.e(TAG, "Beacon " + beacon.getMinor() + " Update FAILED");
                EventBus.getDefault().post(new UpdatedScannedDeviceEvent(beacon, true));
            }

            public void onBeaconUpdate(ScannedDeviceRecord beacon, int beaconPIN) {
                super.onBeaconUpdate(beacon, beaconPIN);
                beacon.removeGattStatus(GattStatus.UPDATING);
                beacon.addGattStatus(GattStatus.UPDATED);
//                    beacon.setGattRead(true);
                TrackerLog.e(TAG, "Beacon " + beacon.getMinor() + " Update SUCCEDED: " + beacon.getAdvertisingInterval() + " " + beacon.getAdvertisingRate());
                Log.d("RadBeaconManager", "Beacon " + beacon.getMajor() + "." + beacon.getMinor() + " Update SUCCEDED: " + ScannedDeviceRecord.DOT_TRANSMIT_POWER_VALUES[beacon.getRadBeaconTransmitPowerIndex()] + " " + beacon.getAdvertisingRate());
                setResultsWithUpdatedBeacon(beacon);
            }
        });

    }

    public void updateBeacon(ScannedDeviceRecord pRadBeacon, ScannedDeviceRecord pBeaconInlist, String pBeaconPin, RadBeaconUpdateCallback pRadBeaconUpdateCallback) {
        pBeaconInlist.addGattStatus(GattStatus.UPDATING);
        EventBus.getDefault().post(new UpdatedScannedDeviceEvent(pBeaconInlist, true));
        RadBeaconUpdateGattCallback radBeaconUpdateGattCallback = new RadBeaconUpdateGattCallback(pRadBeacon.device, pRadBeacon, pBeaconInlist, pBeaconPin, pRadBeaconUpdateCallback);
        BluetoothGatt bluetoothGatt = pRadBeacon.device.connectGatt(getApplicationContext(), false, radBeaconUpdateGattCallback);
        radBeaconUpdateGattCallback.startCancelTimer(RAD_GATT_ACTION_TIMEOUT_SECONDS, bluetoothGatt, pRadBeaconUpdateCallback);
    }

    public void updateBeaconPin(ScannedDeviceRecord pRadBeacon, String pOldPin, String pNewPin, RadBeaconUpdatePINCallback pRadBeaconUpdatePinCallback) {
        BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(pRadBeacon.getMac_beacon());
        RadBeaconUpdatePinGattCallback radBeaconUpdatePinGattCallback = new RadBeaconUpdatePinGattCallback(bluetoothDevice, pRadBeacon, pOldPin, pNewPin, pRadBeaconUpdatePinCallback);
        BluetoothGatt bluetoothGatt = bluetoothDevice.connectGatt(getApplicationContext(), false, radBeaconUpdatePinGattCallback);
        radBeaconUpdatePinGattCallback.startCancelTimer(RAD_GATT_ACTION_TIMEOUT_SECONDS, bluetoothGatt, pRadBeaconUpdatePinCallback);
    }


    /***
     * used for updating bluetooth device's UI.
     * If scans new ibeacon device, add it to list.
     * If scans existed ibeacon, update its status, including rssi, accuracy distance and scan timestamp.
     * @param btDevice
     */
//    public void updateViewItem_ibeacon (ScannedDeviceRecord btDevice) {
//        for (int i = 0; i < ibeaconList.size(); i++) {
//            if (ibeaconList.get(i).getMac_beacon().equals(btDevice.getMac_beacon())) {
//                ibeaconList.get(i).setDeviceStatus(btDevice.getRSSI(), btDevice.getAccuracy(), btDevice.getDistance(), btDevice.getTimestamp_scan());
//                TrackerLog.d(TAG, "Exist device scanned, device [Minor|RSSI]: [" + btDevice.getMinor()
//                        + "|" + btDevice.getRSSI() + "]");
//                updateDeviceList();
//                return;
//            }
//        }
//        TrackerLog.d(TAG, "New device scanned, device [Minor|RSSI]: [" + btDevice.getMinor()
//                + "|" + btDevice.getRSSI() + "]");
//        TrackerLog.d(TAG, "New device scanned, total: " + (ibeaconList.size()+1));
//        ibeaconList.add(btDevice);
//
//        updateDeviceList();
//        // TODO if not in filter mode, clear or not ?
//    }

    /***
     * used for updating bluetooth device's UI.
     * If in filter mode, the bleList will be clear.
     * If scans new ble device, add it to list.
     * If scans existed ble device, update its status, including rssi, accuracy distance and scan timestamp.
     * @param btDevice
     */
//    public void updateViewItem_ble(ScannedDeviceRecord btDevice) {
//        if (!mDeviceListFragment.isFilterSwitchChecked()) {
//            for (int i = 0; i < bleList.size(); i++) {
//                if (bleList.get(i).getMac_beacon().equals(btDevice.getMac_beacon())) {
//                    return;
//                }
//            }
//            bleList.add(btDevice);
//            updateDeviceList();
//        }
//        else {
////            TODO clear or not ?
//            bleList.clear();
//        }
//    }
}
