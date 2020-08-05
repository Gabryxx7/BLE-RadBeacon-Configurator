package com.unimelb.marinig.bletracker.Events;

import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;

public class UpdatedScannedDeviceEvent {
    public final ScannedDeviceRecord device;
    public final boolean updateView;

    public UpdatedScannedDeviceEvent(ScannedDeviceRecord device){
        this.device = device;
        this.updateView = false;
    }


    public UpdatedScannedDeviceEvent(ScannedDeviceRecord device, boolean pUpdateView){
        this.device = device;
        this.updateView = pUpdateView;
    }
}