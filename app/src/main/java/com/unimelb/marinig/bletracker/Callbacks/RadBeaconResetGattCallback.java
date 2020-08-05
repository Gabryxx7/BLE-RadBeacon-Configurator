package com.unimelb.marinig.bletracker.Callbacks;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.unimelb.marinig.bletracker.Interfaces.BeaconResetter;
import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;
import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord.GattStatus;

public class RadBeaconResetGattCallback extends RadBeaconGattCallback {
    protected final RadBeaconResetCallback callback;

    private BeaconResetter resetter;

    public RadBeaconResetGattCallback(BluetoothDevice bluetoothDevice, ScannedDeviceRecord pBeacon, String param1String, RadBeaconResetCallback pBeaconResetCallback) {
        super(bluetoothDevice, param1String);
        this.callback = pBeaconResetCallback;
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
            stringBuilder.append("' reset");
            Log.d("RadBeaconManager", stringBuilder.toString());
            this.callback.onBeaconReset(this.beacon, getOperationResult());
        }
    }

    public void handleServicesDiscovered() {
        Log.d("RadBeaconManager", "Services discovered for reset on beacon " + this.beacon.getMac_beacon());
//        Log.d("RadBeaconManager", stringBuilder.toString());
//        int i = RadBeaconManager.null.$SwitchMap$com$radiusnetworks$radbeaconkit$RadBeacon$RadBeaconType[this.beacon.getRadBeaconType().ordinal()];
//        if (i != 10) {
//            switch (i) {
//                case 6:
//                    this.resetter = new RadBeaconManager.RadBeaconDotResetter(RadBeaconManager.this, this.gatt, this.beaconService, this, this.beaconPIN);
//                    break;
//                case 4:
//                case 5:
//                    this.resetter = new RadBeaconManager.KSTBeaconResetter(RadBeaconManager.this, this.gatt, this.beaconService, this, this.beaconPIN);
//                    break;
//                case 3:
//                    this.resetter = new RadBeaconManager.RadBeaconV32Resetter(RadBeaconManager.this, this.gatt, this.beaconService, this, this.beaconPIN);
//                    break;
//                case 1:
//                case 2:
//                    this.resetter = new RadBeaconManager.RadBeaconResetter(RadBeaconManager.this, this.gatt, this.beaconService, this, this.beaconPIN);
//                    break;
//            }
//        } else {
//            this.resetter = new RadBeaconManager.RadBeaconX4SResetter(RadBeaconManager.this, this.gatt, this.beaconService, this, this.beaconPIN);
//        }
//        if (this.resetter != null)
//            this.resetter.resetBeacon(this.beacon);
    }

    public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int param1Int) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Characteristic read: ");
        stringBuilder.append(bluetoothGattCharacteristic.getUuid().toString());
        Log.d("RadBeacon", stringBuilder.toString());
        super.onCharacteristicRead(bluetoothGatt, bluetoothGattCharacteristic, param1Int);
        this.resetter.onCharacteristicRead(bluetoothGatt, bluetoothGattCharacteristic, param1Int);
        callbackIfComplete(bluetoothGatt);
    }

    public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int param1Int) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Characteristic written: ");
        stringBuilder.append(bluetoothGattCharacteristic.getUuid().toString());
        Log.d("RadBeacon", stringBuilder.toString());
        super.onCharacteristicWrite(bluetoothGatt, bluetoothGattCharacteristic, param1Int);
        this.resetter.onCharacteristicWrite(bluetoothGatt, bluetoothGattCharacteristic, param1Int);
    }
}