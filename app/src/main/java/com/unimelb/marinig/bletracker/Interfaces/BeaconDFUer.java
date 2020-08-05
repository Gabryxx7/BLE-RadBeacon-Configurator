package com.unimelb.marinig.bletracker.Interfaces;


import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;

public interface BeaconDFUer extends BeaconOperation {
    void bootToDFU(ScannedDeviceRecord pBeacon);
}