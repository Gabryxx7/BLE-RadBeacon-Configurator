package com.unimelb.marinig.bletracker.Callbacks;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.unimelb.marinig.bletracker.Interfaces.BeaconPINUpdater;
import com.unimelb.marinig.bletracker.Interfaces.RadBeaconDotV11PINUpdater;
import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;
import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord.GattStatus;

public class RadBeaconUpdatePinGattCallback extends RadBeaconGattCallback {
    protected final RadBeaconUpdatePINCallback callback;

    protected final String newPin;

    private BeaconPINUpdater updater;

    public RadBeaconUpdatePinGattCallback(BluetoothDevice bluetoothDevice, ScannedDeviceRecord pBeacon, String param1String1, String param1String2, RadBeaconUpdatePINCallback pBeaconUpdatePinCallback) {
        super(bluetoothDevice, param1String1);
        this.callback = pBeaconUpdatePinCallback;
        this.beacon = pBeacon;
        this.newPin = param1String2;
        this.beacon.setGattStatus(GattStatus.COMPLETED,false);
    }

    private void callbackIfComplete(BluetoothGatt bluetoothGatt) {
        if (this.beacon.hasGattStatus(GattStatus.COMPLETED)) {
            setCallbackStatus(RadBeaconGattCallback.CallbackStatus.COMPLETE);
            bluetoothGatt.close();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Beacon named '");
            stringBuilder.append(this.beacon.getName());
            stringBuilder.append("' PIN updated");
            Log.d("RadBeaconManager", stringBuilder.toString());
            this.callback.onBeaconPinUpdated(this.beacon, getOperationResult());
        }
    }

    public void handleServicesDiscovered() {
        Log.d("RadBeaconManager", "Services discovered for updating PIN on beacon " + this.beacon.getMac_beacon());
        this.updater = new RadBeaconDotV11PINUpdater(this.gatt, this.beaconService, this, this.beaconPIN, this.newPin);
        if (this.updater != null)
            this.updater.updateBeaconPIN(this.beacon);
    }

    public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        super.onCharacteristicChanged(bluetoothGatt, bluetoothGattCharacteristic);
        this.updater.onCharacteristicChanged(bluetoothGatt, bluetoothGattCharacteristic);
        callbackIfComplete(bluetoothGatt);
    }

    public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int param1Int) {
        super.onCharacteristicRead(bluetoothGatt, bluetoothGattCharacteristic, param1Int);
        this.updater.onCharacteristicRead(bluetoothGatt, bluetoothGattCharacteristic, param1Int);
        callbackIfComplete(bluetoothGatt);
    }

    public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int param1Int) {
        super.onCharacteristicWrite(bluetoothGatt, bluetoothGattCharacteristic, param1Int);
        this.updater.onCharacteristicWrite(bluetoothGatt, bluetoothGattCharacteristic, param1Int);
    }
}