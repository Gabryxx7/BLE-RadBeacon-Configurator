package com.unimelb.marinig.bletracker.Events;

import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;

public class NewDeviceScannedEvent {
    public final ScannedDeviceRecord device;

    public NewDeviceScannedEvent(ScannedDeviceRecord device){
        this.device = device;
    }
}