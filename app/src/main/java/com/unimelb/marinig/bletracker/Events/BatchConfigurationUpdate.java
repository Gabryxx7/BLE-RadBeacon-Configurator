package com.unimelb.marinig.bletracker.Events;

import com.unimelb.marinig.bletracker.Model.BeaconConfig;
import com.unimelb.marinig.bletracker.Model.BeaconConfigurator;
import com.unimelb.marinig.bletracker.Model.BeaconConfigurator.BatchConfigState;
import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;
import com.unimelb.marinig.bletracker.Services.BLEScanner;

import java.util.ArrayList;

public class BatchConfigurationUpdate {
    public BeaconConfigurator configurator;
    public ScannedDeviceRecord beacon;
    public boolean fromScanner;

    public BatchConfigurationUpdate(boolean fromScanner, BeaconConfigurator pConfigurator){
        this.configurator = pConfigurator;
        this.fromScanner = fromScanner;
    }

    public BatchConfigurationUpdate(boolean fromScanner, BeaconConfigurator pConfigurator, ScannedDeviceRecord beacon){
        this(fromScanner, pConfigurator);
        this.beacon = beacon;
    }
}
