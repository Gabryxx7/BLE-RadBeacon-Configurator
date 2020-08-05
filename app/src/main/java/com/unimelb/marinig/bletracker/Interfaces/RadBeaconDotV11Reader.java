package com.unimelb.marinig.bletracker.Interfaces;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.unimelb.marinig.bletracker.Callbacks.RadBeaconReadGattCallback;
import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;
import com.unimelb.marinig.bletracker.Utils.Utils;

import java.nio.ByteBuffer;
import java.util.UUID;

public class RadBeaconDotV11Reader extends RadBeaconDotV11Interface implements BeaconReader {
    private BluetoothGattService beaconService;

    private UUID[] dotBeaconV11ReadBeaconCharacteristics = { RADBEACON_DOT_V11_CHARACTERISTIC_1_UUID, RADBEACON_DOT_V11_NAME_UUID, RADBEACON_DOT_V11_UUID_UUID, RADBEACON_DOT_V11_BEACON_VALUES_UUID, RADBEACON_DOT_V11_EDDYSTONE_UID_UUID, RADBEACON_DOT_V11_EDDYSTONE_URL_UUID };

    public RadBeaconDotV11Reader(BluetoothGatt bluetoothGatt, BluetoothGattService bluetoothGattService, RadBeaconReadGattCallback pBeaconReadGattCallback) {
        super(bluetoothGatt, pBeaconReadGattCallback, null);
        this.beaconService = bluetoothGattService;
    }

    private boolean readNextCharacteristicInQueue() {
        if (this.characteristicQueue.size() > 0) {
            this.gatt.readCharacteristic((BluetoothGattCharacteristic) this.characteristicQueue.poll());
            return true;
        }
        return false;
    }

    private void setCharacteristic1Values(byte[] param1ArrayOfByte) {
        this.beacon.setModel("RadBeacon Dot");
        String str = String.format("%d.%d", new Object[] { Byte.valueOf(param1ArrayOfByte[1]), Byte.valueOf(param1ArrayOfByte[2]) });
        this.beacon.setVersion(str);
        this.beacon.setBatteryLevel(param1ArrayOfByte[12]);
        byte[] arrayOfByte = new byte[6];
        System.arraycopy(param1ArrayOfByte, 6, arrayOfByte, 0, 6);
        this.beacon.setDeviceID(Utils.byteArrayToHexString(arrayOfByte));
        byte b1 = param1ArrayOfByte[4];
        this.beacon.setSupportedActions(b1);
        byte b2 = param1ArrayOfByte[13];
        b1 = (byte)((byte)((b2 & 0xFFFFFFF0) >> 4) & 0xF);
        this.beacon.setAdvertisingRate(b1);
        b1 = (byte)(b2 & 0xF);
        this.beacon.setTransmitPowerIndex(b1);
        b1 = (byte)(param1ArrayOfByte[14] & 0xF);
        this.beacon.setBeaconTypes(b1);
    }

    private void setEddystoneUIDValues(byte[] param1ArrayOfByte) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(param1ArrayOfByte);
        byte b = byteBuffer.get();
        this.beacon.setEddystoneUIDCalibratedPower(b);
        byte[] arrayOfByte = new byte[10];
        byteBuffer.get(arrayOfByte, 0, arrayOfByte.length);
        this.beacon.setNamespaceID(arrayOfByte);
        arrayOfByte = new byte[6];
        byteBuffer.get(arrayOfByte, 0, arrayOfByte.length);
        this.beacon.setInstanceID(arrayOfByte);
    }

    private void setEddystoneURLValue(byte[] param1ArrayOfByte) {
        byte b = param1ArrayOfByte[0];
        if (b > 0) {
            byte b1 = param1ArrayOfByte[1];
            this.beacon.setEddystoneURLCalibratedPower(b1);
            byte[] arrayOfByte = new byte[--b];
            System.arraycopy(param1ArrayOfByte, 2, arrayOfByte, 0, b);
            this.beacon.setEncodedURL(arrayOfByte);
            return;
        }
        this.beacon.setEncodedURL(null);
    }

    private void setIBeaconAltBeaconValues(byte[] param1ArrayOfByte) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(param1ArrayOfByte);
        short s1 = byteBuffer.getShort();
        short s2 = byteBuffer.getShort();
        byte b = byteBuffer.get();
        this.beacon.setMajor(s1);
        this.beacon.setMinor(s2);
        this.beacon.setIBeaconCalibratedPower(b);
    }

    public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int param1Int) {
        Log.d("RadBeaconManager", "Characteristic read " + bluetoothGattCharacteristic.getUuid().toString());
        byte[] arrayOfByte = bluetoothGattCharacteristic.getValue();
        if (bluetoothGattCharacteristic.getUuid().equals(RADBEACON_DOT_V11_CHARACTERISTIC_1_UUID)) {
            Log.d("RadBeaconManager", "Reading Dot Char1 UUID");
            setCharacteristic1Values(arrayOfByte);
        } else {
            String str;
            if (bluetoothGattCharacteristic.getUuid().equals(RADBEACON_DOT_V11_NAME_UUID)) {
                Log.d("RadBeaconManager", "Reading Dot Name UUID");
                str = Utils.stringFromByteArray(arrayOfByte);
                this.beacon.setRadName(str);
            } else if (bluetoothGattCharacteristic.getUuid().equals(RADBEACON_DOT_V11_UUID_UUID)) {
                Log.d("RadBeaconManager", "Reading Dot UUID UUID");
                str = Utils.UUIDFromBytes(arrayOfByte).toString();
                this.beacon.setUUIDString(str);
            } else if (bluetoothGattCharacteristic.getUuid().equals(RADBEACON_DOT_V11_BEACON_VALUES_UUID)) {
                Log.d("RadBeaconManager", "Reading Dot Values UUID");
                setIBeaconAltBeaconValues(arrayOfByte);
            } else if (bluetoothGattCharacteristic.getUuid().equals(RADBEACON_DOT_V11_EDDYSTONE_UID_UUID)) {
                Log.d("RadBeaconManager", "Reading Dot EDUID UUID");
                setEddystoneUIDValues(arrayOfByte);
            } else if (bluetoothGattCharacteristic.getUuid().equals(RADBEACON_DOT_V11_EDDYSTONE_URL_UUID)) {
                Log.d("RadBeaconManager", "Reading Dot EDURL UUID");
                setEddystoneURLValue(arrayOfByte);
            }
        }
        this.beacon.setGattStatus(ScannedDeviceRecord.GattStatus.COMPLETED, !readNextCharacteristicInQueue());
    }

    public void readBeacon(ScannedDeviceRecord pBeacon) {
        this.beacon = pBeacon;
        Log.d("RadBeaconManager", "Reading Beacon");
        saveCharacteristics(this.beaconService, this.dotBeaconV11ReadBeaconCharacteristics);
        readNextCharacteristicInQueue();
    }

    protected void writeValueForCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {}
}
