package com.unimelb.marinig.bletracker.Events;

import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;

import java.util.List;

public class ScannerUpdateEvent {
    public final List<ScannedDeviceRecord> deviceList;
    public final boolean isScanning;

    public ScannerUpdateEvent(List<ScannedDeviceRecord> bluetoothDevices, boolean isScanning){
        this.deviceList = bluetoothDevices;
        this.isScanning = isScanning;
    }
}
