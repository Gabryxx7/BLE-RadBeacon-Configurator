package com.unimelb.marinig.bletracker.Callbacks;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import com.unimelb.marinig.bletracker.Interfaces.BeaconReader;
import com.unimelb.marinig.bletracker.Interfaces.RadBeaconDotV11Reader;
import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;
import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord.GattStatus;

public class RadBeaconReadGattCallback extends RadBeaconGattCallback {

    protected final RadBeaconReadCallback callback;

    protected BeaconReader reader;

    public RadBeaconReadGattCallback(BluetoothDevice bluetoothDevice, ScannedDeviceRecord pBeacon, RadBeaconReadCallback pBeaconReadCallback) {
        super(bluetoothDevice);
        this.callback = pBeaconReadCallback;
        this.beacon = pBeacon;
        this.reader = null;
    }

    private void callbackIfComplete(BluetoothGatt pBluetoothGatt) {
        if (this.beacon.hasGattStatus(GattStatus.COMPLETED)) {
            setCallbackStatus(RadBeaconGattCallback.CallbackStatus.COMPLETE);
            stopCancelTimer();
            pBluetoothGatt.disconnect();
            pBluetoothGatt.close();
            Log.d("RadBeaconManager", "Beacon named " + this.beacon.getName() + " found and populated with configuration");
            this.callback.onBeaconRead(this.beacon);
        }
    }

    public void handleServicesDiscovered() {
        Log.d("RadBeaconManager", "RadBeacon Services discovered for read on beacon " + this.beacon.getMac_beacon());
        this.reader = new RadBeaconDotV11Reader(this.gatt, this.beaconService, this);
        if (this.reader != null) {
            this.reader.readBeacon(this.beacon);
            return;
        }
        Log.d("RadBeaconManager", "Reader for beacon type not found");
    }

    public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int param1Int) {
        super.onCharacteristicRead(bluetoothGatt, bluetoothGattCharacteristic, param1Int);
        this.reader.onCharacteristicRead(bluetoothGatt, bluetoothGattCharacteristic, param1Int);
        callbackIfComplete(bluetoothGatt);
    }

    public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int param1Int) {
        super.onCharacteristicWrite(bluetoothGatt, bluetoothGattCharacteristic, param1Int);
        this.reader.onCharacteristicWrite(bluetoothGatt, bluetoothGattCharacteristic, param1Int);
    }

    public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int param1Int1, int param1Int2) {
        super.onConnectionStateChange(bluetoothGatt, param1Int1, param1Int2);
        if (param1Int2 == 0)
            this.callback.onBeaconConnectFailed(this.beacon);
    }

    public void onDescriptorWrite(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int param1Int) {
        Log.d("RadBeacon Manager", "Descriptor written");
        this.reader.readBeacon(this.beacon);
    }








}