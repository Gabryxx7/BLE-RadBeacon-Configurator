package com.unimelb.marinig.bletracker.Events;

public class ScanStopEvent {
    public long startScanTime;
    public long endScanTime;
    public long scanCycle;
    public long firstScanStartTime;
    public ScanStopEvent(long pScanCycle, long pFirstScanStartTime, long pStartScanTime, long pEndScanTime){
        startScanTime = pStartScanTime;
        scanCycle = pScanCycle;
        endScanTime = pEndScanTime;
        firstScanStartTime = pFirstScanStartTime;
    }
}