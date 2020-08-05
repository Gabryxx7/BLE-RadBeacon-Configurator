package com.unimelb.marinig.bletracker.Events;

import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;

import java.util.List;

public class UpdatedDeviceListAvailable {
    public final List<ScannedDeviceRecord> deviceList;
    public final String sendTo;

    public UpdatedDeviceListAvailable(List<ScannedDeviceRecord> bluetoothDevices, String sendTo){
        this.deviceList = bluetoothDevices;
        this.sendTo = sendTo;
    }
}
