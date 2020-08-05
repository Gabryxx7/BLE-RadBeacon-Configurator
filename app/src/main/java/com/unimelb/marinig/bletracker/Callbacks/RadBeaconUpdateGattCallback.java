package com.unimelb.marinig.bletracker.Callbacks;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.unimelb.marinig.bletracker.Interfaces.BeaconWriter;
import com.unimelb.marinig.bletracker.Interfaces.RadBeaconDotV11Writer;
import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;

public class RadBeaconUpdateGattCallback extends RadBeaconGattCallback {
    protected final RadBeaconUpdateCallback callback;

    protected BeaconWriter writer;
    protected ScannedDeviceRecord beaconInList;

    public RadBeaconUpdateGattCallback(BluetoothDevice bluetoothDevice, ScannedDeviceRecord pBeacon, ScannedDeviceRecord pBeaconInList, String pBeaconPin, RadBeaconUpdateCallback pBeaconUpdateCallback) {
        super(bluetoothDevice, pBeaconPin);
        this.callback = pBeaconUpdateCallback;
        this.beacon = pBeacon;
        this.beaconInList = pBeaconInList;
        this.beacon.setGattStatus(ScannedDeviceRecord.GattStatus.COMPLETED, false);
    }

    private void callbackIfComplete(BluetoothGatt bluetoothGatt) {
        if (this.beacon.hasGattStatus(ScannedDeviceRecord.GattStatus.COMPLETED)) {
            setCallbackStatus(RadBeaconGattCallback.CallbackStatus.COMPLETE);
            stopCancelTimer();
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            Log.d("RadBeaconManager", "Beacon named " + this.beacon.getName() + " updated configuration");
//            if (this.beacon.isDirectSlotAccessSupported() && getOperationResult() == 0)
//                this.beacon.resetSlotDataForWriting();
            if(beaconInList != null) {
                beaconInList.setRadName(this.beacon.getRadName());
                beaconInList.setAdvertisingRate(this.beacon.getAdvertisingRate());
                beaconInList.setAdvertisingInterval(this.beacon.getAdvertisingInterval());
                beaconInList.setTransmitPowerIndex(this.beacon.getRadBeaconTransmitPowerIndex());
                beaconInList.setName(this.beacon.getName());
                beaconInList.setBatteryLevel(this.beacon.getBatteryLevel());
            }
            this.callback.onBeaconUpdate(beaconInList, getOperationResult());
        }
    }

    public void handleServicesDiscovered() {
        Log.d("RadBeaconManager", "Services discovered for update on beacon " + this.beacon.getMac_beacon());
        this.writer = new RadBeaconDotV11Writer(this.gatt, this.beaconService, this, this.beaconPIN);
        if (this.writer != null) {
            this.writer.updateBeacon(this.beacon);
            return;
        }
    }

    public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        super.onCharacteristicChanged(bluetoothGatt, bluetoothGattCharacteristic);
        this.writer.onCharacteristicChanged(bluetoothGatt, bluetoothGattCharacteristic);
        callbackIfComplete(bluetoothGatt);
    }

    public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int param1Int) {
        super.onCharacteristicRead(bluetoothGatt, bluetoothGattCharacteristic, param1Int);
        this.writer.onCharacteristicRead(bluetoothGatt, bluetoothGattCharacteristic, param1Int);
        callbackIfComplete(bluetoothGatt);
    }

    public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int param1Int) {
        super.onCharacteristicWrite(bluetoothGatt, bluetoothGattCharacteristic, param1Int);
        this.writer.onCharacteristicWrite(bluetoothGatt, bluetoothGattCharacteristic, param1Int);
        callbackIfComplete(bluetoothGatt);
    }
}
