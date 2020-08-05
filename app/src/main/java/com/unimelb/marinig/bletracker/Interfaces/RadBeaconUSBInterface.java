package com.unimelb.marinig.bletracker.Interfaces;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;
import com.unimelb.marinig.bletracker.Utils.Utils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import com.unimelb.marinig.bletracker.Callbacks.RadBeaconGattCallback;

public abstract class RadBeaconUSBInterface implements BeaconOperation {
    public static final int BEACON_ACTION_DFU_REBOOT = 4;

    public static final int BEACON_ACTION_DO_NOTHING = 0;

    public static final int BEACON_ACTION_LOCK = 5;

    public static final int BEACON_ACTION_RESET_TO_FACTORY = 3;

    public static final int BEACON_ACTION_UPDATE_ADV = 1;

    public static final int BEACON_ACTION_UPDATE_PIN = 2;

    public static final UUID BEACON_ACTION_UUID = Utils.UUIDFrom16BitString("AAAB");

    public static final String BEACON_ACTION_UUID16 = "AAAB";

    public static final UUID BEACON_ADVERTISING_INTERVAL_UUID = Utils.UUIDFrom16BitString("AAA8");

    public static final String BEACON_ADVERTISING_INTERVAL_UUID16 = "AAA8";

    public static final UUID BEACON_DEVICE_ID_UUID = Utils.UUIDFrom16BitString("AAA1");

    public static final String BEACON_DEVICE_ID_UUID16 = "AAA1";

    public static final UUID BEACON_MAJOR_UUID = Utils.UUIDFrom16BitString("AAA4");

    public static final String BEACON_MAJOR_UUID16 = "AAA4";

    public static final UUID  BEACON_MEASURED_POWER_UUID = Utils.UUIDFrom16BitString("AAA6");

    public static final String BEACON_MEASURED_POWER_UUID16 = "AAA6";

    public static final UUID BEACON_MINOR_UUID = Utils.UUIDFrom16BitString("AAA5");

    public static final String BEACON_MINOR_UUID16 = "AAA5";

    public static final UUID BEACON_MODEL_UUID = Utils.UUIDFrom16BitString("AAA0");

    public static final String BEACON_MODEL_UUID16 = "AAA0";

    public static final UUID BEACON_NAME_UUID = Utils.UUIDFrom16BitString("AAA2");

    public static final String BEACON_NAME_UUID16 = "AAA2";

    public static final UUID BEACON_NEW_PIN_UUID = Utils.UUIDFrom16BitString("AAAA");

    public static final String BEACON_NEW_PIN_UUID16 = "AAAA";

    public static final UUID BEACON_PIN_UUID = Utils.UUIDFrom16BitString("AAAC");

    public static final String BEACON_PIN_UUID16 = "AAAC";

    public static final int BEACON_RESULT_ERROR = 2;

    public static final int BEACON_RESULT_INVALID_PIN = 1;

    public static final int BEACON_RESULT_SUCCESS = 0;

    public static final UUID BEACON_RESULT_UUID = Utils.UUIDFrom16BitString("AAA9");

    public static final String BEACON_RESULT_UUID16 = "AAA9";

    public static final UUID BEACON_TRANSMIT_POWER_UUID = Utils.UUIDFrom16BitString("AAA7");

    public static final String BEACON_TRANSMIT_POWER_UUID16 = "AAA7";

    public static final UUID BEACON_TYPES_UUID = Utils.UUIDFrom16BitString("AAAD");

    public static final String BEACON_TYPES_UUID16 = "AAAD";

    public static final UUID BEACON_UUID_UUID = Utils.UUIDFrom16BitString("AAA3");

    public static final String BEACON_UUID_UUID16 = "AAA3";

    public static final UUID BEACON_VERSION_UUID = Utils.UUIDFrom16BitString("AAAE");

    public static final String BEACON_VERSION_UUID16 = "AAAE";

    protected ScannedDeviceRecord beacon;

    protected String beaconPIN;

    protected final RadBeaconGattCallback callback;

    protected Queue<BluetoothGattCharacteristic> characteristicQueue;

    protected BluetoothGatt gatt;

    protected BluetoothGattCharacteristic resultCharacteristic;



    public RadBeaconUSBInterface(BluetoothGatt paramBluetoothGatt, BluetoothGattService paramBluetoothGattService, RadBeaconGattCallback pRadBeaconGattCallback, String paramString, UUID[] paramArrayOfUUID) {
        this.gatt = paramBluetoothGatt;
        this.callback = pRadBeaconGattCallback;
        this.beaconPIN = paramString;
        saveCharacteristics(paramBluetoothGattService, paramArrayOfUUID);
    }

    public RadBeaconUSBInterface(BluetoothGatt paramBluetoothGatt, RadBeaconGattCallback pRadBeaconGattCallback, String paramString) {
        this.gatt = paramBluetoothGatt;
        this.callback = pRadBeaconGattCallback;
        this.beaconPIN = paramString;
    }

    protected UUID getBeaconResultUUID() { return BEACON_RESULT_UUID; }

    public void onCharacteristicChanged(BluetoothGatt paramBluetoothGatt, BluetoothGattCharacteristic paramBluetoothGattCharacteristic) {}

    public void onCharacteristicRead(BluetoothGatt paramBluetoothGatt, BluetoothGattCharacteristic paramBluetoothGattCharacteristic, int resultStatus) {
        if (paramBluetoothGattCharacteristic.getUuid().equals(this.resultCharacteristic.getUuid())) {
            resultStatus = readResultCharacteristicValue(paramBluetoothGattCharacteristic.getValue());
            this.callback.setOperationResult(resultStatus);
        }
    }

    public void onCharacteristicWrite(BluetoothGatt paramBluetoothGatt, BluetoothGattCharacteristic paramBluetoothGattCharacteristic, int paramInt) {
        this.beacon.setGattStatus(ScannedDeviceRecord.GattStatus.COMPLETED, !writeNextCharacteristicInQueue());
        if (this.beacon.hasGattStatus(ScannedDeviceRecord.GattStatus.COMPLETED))
            readResultCharacteristic();
    }

    protected void readResultCharacteristic() { this.gatt.readCharacteristic(this.resultCharacteristic); }

    protected int readResultCharacteristicValue(byte[] paramArrayOfByte) {
        return Utils.byteArrayToInteger(paramArrayOfByte);
    }

    protected void saveCharacteristics(BluetoothGattService paramBluetoothGattService, UUID[] paramArrayOfUUID) {
        this.characteristicQueue = new LinkedList();
        int i = paramArrayOfUUID.length;
        for (byte b = 0; b < i; b++) {
            UUID uUID = paramArrayOfUUID[b];
            this.characteristicQueue.add(paramBluetoothGattService.getCharacteristic(uUID));
        }
        this.resultCharacteristic = paramBluetoothGattService.getCharacteristic(getBeaconResultUUID());
    }

    protected boolean writeNextCharacteristicInQueue() {
        if (this.characteristicQueue.size() > 0) {
            writeValueForCharacteristic((BluetoothGattCharacteristic)this.characteristicQueue.poll());
            return true;
        }
        return false;
    }

    protected abstract void writeValueForCharacteristic(BluetoothGattCharacteristic paramBluetoothGattCharacteristic);
}
