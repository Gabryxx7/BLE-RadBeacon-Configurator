package com.unimelb.marinig.bletracker.Interfaces;

import android.util.Log;

import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;
import com.unimelb.marinig.bletracker.Utils.Utils;

import java.util.Arrays;

public class RadBeaconDotV11Marshaller {
    private ScannedDeviceRecord beacon;

    private String beaconPIN;

    private byte[] beaconValuesCharacteristicValue = new byte[5];

    private byte[] characteristic0Value = new byte[9];

    private byte[] characteristic1Value = new byte[2];

    private byte command;

    private byte[] eddystoneUIDCharacteristicValue = new byte[17];

    private byte[] eddystoneURLCharacteristicValue = new byte[20];

    private byte[] nameCharacteristicValue = new byte[20];

    private byte[] uuidCharacteristicValue = new byte[16];

    private byte[][] allCharacteristics = { this.characteristic0Value, this.characteristic1Value, this.nameCharacteristicValue, this.uuidCharacteristicValue, this.beaconValuesCharacteristicValue, this.eddystoneUIDCharacteristicValue, this.eddystoneURLCharacteristicValue };


    public RadBeaconDotV11Marshaller(ScannedDeviceRecord pRadBeacon, String paramString, byte paramByte) {
        this.beacon = pRadBeacon;
        this.beaconPIN = paramString;
        this.command = paramByte;
        marshallBeaconValues();
    }

    private byte advertisingAndPowerByte() { return (byte)((this.beacon.getAdvertisingRate() << 4 & 0xFFFFFFF0) + (this.beacon.getRadBeaconTransmitPowerIndex() & 0xF)); }

    private void marshallBeaconValues() {
        zeroAllCharacteristics();
        marshallCharacteristic0();
        marshallCharacteristic1();
        marshallBeaconName();
        byte b = this.beacon.getBeaconTypes();
        if ((b & 0x8) == 8)
            marshallEddystoneURLValues();
        if ((b & 0x4) == 4)
            marshallEddystoneUIDValues();
        if ((b & 0x3) != 0)
            marshallAltIBeaconValues();
    }

    private void marshallAltIBeaconValues() {
        Log.d("RadBeaconManager", "Updating beacon minor: " +this.beacon.getMinor());
        System.arraycopy(Utils.bytesFromUUIDString(this.beacon.getUUIDString()), 0, this.uuidCharacteristicValue, 0, 16);
        System.arraycopy(Utils.bytesFromShort((short) this.beacon.getMajor()), 0, this.beaconValuesCharacteristicValue, 0, 2);
        System.arraycopy(Utils.bytesFromShort((short) this.beacon.getMinor()), 0, this.beaconValuesCharacteristicValue, 2, 2);
        this.beaconValuesCharacteristicValue[4] = this.beacon.getIBeaconCalibratedPower();
    }

    private void marshallBeaconName() {
        int i = Math.min(this.beacon.getName().length(), 20);
        Log.d("RadBeaconManager", "Updating beacon name: " +this.beacon.getName());
        System.arraycopy(this.beacon.getName().getBytes(), 0, this.nameCharacteristicValue, 0, i);
    }

    private void marshallCharacteristic0() {
        this.characteristic0Value[0] = this.command;
        byte[] arrayOfByte = new byte[8];
        Arrays.fill(arrayOfByte, (byte)0);
        int i = Math.min(this.beaconPIN.length(), 8);
        System.arraycopy(this.beaconPIN.getBytes(), 0, arrayOfByte, 0, i);
        System.arraycopy(arrayOfByte, 0, this.characteristic0Value, 1, 8);
    }

    private void marshallCharacteristic1() { System.arraycopy(packedCharacteristic1Value(), 0, this.characteristic1Value, 0, 2); }

    private void marshallEddystoneUIDValues() {
        this.eddystoneUIDCharacteristicValue[0] = this.beacon.getEddystoneUIDCalibratedPower();
        byte[] arrayOfByte = this.beacon.getNamespaceID();
        System.arraycopy(arrayOfByte, 0, this.eddystoneUIDCharacteristicValue, 1, arrayOfByte.length);
        int i = arrayOfByte.length;
        arrayOfByte = this.beacon.getInstanceID();
        System.arraycopy(arrayOfByte, 0, this.eddystoneUIDCharacteristicValue, 1 + i, arrayOfByte.length);
    }

    private void marshallEddystoneURLValues() {
        byte[] arrayOfByte = this.beacon.getEncodedURL();
        int i = Math.min(arrayOfByte.length, 18);
        this.eddystoneURLCharacteristicValue[0] = (byte)(i + 1);
        this.eddystoneURLCharacteristicValue[1] = this.beacon.getEddystoneURLCalibratedPower();
        System.arraycopy(arrayOfByte, 0, this.eddystoneURLCharacteristicValue, 2, i);
    }

    private byte optionsAndBeaconTypesByte() { return (byte)(this.beacon.getBeaconTypes() & 0xF); }

    private byte[] packedCharacteristic1Value() { return new byte[] { advertisingAndPowerByte(), optionsAndBeaconTypesByte() }; }

    private void zeroAllCharacteristics() {
        byte[][] arrayOfByte = this.allCharacteristics;
        int i = arrayOfByte.length;
        for (byte b = 0; b < i; b++)
            Arrays.fill(arrayOfByte[b], (byte)0);
    }

    public byte[] getBeaconValuesCharacteristicValue() { return this.beaconValuesCharacteristicValue; }

    public byte[] getCharacteristic0Value() { return this.characteristic0Value; }

    public byte[] getCharacteristic1Value() { return this.characteristic1Value; }

    public byte[] getEddystoneUIDCharacteristicValue() { return this.eddystoneUIDCharacteristicValue; }

    public byte[] getEddystoneURLCharacteristicValue() { return this.eddystoneURLCharacteristicValue; }

    public byte[] getNameCharacteristicValue() { return this.nameCharacteristicValue; }

    public byte[] getUUIDCharacteristicValue() { return this.uuidCharacteristicValue; }
}
