package com.unimelb.marinig.bletracker.Events;

import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;

public class ReadBeaconEvent {
    public ScannedDeviceRecord beaconToRead;
    public ReadBeaconEvent(ScannedDeviceRecord beaconToRead){
        this.beaconToRead = beaconToRead;
    }
}