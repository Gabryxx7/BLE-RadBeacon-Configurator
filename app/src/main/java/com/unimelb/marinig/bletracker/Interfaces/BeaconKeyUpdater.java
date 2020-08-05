package com.unimelb.marinig.bletracker.Interfaces;


import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;

public interface BeaconKeyUpdater extends BeaconOperation {
    void updateBeaconKey(ScannedDeviceRecord pBeacon);
}