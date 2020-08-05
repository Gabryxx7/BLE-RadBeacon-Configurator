package com.unimelb.marinig.bletracker.Interfaces;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;

import com.unimelb.marinig.bletracker.Callbacks.RadBeaconLockGattCallback;
import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;

public class RadBeaconDotV11Locker extends RadBeaconDotV11ActionWithPIN implements BeaconLocker {
    public RadBeaconDotV11Locker(BluetoothGatt bluetoothGatt, BluetoothGattService bluetoothGattService, RadBeaconLockGattCallback pBeaconLockGattCallback, String param1String) {
        super(bluetoothGatt, bluetoothGattService, pBeaconLockGattCallback, param1String, (byte)5);
    }

    public void lockBeacon(ScannedDeviceRecord pBeacon) {
        this.beacon = pBeacon;
        writeNextCharacteristicInQueue();
    }
}