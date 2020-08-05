package com.unimelb.marinig.bletracker.Callbacks;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.unimelb.marinig.bletracker.Interfaces.BeaconLocker;
import com.unimelb.marinig.bletracker.Interfaces.RadBeaconDotV11Locker;
import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;
import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord.GattStatus;

public class RadBeaconLockGattCallback extends RadBeaconGattCallback {
    protected final RadBeaconLockCallback callback;

    private BeaconLocker locker;

    public RadBeaconLockGattCallback(BluetoothDevice bluetoothDevice, ScannedDeviceRecord pBeacon, String param1String, RadBeaconLockCallback pBeaconLockCallback) {
        super(bluetoothDevice, param1String);
        this.callback = pBeaconLockCallback;
        this.beacon = pBeacon;
        this.beacon.setGattStatus(GattStatus.COMPLETED, false);
    }

    private void callbackIfComplete(BluetoothGatt bluetoothGatt) {
        if (this.beacon.hasGattStatus(GattStatus.COMPLETED)) {
            setCallbackStatus(RadBeaconGattCallback.CallbackStatus.COMPLETE);
            bluetoothGatt.close();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Beacon named '");
            stringBuilder.append(this.beacon.getName());
            stringBuilder.append("' locked");
            Log.d("RadBeaconManager", stringBuilder.toString());
            this.callback.onBeaconLocked(this.beacon, getOperationResult());
        }
    }

    public void handleServicesDiscovered() {
        Log.d("RadBeaconManager", "Services discovered for locking beacon " + this.beacon.getMac_beacon());
        this.locker = new RadBeaconDotV11Locker(this.gatt, this.beaconService, this, this.beaconPIN);
        if (this.locker != null)
            this.locker.lockBeacon(this.beacon);
    }

    public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int param1Int) {
        super.onCharacteristicRead(bluetoothGatt, bluetoothGattCharacteristic, param1Int);
        this.locker.onCharacteristicRead(bluetoothGatt, bluetoothGattCharacteristic, param1Int);
        callbackIfComplete(bluetoothGatt);
    }

    public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int param1Int) {
        super.onCharacteristicWrite(bluetoothGatt, bluetoothGattCharacteristic, param1Int);
        this.locker.onCharacteristicWrite(bluetoothGatt, bluetoothGattCharacteristic, param1Int);
    }
}