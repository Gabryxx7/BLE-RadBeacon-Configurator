package com.unimelb.marinig.bletracker.Interfaces;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.unimelb.marinig.bletracker.Callbacks.RadBeaconUpdatePinGattCallback;
import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;

import java.util.UUID;

public class RadBeaconDotV11PINUpdater extends RadBeaconDotV11Interface implements BeaconPINUpdater {
    private static final int CHARACTERISTIC_ZERO_CURRENT_PIN_INDEX = 1;

    private static final int CHARACTERISTIC_ZERO_NEW_PIN_INDEX = 9;

    private static final int CHARACTERISTIC_ZERO_UPDATE_PIN_COMMAND_INDEX = 0;

    private static final int CHARACTERISTIC_ZERO_UPDATE_PIN_LENGTH = 17;

    private byte[] characteristic0Value = new byte[17];

    protected String newBeaconPIN;

    public RadBeaconDotV11PINUpdater(BluetoothGatt bluetoothGatt, BluetoothGattService bluetoothGattService, RadBeaconUpdatePinGattCallback pBeaconUpdatePinGattCallback, String param1String1, String param1String2) {
        super(bluetoothGatt, bluetoothGattService, pBeaconUpdatePinGattCallback, param1String1, new UUID[] { RADBEACON_DOT_V11_CHARACTERISTIC_0_UUID });
        this.newBeaconPIN = param1String2;
        composeCharacteristic0Value();
    }

    private void composeCharacteristic0Value() {
        this.characteristic0Value[0] = 2;
        System.arraycopy(this.beaconPIN.getBytes(), 0, this.characteristic0Value, 1, this.beaconPIN.length());
        System.arraycopy(this.newBeaconPIN.getBytes(), 0, this.characteristic0Value, 9, this.newBeaconPIN.length());
    }

    public void updateBeaconPIN(ScannedDeviceRecord pBeacon) {
        this.beacon = pBeacon;
        writeNextCharacteristicInQueue();
    }

    public void writeValueForCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        if (bluetoothGattCharacteristic.getUuid().equals(RADBEACON_DOT_V11_CHARACTERISTIC_0_UUID)) {
            bluetoothGattCharacteristic.setValue(this.characteristic0Value);
            this.gatt.writeCharacteristic(bluetoothGattCharacteristic);
        }
    }
}