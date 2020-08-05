package com.unimelb.marinig.bletracker.Callbacks;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import android.telecom.Call;
import android.util.Log;

import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public abstract class RadBeaconGattCallback extends BluetoothGattCallback {
    private static final String LOG_TAG = "RadBeaconManager";

    protected ScannedDeviceRecord beacon;
    protected String beaconPIN;
    protected BluetoothGattService beaconService;
    protected CallbackStatus callbackStatus;
    protected Queue<BluetoothGattCharacteristic> characteristicQueue;
    protected final BluetoothDevice device;
    protected boolean discoveringServices;
    protected BluetoothGatt gatt;
    protected int operationResult;
    private BluetoothGattCharacteristic resultCharacteristic;
    private Handler mHandler;

    private class CallbackCancelRunnable implements Runnable{
        public final RadBeaconCallback callback;
        public CallbackCancelRunnable( final RadBeaconCallback callback){
            super();
            this.callback = callback;
        }

        public void run() {
            Log.d("RadBeaconManager", "Cancel operation timer triggered");
            if (RadBeaconGattCallback.this.getCallbackStatus() != RadBeaconGattCallback.CallbackStatus.COMPLETE) {
                Log.d("RadBeaconManager", "Canceling operation due to timeout");
                RadBeaconGattCallback.this.cancel();
                if (gatt != null)
                    gatt.close();

                callback.onBeaconConnectFailed(RadBeaconGattCallback.this.beacon);
            }
        }
    }
    private CallbackCancelRunnable mCancelRunnable;


    public RadBeaconGattCallback(BluetoothDevice paramBluetoothDevice) { this(paramBluetoothDevice, null); }

    public RadBeaconGattCallback(BluetoothDevice paramBluetoothDevice, String pBeaconPin) {
        this.device = paramBluetoothDevice;
        this.beaconPIN = pBeaconPin;
        this.beacon = null;
        this.gatt = null;
        this.beaconService = null;
        this.characteristicQueue = new LinkedList();
        this.discoveringServices = false;
        this.callbackStatus = CallbackStatus.INITIALIZED;
    }



    public void cancel() { this.callbackStatus = CallbackStatus.CANCELED; }

    public ScannedDeviceRecord getBeacon() { return this.beacon; }

    public CallbackStatus getCallbackStatus() { return this.callbackStatus; }

    public BluetoothDevice getDevice() { return this.device; }

    public int getOperationResult() { return this.operationResult; }

    protected abstract void handleServicesDiscovered();

    public boolean isCanceled() { return (this.callbackStatus == CallbackStatus.CANCELED); }

    public void onCharacteristicChanged(BluetoothGatt paramBluetoothGatt, BluetoothGattCharacteristic paramBluetoothGattCharacteristic) { super.onCharacteristicChanged(paramBluetoothGatt, paramBluetoothGattCharacteristic); }

    public void onCharacteristicRead(BluetoothGatt paramBluetoothGatt, BluetoothGattCharacteristic paramBluetoothGattCharacteristic, int paramInt) { super.onCharacteristicRead(paramBluetoothGatt, paramBluetoothGattCharacteristic, paramInt); }

    public void onCharacteristicWrite(BluetoothGatt paramBluetoothGatt, BluetoothGattCharacteristic paramBluetoothGattCharacteristic, int paramInt) { super.onCharacteristicWrite(paramBluetoothGatt, paramBluetoothGattCharacteristic, paramInt); }

    public void onConnectionStateChange(BluetoothGatt paramBluetoothGatt, int status, int newState) {
        super.onConnectionStateChange(paramBluetoothGatt, status, newState);

        String logStr = "Connection state changed to " +
                ((newState == BluetoothGatt.STATE_CONNECTED) ? "CONNECTED" : "DISCONNECTED") +
                " on status" + ((status == BluetoothGatt.GATT_SUCCESS) ? "SUCCESS" : Integer.toString(status)) +
                " for device address " + this.device.getAddress();
        Log.d("RadBeaconManager", logStr);

        if (newState == BluetoothGatt.STATE_CONNECTED && !this.discoveringServices) {
            this.discoveringServices = true;
            //this.beacon.setHardwareAddress(this.device.getAddress());
            this.gatt = paramBluetoothGatt;
            if (paramBluetoothGatt.discoverServices()) {
                Log.d("RadBeaconManager", "Discovering services for device " + this.device.getName() );
                return;
            }
            StringBuilder stringBuilder2 = new StringBuilder();
            Log.d("RadBeaconManager", "Failed discovering services for device " + this.device.getName() );
            return;
        }
        if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            this.discoveringServices = false;
            stopCancelTimer();
        }
    }

    public void onServicesDiscovered(BluetoothGatt pBluetoothGatt, int status) {
        super.onServicesDiscovered(pBluetoothGatt, status);
        Log.d("RadBeaconManager", "Services discovered for device " + this.device.getName());
        List<BluetoothGattService> servicesList= pBluetoothGatt.getServices();
        for(BluetoothGattService service : servicesList){
            UUID uUID = service.getUuid();
            Log.d("RadBeaconManager", "Service UUID :" +uUID.toString());
            if(uUID.equals(UUID.fromString(ScannedDeviceRecord.RADBEACON_DOT_V11_GATT_SERVICE_UUID))){
                this.beaconService = pBluetoothGatt.getService(uUID);
                handleServicesDiscovered();
                return;
            }
        }
        pBluetoothGatt.close();
    }

    public void setCallbackStatus(CallbackStatus paramCallbackStatus) { this.callbackStatus = paramCallbackStatus; }

    public void setOperationResult(int paramInt) { this.operationResult = paramInt; }

    public void startCancelTimer(int paramInt, final BluetoothGatt gatt, final RadBeaconCallback callback) {
        if(mHandler == null){
            mHandler = new Handler(Looper.getMainLooper());
        }
        if(mCancelRunnable != null)
            mHandler.removeCallbacks(mCancelRunnable);

        mCancelRunnable = new CallbackCancelRunnable(callback);
        Log.d("RadBeaconManager", "Cancel operation timer STARTED");
        mHandler.postDelayed(mCancelRunnable, TimeUnit.MILLISECONDS.convert(paramInt, TimeUnit.SECONDS));
    }

    public void stopCancelTimer(){
        if(mHandler != null && mCancelRunnable != null){
            Log.d("RadBeaconManager", "Cancel operation timer STOPPED");
            mHandler.removeCallbacks(mCancelRunnable);
        }
    }

    public enum CallbackStatus {
        CANCELED, COMPLETE, CONNECTED, CONNECTING, DISCONNECTED, DISCONNECTING, DISCOVERED_SERVICES, DISCOVERING_SERVICES, ERROR, INITIALIZED;
    }
}