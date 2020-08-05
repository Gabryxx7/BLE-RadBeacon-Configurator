package com.unimelb.marinig.bletracker.Interfaces;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import com.unimelb.marinig.bletracker.Callbacks.RadBeaconGattCallback;

import java.util.UUID;

public abstract class RadBeaconDotInterface extends RadBeaconUSBInterface {
    public static final byte BEACON_TYPE_ALTBEACON = 2;

    public static final byte BEACON_TYPE_DEFAULT = 7;

    public static final byte BEACON_TYPE_IBEACON = 1;

    public static final byte BEACON_TYPE_ZIPBEACON_UID = 4;

    public static final byte BEACON_TYPE_ZIPBEACON_URL = 8;

    public static final byte PAYLOAD_LENGTH_ALT_IBEACON = 22;

    public static final byte PAYLOAD_LENGTH_ZIPBEACON_UID = 18;

    public static final byte PAYLOAD_TYPE_ALT_IBEACON = 3;

    public static final byte PAYLOAD_TYPE_ZIPBEACON_UID = 4;

    public static final byte PAYLOAD_TYPE_ZIPBEACON_URL = 8;

    public static final int RADBEACON_DOT_CHARACTERISTIC_0_COMMAND_INDEX = 0;

    public static final int RADBEACON_DOT_CHARACTERISTIC_0_LENGTH = 9;

    public static final int RADBEACON_DOT_CHARACTERISTIC_0_PIN_INDEX = 1;

    public static final UUID RADBEACON_DOT_CHARACTERISTIC_0_UUID = UUID.fromString("F1D582D8-C67A-4656-99C7-DF0AD1701EBC");

    public static final String RADBEACON_DOT_CHARACTERISTIC_0_UUID128 = "F1D582D8-C67A-4656-99C7-DF0AD1701EBC";

    public static final int RADBEACON_DOT_CHARACTERISTIC_1_LENGTH = 10;

    public static final UUID RADBEACON_DOT_CHARACTERISTIC_1_UUID = UUID.fromString("F2D582D8-C67A-4656-99C7-DF0AD1701EBC");

    public static final String RADBEACON_DOT_CHARACTERISTIC_1_UUID128 = "F2D582D8-C67A-4656-99C7-DF0AD1701EBC";

    public static final int RADBEACON_DOT_CHARACTERISTIC_2TO4_LENGTH = 52;

    public static final int RADBEACON_DOT_CHARACTERISTIC_2_ADV_POWER_INDEX = 8;

    public static final int RADBEACON_DOT_CHARACTERISTIC_2_BEACON_TYPES_INDEX = 9;

    public static final int RADBEACON_DOT_CHARACTERISTIC_2_LENGTH = 20;

    public static final UUID RADBEACON_DOT_CHARACTERISTIC_2_UUID = UUID.fromString("F3D582D8-C67A-4656-99C7-DF0AD1701EBC");

    public static final String RADBEACON_DOT_CHARACTERISTIC_2_UUID128 = "F3D582D8-C67A-4656-99C7-DF0AD1701EBC";

    public static final int RADBEACON_DOT_CHARACTERISTIC_3_LENGTH = 20;

    public static final UUID RADBEACON_DOT_CHARACTERISTIC_3_UUID = UUID.fromString("F4D582D8-C67A-4656-99C7-DF0AD1701EBC");

    public static final String RADBEACON_DOT_CHARACTERISTIC_3_UUID128 = "F4D582D8-C67A-4656-99C7-DF0AD1701EBC";

    public static final int RADBEACON_DOT_CHARACTERISTIC_4_LENGTH = 12;

    public static final UUID RADBEACON_DOT_CHARACTERISTIC_4_UUID = UUID.fromString("F5D582D8-C67A-4656-99C7-DF0AD1701EBC");

    public static final String RADBEACON_DOT_CHARACTERISTIC_4_UUID128 = "F5D582D8-C67A-4656-99C7-DF0AD1701EBC";

    public static final byte RADBEACON_DOT_COMMAND_CHANGE_PIN = 2;

    public static final byte RADBEACON_DOT_COMMAND_LOCK = 5;

    public static final byte RADBEACON_DOT_COMMAND_RESET = 3;

    public static final byte RADBEACON_DOT_COMMAND_UPDATE = 1;

    public static final byte RADBEACON_DOT_DEFAULT_ADV_POWER = -91;

    public static final int RADBEACON_DOT_NAME_LENGTH = 8;

    public static final int RADBEACON_DOT_PIN_LENGTH = 8;

    public static final int UUID_LENGTH = 16;

    protected BluetoothGattCharacteristic characteristic0;

    public RadBeaconDotInterface(BluetoothGatt paramBluetoothGatt, BluetoothGattService paramBluetoothGattService, RadBeaconGattCallback pRadBeaconGattCallback, String paramString, UUID[] paramArrayOfUUID) { super(paramBluetoothGatt, paramBluetoothGattService, pRadBeaconGattCallback, paramString, paramArrayOfUUID); }

    public RadBeaconDotInterface(BluetoothGatt paramBluetoothGatt, RadBeaconGattCallback pRadBeaconGattCallback, String paramString) { super(paramBluetoothGatt, pRadBeaconGattCallback, paramString); }

    protected UUID getBeaconResultUUID() { return RADBEACON_DOT_CHARACTERISTIC_0_UUID; }

    protected int readResultCharacteristicValue(byte[] paramArrayOfByte) { return paramArrayOfByte[0]; }
}
