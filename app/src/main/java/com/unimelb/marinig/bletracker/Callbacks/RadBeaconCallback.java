package com.unimelb.marinig.bletracker.Callbacks;

import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;

public abstract class RadBeaconCallback {
    public void onBeaconConnectFailed(ScannedDeviceRecord pRadBeacon) {}
}
