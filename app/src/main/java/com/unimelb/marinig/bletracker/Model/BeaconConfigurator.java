package com.unimelb.marinig.bletracker.Model;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.Batch;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BeaconConfigurator {
    private final static String TAG = "BatchConfigBeaconConfigurator";
    private ArrayList<BeaconConfig> mConfigList = null;
    private BeaconConfig mConfigData;
    private BeaconConfig mLastConfig;

    private HashMap<String, BeaconConfig> mMacConfigList = null;
    private Iterator<BeaconConfig> mConfigListIterator = null;
    private ArrayList<BeaconConfig> mCompletedConfig = new ArrayList<>();
    private String mCompletedFileName = "BeaconConfig";
    private File mDataFile;
    private FileWriter mDataFileWriter;
    private CSVWriter mDataFileCsvWriter;
    private SimpleDateFormat mDateFormat;
    private SimpleDateFormat mDateFormatLog;
    private TxPowerSettings mTxSettings;
    private AdvRateSetting mAdvSettings;
    private MinorSetting mMinorSettings;
    private ExtraSetting mExtraSettings;

    private Integer mAskedTxPowerIndex = null;
    private Integer mAskedAdvRateIndex = null;
    private Integer mAskedMinor = null;
    private String mAskedExtra = null;

    private int mRepeats = 0;


    private EnumSet<BatchConfigState> mBatchConfigState = EnumSet.noneOf(BatchConfigState.class);

    public enum BatchConfigState{
        STARTED, PAUSED, UPDATING, WAITING
    }

    public enum TxPowerSettings{
        NO_CHANGE("Do not change"), FROM_FILE("From File"), SEQUENTIAL("Sequential"), ASK("Ask every time"), CONSTANT("Constant");
        private String text;
        TxPowerSettings(String pText) {
            text = pText;
        }
        @Override public String toString() {
            return text;
        }
    }

    public enum AdvRateSetting{
        NO_CHANGE("Do not change"), FROM_FILE("From File"), SEQUENTIAL("Sequential"), ASK("Ask every time"), CONSTANT("Constant");
        private String text;
        AdvRateSetting(String pText) {
            text = pText;
        }
        @Override public String toString() {
            return text;
        }
    }

    public enum ExtraSetting {
        CONSTANT("Constant"), FROM_FILE("From File"), ASK("Ask every time");
        private String text;
        ExtraSetting(String pText) {
            text = pText;
        }
        @Override public String toString() {
            return text;
        }
    }

    public enum MinorSetting {
        NO_CHANGE("Do not change"), FROM_FILE("From File"), SEQUENTIAL("Sequential"), ASK("Ask every time"), CONSTANT("Constant");
        private String text;
        MinorSetting(String pText) {
            text = pText;
        }
        @Override public String toString() {
            return text;
        }
    }

    @NonNull
    @Override
    public String toString() {
        String toPrint = "BeaconConfigurator: {\n";
        toPrint += "TxPowerSetting: "+ mTxSettings +"\n";
        toPrint += "AdvRateSetting: "+ mAdvSettings +"\n";
        toPrint += "MinorSetting: "+ mMinorSettings +"\n";
        toPrint += "ExtraSettings: "+ mExtraSettings +"\n";
        toPrint += "Repeat: "+ mRepeats +" times\n";
        if(mConfigList != null && mConfigList.size() > 0){
            toPrint += "First Config in list: "+ mConfigList.get(0) +"\n";
        }
        else if(mMacConfigList != null && mMacConfigList.size() > 0){
            Map.Entry<String, BeaconConfig> entry =  mMacConfigList.entrySet().iterator().next();
            toPrint += "First Config in list: "+ entry.getValue() +"\n";
        }
        else{
            toPrint += "Starting Config: "+ mConfigData +"\n";
        }
        return toPrint;
    }

    public BeaconConfigurator(){
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
        mDateFormatLog = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mCompletedFileName = "BeaconConfig-"+mDateFormat.format(new Date(Calendar.getInstance().getTimeInMillis())) +".csv";

        // Find the root of the external storage.
        // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal
        File root = android.os.Environment.getExternalStorageDirectory();
        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
        File dir = new File (root.getAbsolutePath() + "/BeaconConfigurator");
        dir.mkdirs();
        mDataFile = new File(dir, mCompletedFileName);
        try {
            mDataFileWriter = new FileWriter(mDataFile, true);
            mDataFileCsvWriter = new CSVWriter(mDataFileWriter,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);
            mDataFileCsvWriter.writeNext(new String[]{"timestamp","time","mac","name","minor","advRate","txPower","battery","extra"});
            mDataFileCsvWriter.flush();
        }catch (Exception ex){}
        mTxSettings = TxPowerSettings.SEQUENTIAL;
        mAdvSettings = AdvRateSetting.SEQUENTIAL;
        mMinorSettings = MinorSetting.SEQUENTIAL;
        mExtraSettings = ExtraSetting.CONSTANT;
        mConfigData = new BeaconConfig();
    }

    public BeaconConfigurator(Uri pFileUri, Context context){
        this();
        mTxSettings = TxPowerSettings.FROM_FILE;
        mAdvSettings = AdvRateSetting.FROM_FILE;
        mMinorSettings = MinorSetting.FROM_FILE;
        mExtraSettings = ExtraSetting.FROM_FILE;
        mConfigData.setTxPowerIndex(0);
        mConfigData.setAdvRateIndex(0);
        mConfigData.setMinor(0);
        mConfigData.setExtra("");
        readConfigFromFile(pFileUri, context);
    }

    public BeaconConfigurator(Uri pFileUri, Context context, TxPowerSettings pTxSettings, Integer pTxIndex, AdvRateSetting pAdvSettings, Integer pAdvRateIndex, MinorSetting pMinorSettings, Integer pMinor, ExtraSetting pExtraSettings, String pExtra){
        this();
        mTxSettings = pTxSettings;
        mAdvSettings = pAdvSettings;
        mMinorSettings = pMinorSettings;
        mExtraSettings = pExtraSettings;
        mConfigData.setTxPowerIndex(pTxIndex);
        mConfigData.setAdvRateIndex(pAdvRateIndex);
        mConfigData.setMinor(pMinor);
        mConfigData.setExtra(pExtra);
        readConfigFromFile(pFileUri, context);
    }

    public BeaconConfigurator(TxPowerSettings pTxSettings, AdvRateSetting pAdvSettings, MinorSetting pMinorSettings, ExtraSetting pExtraSettings){
        this();
        mTxSettings = pTxSettings;
        mAdvSettings = pAdvSettings;
        mMinorSettings = pMinorSettings;
        mExtraSettings = pExtraSettings;
    }

    public void setRepeat(int pRepeat){
        mRepeats = pRepeat;
    }

    public BeaconConfig getInitialConfig(){
        return mConfigData;
    }

    public void addCompletedConfig(ScannedDeviceRecord beacon, BeaconConfig config){
        BeaconConfig clone = config.clone();
        clone.setMAC(beacon.getMac_beacon());
        Log.e(TAG, "Adding config " +clone);
        mCompletedConfig.add(clone);
        writeToFile(beacon, clone);
        Log.e(TAG, "WHATN");
    }

    public void writeToFile(ScannedDeviceRecord beacon, BeaconConfig config){
        try {
            mDataFileCsvWriter.writeNext(new String[]{
                    Calendar.getInstance().getTimeInMillis()+"",
                    mDateFormatLog.format(new Date(Calendar.getInstance().getTimeInMillis())),
                    config.getMAC()+"",
                    beacon.getRadName()+"",
                    config.getMinor()+"",
                    config.getAdvRate()+"",
                    config.getTxPower()+"",
                    beacon.getBatteryLevel()+"",
                    config.getExtra()+""
            });
            mDataFileCsvWriter.flush();
        }
        catch(Exception ex){
            Log.e(TAG, "ERROR!!: "+ ex.getMessage());
        }
    }

    public BeaconConfig getLast(){
        return mLastConfig;
    }

    public boolean needToAsk(){
        if(mTxSettings.equals(BeaconConfigurator.TxPowerSettings.ASK) && mAskedTxPowerIndex == null){
            return true;
        }
        if(mAdvSettings.equals(BeaconConfigurator.AdvRateSetting.ASK) && mAskedAdvRateIndex == null){
            return true;
        }
        if(mMinorSettings.equals(BeaconConfigurator.MinorSetting.ASK) && mAskedMinor == null){
            return true;
        }
        if(mExtraSettings.equals(ExtraSetting.ASK) && mAskedExtra == null){
            return true;
        }

        return false;
    }

    public void setAskedValues(Integer pNewTxIndex, Integer pNewAdvIndex, Integer pNewMinor, String pExtra){
        mAskedTxPowerIndex = pNewTxIndex;
        mAskedAdvRateIndex = pNewAdvIndex;
        mAskedMinor = pNewMinor;
        mAskedExtra = pExtra;
    }


    public BeaconConfig next(String mac){
        BeaconConfig config = null;
        if(mConfigList != null && mConfigList.size() > 0){
            if(mConfigListIterator == null) {
                mConfigListIterator = mConfigList.iterator();
            }
            if(!mConfigListIterator.hasNext()){
                if(mRepeats > 0){
                    mRepeats--;
                    mConfigListIterator = mConfigList.iterator();
                    config = mConfigListIterator.next();
                    setConfig(config, mConfigData);
                    mLastConfig = config;
                    return mLastConfig;
                }
                else{
                    return null;
                }
            }
            else{
                config = mConfigListIterator.next();
                setConfig(config, mConfigData);
                mLastConfig = config;
                return mLastConfig;
            }
        }
        else if(mMacConfigList != null && mMacConfigList.size() > 0){
            config = mMacConfigList.get(mac);
            //This will take care to override config data in case it was not from file
            setConfig(config, mConfigData);
            mLastConfig = config;
            return mLastConfig;
        }

        if(config == null) {
            config = new BeaconConfig();
        }

        setConfig(config, mConfigData);
        mLastConfig = config;
        return mLastConfig;
    }


    public void readConfigFromFile(Uri pFileUri, Context context){
        File configFile = new File(pFileUri.getPath());
        Reader reader = null;
        try {
            FileReader fileReader = new FileReader(configFile);
            reader = new BufferedReader(fileReader);
        }
        catch(FileNotFoundException exc){
            Log.e(TAG, "File " +configFile+" not found: "+exc.getMessage());
            try{
                reader = new InputStreamReader(context.getContentResolver().openInputStream(pFileUri));
            }
            catch(FileNotFoundException exc2) {
                Log.e(TAG, "Content File " + configFile + " not found: " + exc2.getMessage());
            }
        }

        CSVReader csvReader = new CSVReader(reader);
        String[] line = null;
        try {
            String[] header = csvReader.readNext();
            HashMap<String, Integer> indexMap = new HashMap<>();
            for(int i = 0; i < header.length; i++){
                if(header[i].toLowerCase().contains("mac")){
                    indexMap.put("mac", i);
                    mRepeats = 0;
                }
                else if(header[i].toLowerCase().contains("tx")){
                    indexMap.put("tx", i);
                }
                else if(header[i].toLowerCase().contains("adv")){
                    indexMap.put("adv", i);
                }
                else if(header[i].toLowerCase().contains("min")){
                    indexMap.put("min", i);
                }
                else if(header[i].toLowerCase().contains("xtr")){
                    indexMap.put("xtr", i);
                }
            }

            if(indexMap.containsKey("mac")){
                mMacConfigList = new HashMap<>();
                mConfigList = null;
            }
            else{
                mMacConfigList = null;
                mConfigList = new ArrayList<>();
            }

            while ((line = csvReader.readNext()) != null) {
                BeaconConfig config = new BeaconConfig();
                if(indexMap.containsKey("mac")) {
                    config.setMAC(line[indexMap.get("mac")]);
                }

                if(indexMap.containsKey("tx")) {
                    config.setTxPower(line[indexMap.get("tx")]);
                }
                else{
                    config.setTxPowerIndex(); //Do not change if not in the csv
                }

                if(indexMap.containsKey("adv")) {
                    config.setAdvRate(line[indexMap.get("adv")]);
                }
                else{
                    config.setAdvRateIndex(); //Do not change if not in the csv
                }

                if(indexMap.containsKey("min")) {
                    config.setMinor(line[indexMap.get("min")]);
                }
                else{
                    config.setMinor(); //Do not change if not in the csv
                }

                if(indexMap.containsKey("xtr")) {
                    config.setExtra(line[indexMap.get("xtr")]);
                }
                else{
                    config.setExtra("");
                }

                Log.e(TAG, config+"");
                if(mMacConfigList != null){
                    mMacConfigList.put(config.getMAC(), config);
                }
                else{
                    mConfigList.add(config);
                }
            }
        }catch (IOException exc){
            Log.e(TAG, "Error while reading csv file " +exc.getMessage());
        }
    }

    public TxPowerSettings getTxSettings(){
        return mTxSettings;
    }
    public void setTxSettings(TxPowerSettings pTxSettings){
        mTxSettings = pTxSettings;
    }
    public AdvRateSetting getAdvSettings(){
        return mAdvSettings;
    }
    public void setAdvSettings(AdvRateSetting pAdvSettings){
        mAdvSettings = pAdvSettings;
    }
    public MinorSetting getMinorSettings(){
        return mMinorSettings;
    }
    public void setMinorSettings(MinorSetting pMinorSettings){
        mMinorSettings = pMinorSettings;
    }
    public ExtraSetting getExtraSettings(){
        return mExtraSettings;
    }
    public void setExtraSettings(ExtraSetting pExtraSettings){
        mExtraSettings = pExtraSettings;
    }

    public void start(){
        mBatchConfigState = EnumSet.of(BatchConfigState.STARTED);
    }

    public void stop(){
        mBatchConfigState.clear();
    }

    public EnumSet<BatchConfigState> getStates(){
        return mBatchConfigState;
    }

    public boolean hasState(BatchConfigState state){
        return mBatchConfigState.contains(state);
    }

    public boolean addState(BatchConfigState state){
        return mBatchConfigState.add(state);
    }

    public boolean removeState(BatchConfigState state){
        return mBatchConfigState.remove(state);
    }

    public void clearStates(BatchConfigState state){
        mBatchConfigState.clear();
    }


    public static String beaconToString(ScannedDeviceRecord beacon){
        String str = "";
        String tx = ScannedDeviceRecord.DOT_TRANSMIT_POWER_VALUES[beacon.getRadBeaconTransmitPowerIndex()]+"";
        String adv = beacon.getAdvertisingRate() +"";
        String minor = beacon.getMinor()+"";
        str += "[ MAC: "+beacon.getMac_beacon() + ", Minor: " + minor +", Tx: " + tx + "dBm, Adv: " + adv + "hz]";
        return str;
    }


    public void setConfig(BeaconConfig config, BeaconConfig defaultConfig){
        switch (mTxSettings) {
            case NO_CHANGE: {
                config.setTxPowerIndex();
                break;
            }
            case CONSTANT: {
                config.setTxPowerIndex(defaultConfig.getTxPowerIndex());
                break;
            }
            case SEQUENTIAL: {
                config.setTxPowerIndex(defaultConfig.nextTxPowerIndex());
                break;
            }
            case ASK: {
                if(mAskedTxPowerIndex != null) {
                    config.setTxPowerIndex(mAskedTxPowerIndex);
                    mAskedTxPowerIndex = null;
                }
                break;
            }
        }

        switch (mAdvSettings) {
            case NO_CHANGE: {
                config.setAdvRateIndex();
                break;
            }
            case CONSTANT: {
                config.setAdvRateIndex(defaultConfig.getAdvRateIndex());
                break;
            }
            case SEQUENTIAL: {
                config.setAdvRateIndex(defaultConfig.nextAdvRateIndex());
                break;
            }
            case ASK: {
                if(mAskedAdvRateIndex != null) {
                    config.setAdvRateIndex(mAskedAdvRateIndex);
                    mAskedAdvRateIndex = null;
                }
                break;
            }
        }

        switch (mMinorSettings) {
            case NO_CHANGE: {
                config.setMinor();
                break;
            }
            case CONSTANT: {
                config.setMinor(defaultConfig.getMinor());
                break;
            }
            case SEQUENTIAL: {
                config.setMinor(defaultConfig.nextMinor());
                break;
            }
            case ASK: {
                if(mAskedMinor != null) {
                    config.setMinor(mAskedMinor);
                    mAskedMinor = null;
                }
                break;
            }
        }

        switch(mExtraSettings){
            case CONSTANT:{
                config.setExtra(defaultConfig.getExtra());
                break;
            }
            case ASK:{
                if(mAskedExtra != null){
                    config.setExtra(mAskedExtra);
                    mAskedExtra = null;
                }
                break;
            }
        }
    }
}
