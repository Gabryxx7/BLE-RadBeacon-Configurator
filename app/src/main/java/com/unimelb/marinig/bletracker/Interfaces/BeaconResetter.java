package com.unimelb.marinig.bletracker.Interfaces;


import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;

public interface BeaconResetter extends BeaconOperation {
    void resetBeacon(ScannedDeviceRecord pBeacon);
}