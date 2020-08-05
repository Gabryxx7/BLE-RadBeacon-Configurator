package com.unimelb.marinig.bletracker.Events;

import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;

public class UpdateBeaconEvent {
    public ScannedDeviceRecord beaconToUpdate;
    public String beaconPIN;
    public UpdateBeaconEvent(ScannedDeviceRecord pBeaconToUpdate, String pBeaconPin){
        this.beaconToUpdate = pBeaconToUpdate;
        this.beaconPIN = pBeaconPin;
    }
}