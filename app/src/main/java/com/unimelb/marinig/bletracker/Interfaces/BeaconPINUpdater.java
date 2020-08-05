package com.unimelb.marinig.bletracker.Interfaces;


import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;

public interface BeaconPINUpdater extends BeaconOperation {
    void updateBeaconPIN(ScannedDeviceRecord pBeacon);
}