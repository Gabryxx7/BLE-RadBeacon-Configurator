package com.unimelb.marinig.bletracker.Interfaces;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import com.unimelb.marinig.bletracker.Callbacks.RadBeaconGattCallback;

import java.util.Arrays;
import java.util.UUID;

public class RadBeaconDotV11ActionWithPIN extends RadBeaconDotV11Interface {
    private static final int CHARACTERISTIC_ZERO_ACTION_WITH_PIN_LENGTH = 9;

    private static final int CHARACTERISTIC_ZERO_COMMAND_INDEX = 0;

    private static final int CHARACTERISTIC_ZERO_CURRENT_PIN_INDEX = 1;

    private static final UUID[] actionWithPINCharacteristics = { RADBEACON_DOT_V11_CHARACTERISTIC_0_UUID };

    private byte beaconAction;

    private byte[] characteristic0Value = new byte[9];

    public RadBeaconDotV11ActionWithPIN(BluetoothGatt paramBluetoothGatt, BluetoothGattService paramBluetoothGattService, RadBeaconGattCallback pRadBeaconGattCallback, String paramString, byte paramByte) {
        super(paramBluetoothGatt, paramBluetoothGattService, pRadBeaconGattCallback, paramString, actionWithPINCharacteristics);
        this.beaconAction = paramByte;
        composeCharacteristic0Value();
    }

    private void composeCharacteristic0Value() {
        Arrays.fill(this.characteristic0Value, (byte)0);
        this.characteristic0Value[0] = this.beaconAction;
        System.arraycopy(this.beaconPIN.getBytes(), 0, this.characteristic0Value, 1, Math.min(this.beaconPIN.length(), 8));
    }

    protected void writeValueForCharacteristic(BluetoothGattCharacteristic paramBluetoothGattCharacteristic) {
        if (paramBluetoothGattCharacteristic.getUuid().equals(RADBEACON_DOT_V11_CHARACTERISTIC_0_UUID)) {
            paramBluetoothGattCharacteristic.setValue(this.characteristic0Value);
            this.gatt.writeCharacteristic(paramBluetoothGattCharacteristic);
        }
    }
}
