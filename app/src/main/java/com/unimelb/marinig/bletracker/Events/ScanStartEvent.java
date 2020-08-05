package com.unimelb.marinig.bletracker.Events;

public class ScanStartEvent {
    public long startScanTime;
    public long scanCycle;
    public long firstScanStartTime;
    public ScanStartEvent(long pScanCycle, long pFirstScanStartTime, long pStartScanTime){
        startScanTime = pStartScanTime;
        firstScanStartTime = pFirstScanStartTime;
        scanCycle = pScanCycle;
    }
}