package com.unimelb.marinig.bletracker.Callbacks;

import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;

public abstract class RadBeaconUpdateCallback extends RadBeaconCallback {
    public void onBeaconUpdate(ScannedDeviceRecord pRadBeacon, int paramInt) {}
}
