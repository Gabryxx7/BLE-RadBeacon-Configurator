package com.unimelb.marinig.bletracker.Interfaces;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;

import com.unimelb.marinig.bletracker.Callbacks.RadBeaconGattCallback;
import java.util.UUID;

public abstract class RadBeaconDotV11Interface extends RadBeaconDotInterface {
    public static final String RADBEACON_DOT_V11_BEACON_TYPES_UUID128 = "F5CEC428-2EBB-47AB-A753-0CE09E9FE64B";

    public static final int RADBEACON_DOT_V11_BEACON_VALUES_CHARACTERISTIC_LENGTH = 5;

    public static final UUID RADBEACON_DOT_V11_BEACON_VALUES_UUID = UUID.fromString("F5CEC428-2EBB-47AB-A753-0CE09E9FE64B");

    public static final int RADBEACON_DOT_V11_CHARACTERISTIC_0_COMMAND_INDEX = 0;

    public static final int RADBEACON_DOT_V11_CHARACTERISTIC_0_LENGTH = 9;

    public static final int RADBEACON_DOT_V11_CHARACTERISTIC_0_PIN_INDEX = 1;

    public static final UUID RADBEACON_DOT_V11_CHARACTERISTIC_0_UUID = UUID.fromString("F1CEC428-2EBB-47AB-A753-0CE09E9FE64B");

    public static final String RADBEACON_DOT_V11_CHARACTERISTIC_0_UUID128 = "F1CEC428-2EBB-47AB-A753-0CE09E9FE64B";

    public static final byte RADBEACON_DOT_V11_CHARACTERISTIC_1_ADVERTISING_RATE_MASK = -16;

    public static final int RADBEACON_DOT_V11_CHARACTERISTIC_1_ADV_POWER_INDEX = 0;

    public static final int RADBEACON_DOT_V11_CHARACTERISTIC_1_ADV_RATE_TX_POWER_INDEX = 13;

    public static final int RADBEACON_DOT_V11_CHARACTERISTIC_1_BATTERY_LEVEL_INDEX = 12;

    public static final byte RADBEACON_DOT_V11_CHARACTERISTIC_1_BEACON_TYPES_MASK = 15;

    public static final int RADBEACON_DOT_V11_CHARACTERISTIC_1_DEVICE_ID_INDEX = 6;

    public static final int RADBEACON_DOT_V11_CHARACTERISTIC_1_DEVICE_ID_LENGTH = 6;

    public static final int RADBEACON_DOT_V11_CHARACTERISTIC_1_LENGTH = 15;

    public static final int RADBEACON_DOT_V11_CHARACTERISTIC_1_MODEL_INDEX = 0;

    public static final int RADBEACON_DOT_V11_CHARACTERISTIC_1_OPTIONS_BEACON_TYPES_INDEX = 1;

    public static final int RADBEACON_DOT_V11_CHARACTERISTIC_1_OPTIONS_ENABLED_BEACON_TYPES_INDEX = 14;

    public static final int RADBEACON_DOT_V11_CHARACTERISTIC_1_REVERSED_MAC_ADDRESS_INDEX = 5;

    public static final int RADBEACON_DOT_V11_CHARACTERISTIC_1_SUPPORTED_ACTIONS_INDEX = 4;

    public static final int RADBEACON_DOT_V11_CHARACTERISTIC_1_SUPPORTED_BEACON_TYPES_INDEX = 3;

    public static final byte RADBEACON_DOT_V11_CHARACTERISTIC_1_TRANSMIT_POWER_MASK = 15;

    public static final UUID RADBEACON_DOT_V11_CHARACTERISTIC_1_UUID = UUID.fromString("F2CEC428-2EBB-47AB-A753-0CE09E9FE64B");

    public static final String RADBEACON_DOT_V11_CHARACTERISTIC_1_UUID128 = "F2CEC428-2EBB-47AB-A753-0CE09E9FE64B";

    public static final int RADBEACON_DOT_V11_CHARACTERISTIC_1_VERSION_MAJOR_INDEX = 1;

    public static final int RADBEACON_DOT_V11_CHARACTERISTIC_1_VERSION_MINOR_INDEX = 2;

    public static final int RADBEACON_DOT_V11_CHARACTERISTIC_1_WRITE_LENGTH = 2;

    public static final int RADBEACON_DOT_V11_EDDYSTONE_UID_CHARACTERISTIC_LENGTH = 17;

    public static final UUID RADBEACON_DOT_V11_EDDYSTONE_UID_UUID = UUID.fromString("F6CEC428-2EBB-47AB-A753-0CE09E9FE64B");

    public static final String RADBEACON_DOT_V11_EDDYSTONE_UID_UUID128 = "F6CEC428-2EBB-47AB-A753-0CE09E9FE64B";

    public static final int RADBEACON_DOT_V11_EDDYSTONE_URL_CHARACTERISTIC_LENGTH = 20;

    public static final UUID RADBEACON_DOT_V11_EDDYSTONE_URL_UUID = UUID.fromString("F7CEC428-2EBB-47AB-A753-0CE09E9FE64B");

    public static final String RADBEACON_DOT_V11_EDDYSTONE_URL_UUID128 = "F7CEC428-2EBB-47AB-A753-0CE09E9FE64B";

    public static final int RADBEACON_DOT_V11_MAX_URL_LENGTH = 18;

    public static final int RADBEACON_DOT_V11_NAME_CHARACTERISTIC_LENGTH = 20;

    public static final UUID RADBEACON_DOT_V11_NAME_UUID = UUID.fromString("F3CEC428-2EBB-47AB-A753-0CE09E9FE64B");

    public static final String RADBEACON_DOT_V11_NAME_UUID128 = "F3CEC428-2EBB-47AB-A753-0CE09E9FE64B";

    public static final int RADBEACON_DOT_V11_PIN_LENGTH = 8;

    public static final int RADBEACON_DOT_V11_UUID_CHARACTERISTIC_LENGTH = 16;

    public static final UUID RADBEACON_DOT_V11_UUID_UUID = UUID.fromString("F4CEC428-2EBB-47AB-A753-0CE09E9FE64B");

    public static final String RADBEACON_DOT_V11_UUID_UUID128 = "F4CEC428-2EBB-47AB-A753-0CE09E9FE64B";

    public static final byte RADBEACON_V11_BEACON_TYPES_MASK = 15;

    public RadBeaconDotV11Interface(BluetoothGatt paramBluetoothGatt, BluetoothGattService paramBluetoothGattService, RadBeaconGattCallback pRadBeaconGattCallback, String paramString, UUID[] paramArrayOfUUID) { super(paramBluetoothGatt, paramBluetoothGattService, pRadBeaconGattCallback, paramString, paramArrayOfUUID); }

    public RadBeaconDotV11Interface(BluetoothGatt paramBluetoothGatt, RadBeaconGattCallback pRadBeaconGattCallback, String paramString) { super(paramBluetoothGatt, pRadBeaconGattCallback, paramString); }

    protected UUID getBeaconResultUUID() { return RADBEACON_DOT_V11_CHARACTERISTIC_0_UUID; }
}

