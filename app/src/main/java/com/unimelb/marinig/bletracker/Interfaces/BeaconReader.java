package com.unimelb.marinig.bletracker.Interfaces;


import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;

public interface BeaconReader extends BeaconOperation {
    void readBeacon(ScannedDeviceRecord pBeacon);
}