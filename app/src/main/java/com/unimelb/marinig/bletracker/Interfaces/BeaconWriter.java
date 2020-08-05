package com.unimelb.marinig.bletracker.Interfaces;


import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;

public interface BeaconWriter extends BeaconOperation {
    void updateBeacon(ScannedDeviceRecord pBeacon);
}