package com.unimelb.marinig.bletracker.Interfaces;


import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;

public interface BeaconLocker extends BeaconOperation {
    void lockBeacon(ScannedDeviceRecord pBeacon);
}