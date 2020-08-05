/**
 * Created by Yuan WANG (811006, yuanw8)
 * Project: The Optimization of Hospital Workflow
 * This file is the used to calculate accuracy
 */

package com.unimelb.marinig.bletracker.Utils;

public class Calculator {

    public static double calculateDistance(int txPower, double rssi, boolean isEddystone) {
        if (rssi == 0) {
            return -1.0;
        }

        double ratio = rssi * 1.0 / (isEddystone ? txPower - 41 : txPower);

        if (ratio < 1.0)
            return Math.pow(ratio,10);
        else
            return (0.89976)*Math.pow(ratio,7.7095) + 0.111;
    }
}
