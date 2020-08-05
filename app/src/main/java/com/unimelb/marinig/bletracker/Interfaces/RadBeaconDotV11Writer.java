package com.unimelb.marinig.bletracker.Interfaces;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.unimelb.marinig.bletracker.Callbacks.RadBeaconUpdateGattCallback;
import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;
import com.unimelb.marinig.bletracker.Utils.Utils;

import java.util.UUID;

public class RadBeaconDotV11Writer extends RadBeaconDotV11Interface implements BeaconWriter {
    private BluetoothGattService beaconService;

    private UUID[] dotBeaconWriteV11Characteristics = { RADBEACON_DOT_V11_CHARACTERISTIC_1_UUID, RADBEACON_DOT_V11_NAME_UUID, RADBEACON_DOT_V11_UUID_UUID, RADBEACON_DOT_V11_BEACON_VALUES_UUID, RADBEACON_DOT_V11_EDDYSTONE_UID_UUID, RADBEACON_DOT_V11_EDDYSTONE_URL_UUID, RADBEACON_DOT_V11_CHARACTERISTIC_0_UUID };

    private RadBeaconDotV11Marshaller marshaller;

    public RadBeaconDotV11Writer(BluetoothGatt bluetoothGatt, BluetoothGattService bluetoothGattService, RadBeaconUpdateGattCallback pBeaconUpdateGattCallback, String param1String) {
        super(bluetoothGatt, pBeaconUpdateGattCallback, param1String);
        this.beaconService = bluetoothGattService;
    }

    public void updateBeacon(ScannedDeviceRecord pBeacon) {
        this.beacon = pBeacon;
        this.marshaller = new RadBeaconDotV11Marshaller(pBeacon, this.beaconPIN, (byte)1);
        saveCharacteristics(this.beaconService, this.dotBeaconWriteV11Characteristics);
        writeNextCharacteristicInQueue();
    }


    public void writeValueForCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        UUID uUID = bluetoothGattCharacteristic.getUuid();
        boolean res = false;
        if (uUID.equals(RADBEACON_DOT_V11_CHARACTERISTIC_1_UUID)) {
            res = bluetoothGattCharacteristic.setValue(this.marshaller.getCharacteristic1Value());
            Log.d("RadBeaconManager", "Writing Dot Char1 UUID "+ Utils.bytesToHex(this.marshaller.getCharacteristic1Value()) +"..." +(res ? "SUCCESS":"FAIL"));
        } else if (uUID.equals(RADBEACON_DOT_V11_NAME_UUID)) {
            res = bluetoothGattCharacteristic.setValue(this.marshaller.getNameCharacteristicValue());
            Log.d("RadBeaconManager", "Writing Dot Name UUID "+ Utils.bytesToHex(this.marshaller.getNameCharacteristicValue()) +"..." +(res ? "SUCCESS":"FAIL"));
        } else if (uUID.equals(RADBEACON_DOT_V11_UUID_UUID)) {
            res = bluetoothGattCharacteristic.setValue(this.marshaller.getUUIDCharacteristicValue());
            Log.d("RadBeaconManager", "Writing Dot UUID UUID "+ Utils.bytesToHex(this.marshaller.getUUIDCharacteristicValue()) +"..." +(res ? "SUCCESS":"FAIL"));
        } else if (uUID.equals(RADBEACON_DOT_V11_BEACON_VALUES_UUID)) {
            res = bluetoothGattCharacteristic.setValue(this.marshaller.getBeaconValuesCharacteristicValue());
            Log.d("RadBeaconManager", "Writing Dot Values UUID "+ Utils.bytesToHex(this.marshaller.getBeaconValuesCharacteristicValue()) +"..." +(res ? "SUCCESS":"FAIL"));
        } else if (uUID.equals(RADBEACON_DOT_V11_EDDYSTONE_UID_UUID)) {
            res = bluetoothGattCharacteristic.setValue(this.marshaller.getEddystoneUIDCharacteristicValue());
            Log.d("RadBeaconManager", "Writing Dot EDUID UUID "+ Utils.bytesToHex(this.marshaller.getEddystoneUIDCharacteristicValue()) +"..." +(res ? "SUCCESS":"FAIL"));
        } else if (uUID.equals(RADBEACON_DOT_V11_EDDYSTONE_URL_UUID)) {
            res = bluetoothGattCharacteristic.setValue(this.marshaller.getEddystoneURLCharacteristicValue());
            Log.d("RadBeaconManager", "Writing Dot EDURL UUID "+ Utils.bytesToHex(this.marshaller.getEddystoneURLCharacteristicValue()) +"..." +(res ? "SUCCESS":"FAIL"));
        } else if (uUID.equals(RADBEACON_DOT_V11_CHARACTERISTIC_0_UUID)) {
            res = bluetoothGattCharacteristic.setValue(this.marshaller.getCharacteristic0Value());
            Log.d("RadBeaconManager", "Writing Dot Char10UUID "+ Utils.bytesToHex(this.marshaller.getCharacteristic0Value()) +"..." +(res ? "SUCCESS":"FAIL"));
        }
        this.gatt.writeCharacteristic(bluetoothGattCharacteristic);
    }
}