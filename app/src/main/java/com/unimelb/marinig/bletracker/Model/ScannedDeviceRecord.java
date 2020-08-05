/**
 * Created by Yuan WANG (811006, yuanw8)
 * Project: The Optimization of Hospital Workflow
 * This file is the record class
 */

package com.unimelb.marinig.bletracker.Model;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.os.ParcelUuid;

import com.google.gson.annotations.SerializedName;
import com.unimelb.marinig.bletracker.Events.ReadBeaconEvent;
import com.unimelb.marinig.bletracker.Logger.TrackerLog;
import com.unimelb.marinig.bletracker.Utils.Calculator;
import com.unimelb.marinig.bletracker.Utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;


public class ScannedDeviceRecord {

    public class DeltaTime {
        public Long deltaStart;
        public Long deltaEnd;
        public Long deltaTime;
        public int RSSI;
        public Long deltaStart_gobal;
        public Long deltaTime_gobal;
    }

    public static class Comparators {
        //Other two ways of doing the same stuff!
        //public static final Comparator<ScannedDeviceRecord> NAME = Comparator.comparing(ScannedDeviceRecord::getUUID);
        public static final Comparator<ScannedDeviceRecord> NAME_ASC = (ScannedDeviceRecord sr1, ScannedDeviceRecord sr2) -> sr1.getUUID().compareTo(sr2.getUUID());

        public static Comparator<ScannedDeviceRecord> NAME_DESC = new Comparator<ScannedDeviceRecord>() {
            @Override
            public int compare(ScannedDeviceRecord sr1, ScannedDeviceRecord sr2) {
                return sr2.getUUID().compareTo(sr1.getUUID());
            }
        };

        public static Comparator<ScannedDeviceRecord> DEVICE_TYPE = new Comparator<ScannedDeviceRecord>() {
            @Override
            public int compare(ScannedDeviceRecord sr1, ScannedDeviceRecord sr2) {
                return sr1.getDeviceType() - sr2.getDeviceType(); //This works because the TYPE_BEACON is smaller so it will be first
            }
        };

        public static Comparator<ScannedDeviceRecord> RSSI = new Comparator<ScannedDeviceRecord>() {
            @Override
            public int compare(ScannedDeviceRecord sr1, ScannedDeviceRecord sr2) {
                return sr1.getRSSI() - sr2.getRSSI();
            }
        };

        public static Comparator<ScannedDeviceRecord> TIME_SCAN = new Comparator<ScannedDeviceRecord>() {
            @Override
            public int compare(ScannedDeviceRecord sr1, ScannedDeviceRecord sr2) {
                return (int) (sr1.timeStamp_scan_start - sr2.timeStamp_scan_start);
            }
        };

        public static Comparator<ScannedDeviceRecord> MAC = new Comparator<ScannedDeviceRecord>() {
            @Override
            public int compare(ScannedDeviceRecord sr1, ScannedDeviceRecord sr2) {
                return sr1.getMac_beacon().compareTo(sr2.getMac_beacon());
            }
        };

        public static Comparator<ScannedDeviceRecord> MAJOR = new Comparator<ScannedDeviceRecord>() {
            @Override
            public int compare(ScannedDeviceRecord sr1, ScannedDeviceRecord sr2) {
                return sr1.getMajor() - sr2.getMajor();
            }
        };

        public static Comparator<ScannedDeviceRecord> MINOR = new Comparator<ScannedDeviceRecord>() {
            @Override
            public int compare(ScannedDeviceRecord sr1, ScannedDeviceRecord sr2) {
                return sr1.getMinor() - sr2.getMinor();
            }
        };
    }


    public static final String RADBEACON_DEFAULT_UUID = "2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6";
    public static final String RADBEACON_DOT_GATT_SERVICE_UUID = "F0D582D8-C67A-4656-99C7-DF0AD1701EBC";
    public static final String RADBEACON_DOT_V11_GATT_SERVICE_UUID = "F0CEC428-2EBB-47AB-A753-0CE09E9FE64B";
    public static final String RADBEACON_V1_GATT_SERVICE_UUID = "0A89139F-BA2B-4003-A72D-18E332BE098C";
    public static final String RADBEACON_V2_GATT_SERVICE_UUID = "42f16149-2720-42a7-a8d2-9988af73de3c";
    public static final String RADBEACON_V32_GATT_SERVICE_UUID = "e48eb4a4-a2e4-4841-a82f-367cf20a82bc";
    public static final String RADBEACON_X4S_GATT_SERVICE_UUID = "890F5A8F-C586-4191-B002-35EEC9770000";
    public static final String RADBEACON_E_GATT_SERVICE_UUID = "18010000-ddbc-4617-924e-5b8dc925f111";
    public static final String RADBEACON_G_DOT_GATT_SERVICE_UUID = "f02d2e13-e630-4a8d-a381-3a55aca95823";
    public static final String RADBEACON_G_USB_GATT_SERVICE_UUID = "4f3edfe7-6f17-4f87-b2ee-ea2cdac0dd02";
    public static final String RADBEACON_KST_ALT_BEACON_GATT_SERVICE_UUID = "09863C4A-C6C1-48DD-BFB3-65F928D694B7";
    public static final String RADBEACON_KST_PARTICLE_GATT_SERVICE_UUID = "248E4F81-E46C-4762-BF3F-84069C5C3F09";
    public static final Integer[] DOT_TRANSMIT_POWER_VALUES = {-18, -15, -12, -9, -6, -3, 0 ,3};
    public static final Integer[] ADV_RATE_VALUES = new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    public transient static final short MANUFACTURER_DATA_APPLE_BEACON_CODE = 533;
    public transient static final short MANUFACTURER_DATA_RADIUS_BEACON_CODE = -16724;
    public transient static final short MANUFACTURER_DATA_RADIUS_TELEMETRY_CODE = 340;
    public transient static final short MANUFACTURER_ID_APPLE = 76;
    public transient static final short MANUFACTURER_ID_APPLE_RAW_BYTES = 19456;
    public transient static final short MANUFACTURER_ID_GOOGLE_RAW_BYTES = -21762;
    public transient static final short MANUFACTURER_ID_RADIUS = 280;
    public transient static final short MANUFACTURER_ID_RADIUS_RAW_BYTES = 6145;


    //Transient avoid the variable to show up in the JSON version of this class
    private transient final static String TAG = ScannedDeviceRecord.class.getSimpleName();
    public transient static final int TYPE_IBEACON = 0;
    public transient static final int TYPE_EDDYSTONE_UID = 1;
    public transient static final int TYPE_EDDYSTONE_URL = 2;
    public transient static final int TYPE_EDDYSTONE_EID = 3;
    public transient static final int TYPE_EDDYSTONE_TLM = 4;
    public transient static final int TYPE_DEVICE = 5;
    private transient int mDeviceType = TYPE_DEVICE;

    public transient static final String TYPE_NAME_IBEACON = "IBEACON";
    public transient static final String TYPE_NAME_EDDYSTONE_UID = "EDDYSTONE_UID";
    public transient static final String TYPE_NAME_EDDYSTONE_URL = "EDDYSTONE_URL";
    public transient static final String TYPE_NAME_EDDYSTONE_EID = "EDDYSTONE_EID";
    public transient static final String TYPE_NAME_EDDYSTONE_TLM = "EDDYSTONE_TLM";
    public transient static final String TYPE_NAME_DEVICE = "DEVICE";

    public transient static String TEST_CODE_DELTA = "delta";

    //    private byte[] uuisByte = new byte[16];
    private transient Long id_record = -1l;
    private transient boolean mIsConnected = false;
    public transient BluetoothDevice device;
    public transient Long availableTime = 0L;
    // TODO put string in
    private transient Long deltaStart_global = 0L;
    private transient ArrayList<DeltaTime> mDeltaList = new ArrayList<>();
    private transient byte[] scanRecordBytes;
    private transient ScanRecord scanRecord;
    public transient boolean isRadBeaconConfigurable = false;
    public transient boolean isBatchUpdated = false;
    private transient EnumSet<GattStatus> mGattStatus;

    public enum GattStatus {
        NONE, COMPLETED, READING, READ, UPDATING, UPDATED
    }

    /*************** RadBeacon data *********************/
    private String radModel = "Unknown";
    private String radVersion = "";
    private String radDeviceID = "";
    private String radName = null;
    private byte radSupportedActions;
    private short radAdvertisingInterval;
    private byte radAdvertisingRate = 1; //From 1 to 10
    private byte radTransmitPowerIndex = 0; //A index to get values from DOT_TRANSMIT_POWER_VALUES[]
    private byte radBeaconTypes;
    private byte radEddystoneUIDCalibratedPower;
    private byte radEddystoneURLCalibratedPower;
    private byte[] radEncodedURL;
    private String radHardwareAddress;
    private boolean radHasBattery;
    private byte radIBeaconCalibratedPower;
    private String radUUIDString;
    private byte[] radNamespaceID;
    private byte[] radInstanceID;

    public void setModel(String model) { this.radModel = model; }
    public void setVersion(String version) {this.radVersion = version; }
    public void setDeviceID(String deviceId) { this.radDeviceID = deviceId;}
    public void setSupportedActions(byte actions) { this.radSupportedActions = actions; }
    public void setAdvertisingInterval(short interval) { this.radAdvertisingInterval = interval; }
    public void setAdvertisingRate(byte advRate) { this.radAdvertisingRate = advRate; }
    public void setTransmitPowerIndex(byte txPw) { this.radTransmitPowerIndex = txPw; }
    public void setBeaconTypes(byte paramByte) { this.radBeaconTypes = paramByte; }
    public void setEddystoneUIDCalibratedPower(byte paramByte) { this.radEddystoneUIDCalibratedPower = paramByte; }
    public void setEddystoneURLCalibratedPower(byte paramByte) { this.radEddystoneURLCalibratedPower = paramByte; }
    public void setEncodedURL(byte[] paramArrayOfByte) { this.radEncodedURL = paramArrayOfByte; }
    public void setHardwareAddress(String paramString) { this.radHardwareAddress = paramString; }
    public void setHasBattery(boolean paramBoolean) { this.radHasBattery = paramBoolean; }
    public void setIBeaconCalibratedPower(byte paramByte) { this.radIBeaconCalibratedPower = paramByte; }
    public void setUUIDString(String paramString) { this.radUUIDString = paramString; }
    public void setNamespaceID(byte[] paramArrayOfByte) { this.radNamespaceID = paramArrayOfByte; }
    public void setInstanceID(byte[] paramArrayOfByte) { this.radInstanceID = paramArrayOfByte; }
    public void setRadName(String name) { this.radName = name;}

    public void setInstanceID(long paramLong) {
        byte[] arrayOfByte = new byte[6];
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.putLong(paramLong);
        System.arraycopy(byteBuffer.array(), 2, arrayOfByte, 0, 6);
        setInstanceID(arrayOfByte);
    }

    public byte getAdvertisingRate(){ return this.radAdvertisingRate;}
    public short getAdvertisingInterval(){ return this.radAdvertisingInterval;}
    public byte getRadBeaconTransmitPowerIndex(){ return this.radTransmitPowerIndex;}
    public String getUUIDString(){ return this.radUUIDString;}
    public byte getIBeaconCalibratedPower() {return this.radIBeaconCalibratedPower;}
    public byte getEddystoneUIDCalibratedPower() {return this.radEddystoneUIDCalibratedPower;}
    public byte getEddystoneURLCalibratedPower() {return this.radEddystoneURLCalibratedPower;}
    public byte[] getNamespaceID() {return this.radNamespaceID;}
    public byte[] getInstanceID() {return this.radInstanceID;}
    public byte[] getEncodedURL() {return this.radEncodedURL;}
    public byte getBeaconTypes() {return this.radBeaconTypes;}
    public String getRadName() {return this.radName;}


    /************** End RadBeacon Data  *****************/


    @SerializedName("scan_count")
    private int scanCountPerScanPeriod = 1;

    @SerializedName("device_type")
    private String mDeviceTypeName = TYPE_NAME_DEVICE;

    @SerializedName("available")
    private boolean mIsAvailable = true;

    @SerializedName("name")
    private String name = "No name";

    @SerializedName("uuid")
    private String UUID = "UNKNOWN";

    @SerializedName("mac_beacon")
    private String mac_beacon = "UNKNOWN";

    @SerializedName("major")
    private Integer major = -1;

    @SerializedName("minor")
    private Integer minor = -1;

    @SerializedName("txPower")
    private Integer txPower = -55;

    @SerializedName("rssi")
    private Integer RSSI = -1;

    @SerializedName("distance")
    private Double distance = -1.0;

    @SerializedName("timestamp_scan")
    private Long timeStamp_scan = 0L;

    @SerializedName("timestamp_scan_start")
    private Long timeStamp_scan_start = 0L;

    @SerializedName("mac_reader")
    private String android_id = "UNKNOWN";

    @SerializedName("namespace_id")
    private String namespace_id = "UNKNOWN";

    @SerializedName("instance_id")
    private String instance_id = "UNKNOWN";

    @SerializedName("test_code")
    private String test_code = "none";

    @SerializedName("battery_level")
    private int batteryLevel = -1;

    public ScannedDeviceRecord() {
    }

    //So the BluetoothDevice object has some info but if they are update constantly or cached so we should keep a reference to the object
    //and check from time to time or, let's say, when we update the view cause the same bluetooth device has been found we should update its data
    public ScannedDeviceRecord(BluetoothDevice device, int mRssi, ScanRecord pScanRecord, byte[] pScanRecordBytes, String androidId, Long timeStamp_scan_start_global) {
        this.device = device; //So either we hold a reference to the BluetoothDevice, or we comment this line and uncomment the next one to try and get the Bt device name from the advertised data
        //But the second approach cannot always get the full name, this is because the data may be bigger than expected and requires multiple packages to be sent to the device and to be parse correctly
        //Read HERE: https://stackoverflow.com/questions/26290640/android-bluetoothdevice-getname-return-null
        //THe BluetoothLeScanner use ALWAYS the same BluetoothDevice, in fact it "caches" them, so whenever we get the BluetoothDevice object as a result from the scan callback
        //It's actually the same BluetoothDevice that we got previously for this same object, but this time it might have updated data!
        //this.setName(Utils.getNameFromAdvertisedData(scanRecord));
        //this.setName(Utils.getNameFromAdvertisedData(scanRecord));
        mGattStatus = EnumSet.noneOf(GattStatus.class);
        mDeviceType = Utils.getFrameType(pScanRecordBytes);
//        TrackerLog.d(TAG, "frame_HEX: " + byteArrayToHexString(scanRecord));
//        TrackerLog.d(TAG, "mDeviceType: " + mDeviceType);
        this.android_id = androidId;
        this.RSSI = mRssi;
        this.setName(device.getName());
        if(device.getName() == null) {
            this.setName(pScanRecord.getDeviceName()); //To avoid "null" names
        }
//        this.accuracy = Calculator.calculateAccuracy(this.txPower, this.RSSI);
        this.mac_beacon = device.getAddress();
        this.setTimeStamp_scan(Calendar.getInstance().getTimeInMillis(), timeStamp_scan_start_global);
        //this.test_delta = getTimestamp_scan() - getLatestTimestamp(); //We are already doing this inside setTimeStamp_scan
        setDeviceSpecificValues(pScanRecordBytes);

        setManifacturerSpecificData(pScanRecord);
        getServicesUUIDS();
    }

    private void setDeviceSpecificValues(byte[] scanRecordBytes){
        this.scanRecordBytes = scanRecordBytes;
        switch (mDeviceType) {
            case TYPE_IBEACON:
                mDeviceTypeName = TYPE_NAME_IBEACON;
                setValue_IBeacon(scanRecordBytes);
                break;
            case TYPE_EDDYSTONE_UID:
                mDeviceTypeName = TYPE_NAME_EDDYSTONE_UID;
                setValue_EddystoneUID(scanRecordBytes);
                break;
            case TYPE_EDDYSTONE_URL:
                mDeviceTypeName = TYPE_NAME_EDDYSTONE_URL;
                setValue_EddystoneURL(scanRecordBytes);
                break;
            case TYPE_EDDYSTONE_EID:
                mDeviceTypeName = TYPE_NAME_EDDYSTONE_EID;
                setValue_EddystoneEID(scanRecordBytes);
                break;
            case TYPE_EDDYSTONE_TLM:
                mDeviceTypeName = TYPE_NAME_EDDYSTONE_TLM;
                setValue_EddystoneTLM(scanRecordBytes);
                break;
        }
    }

    private static String bytesToHex(byte[] hashInBytes) {

        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            sb.append(String.format("%02x", b));
            sb.append(" ");
        }
        return sb.toString();

    }

    public void setManifacturerSpecificData(ScanRecord pScanRecord){
        this.scanRecord = pScanRecord;
        if(pScanRecord != null){
            byte[] bytes = pScanRecord.getBytes();
            byte[] arrayOfByte = pScanRecord.getManufacturerSpecificData(MANUFACTURER_ID_RADIUS);
            if (arrayOfByte != null) {
                ByteBuffer byteBuffer = ByteBuffer.wrap(arrayOfByte);
//                TrackerLog.e(TAG, "Byte Buffer " + bytesToHex(bytes));
//                TrackerLog.e(TAG, "Manuf Buffer " + bytesToHex(arrayOfByte));
                short s1 = byteBuffer.getShort(0);
                short s2 = byteBuffer.getShort(12);
                //java.util.UUID uUID = ((ParcelUuid) pScanRecord.getServiceUuids().get(0)).getUuid();
                this.batteryLevel = (int) bytes[15];
                //TrackerLog.e(TAG, "Battery Level " + this.mac_beacon + " : " + this.batteryLevel);
                //StringBuilder stringBuilder = new StringBuilder();
                //int i = arrayOfByte.length;
                //byte b;
                //for (b = 0; b < i; b++) {
                //    stringBuilder.append(String.format("%02x", new Object[] { Byte.valueOf(arrayOfByte[b]) }));
                //}
                //String str1;
                //String str2;
                //String str3;
                //String str4 = (str3 = (str2 = (str1 = String.format("Manufacturer Data: %s", new Object[] { stringBuilder })).format("Manufacturer Id: %x", new Object[] { Short.valueOf(s2) })).format("Beacon Code: %x", new Object[] { Short.valueOf(s1) })).format("Service UUID:%s", new Object[] { uUID.toString() });
                //TrackerLog.e(TAG, str1);
                //TrackerLog.e(TAG, str2);
                //TrackerLog.e(TAG, str3);
                //TrackerLog.e(TAG, str4);
            }
        }
    }

    public void setId(long id) {
        this.id_record = id;
    }

    public long getId() {
        return this.id_record;
    }

    public void setName(String name) {
        if (name != null && !name.trim().toLowerCase().equals("null"))
            this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setUUID(java.util.UUID UUID) {
        this.UUID = UUID.toString();
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getUUID() {
        return UUID;
    }

    public void setAndroid_id(String mac_scanner) {
        this.android_id = mac_scanner;
    }

    public String getAndroid_id() {
        return android_id;
    }

    public void setMac_beacon(String mac_beacon) {
        this.mac_beacon = mac_beacon;
    }

    public String getMac_beacon() {
        return mac_beacon;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMajor() {
        return major;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getMinor() {
        return minor;
    }

    public void setTxPower(int txPower) {
        this.txPower = txPower;
    }

    public int getTxPower() {
        return txPower;
    }

    public void setRSSI(int RSSI) {
        this.RSSI = RSSI;
    }

    public int getRSSI() {
        return RSSI;
    }

    public void setBatteryLevel(int pBatteryLevel) {
        this.batteryLevel = pBatteryLevel;
    }

    public int getBatteryLevel() {
        return this.batteryLevel;
    }

    public void setDistance(double distance) {
        if (Double.isFinite(distance))
            this.distance = distance;
        else
            this.distance = -1.0;
    }

    public double getDistance() {
        return distance;
    }

    public void setTimeStamp_scan(long timeStamp_scan, long timeStamp_scan_start_global) {
        setTimeStamp_scan(timeStamp_scan);

        /*if(mDeltaList.size() > 0) {
            DeltaTime lastDelta = mDeltaList.get(mDeltaList.size() - 1);
            lastDelta.deltaStart_gobal = timeStamp_scan_start_global;
            lastDelta.deltaTime_gobal = timeStamp_scan - timeStamp_scan_start_global;
        }

        this.deltaStart_global = timeStamp_scan_start_global;*/
    }

    public void setTimeStamp_scan(long timeStamp_scan) {
        /*if (this.timeStamp_scan == 0) {
            mDeltaList = new ArrayList<>();
        } else {
            DeltaTime delta = new DeltaTime();
            delta.deltaStart = this.timeStamp_scan;
            delta.deltaEnd = timeStamp_scan;
            delta.deltaTime = timeStamp_scan - this.timeStamp_scan;
            delta.RSSI = getRSSI();
            mDeltaList.add(delta);
            //TrackerLog.d(TAG, "delta list: " + mDeltaList.toString());
        }*/
        this.timeStamp_scan = timeStamp_scan;
    }

    public long getTimestamp_scan() {
        return timeStamp_scan;
    }

    public List<DeltaTime> getDeltaList() {
        return mDeltaList;
    }

    public Long getLastDelta(){
        return mDeltaList.get(mDeltaList.size() -1).deltaTime;
    }

    public void setDeviceType(int deviceType) {
        this.mDeviceType = deviceType;
    }

    public int getDeviceType() {
        return mDeviceType;
    }

    public String getDeviceTypeName() {
        return mDeviceTypeName;
    }

    public void setInstance_id(String instance_id) {
        this.instance_id = instance_id;
    }

    public String getInstance_id() {
        return instance_id;
    }

    public String getNamespace_id() {
        return namespace_id;
    }

    public void setNamespace_id(String namespace_id) {
        this.namespace_id = namespace_id;
    }

    public String getTest_code() {
        return test_code;
    }

    public void setTest_code(String test_code) {
        this.test_code = test_code;
    }

    public Long getDeltaStart_global() {
        return deltaStart_global;
    }

    public void setDeltaStart_global(Long deltaStart_global) {
        this.deltaStart_global = deltaStart_global;
    }

    // still not per scan period, simply count
    public int getScanCountPerScanPeriod() {
        return scanCountPerScanPeriod;
    }

    public void updateScanCountPerScanPeriod() {
        this.scanCountPerScanPeriod += 1;
    }

    public void resetScanCountPerScanPeriod() {
//        TODO resetScanCountPerScanPeriod
        this.scanCountPerScanPeriod = 0;
    }

    public Long getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(Long availableTime) {
        this.availableTime = availableTime;
    }

    // have some bugs
    public Long updateAvailableTime() {
        this.availableTime = (timeStamp_scan - timeStamp_scan_start);
        return this.availableTime;
    }

//    public boolean isBeacon(){
//        return this.mDeviceType == TYPE_IBEACON || this.mDeviceType == TYPE_EDDYSTONE_UID;
//    }
    public boolean isIbeacon() {
        return this.mDeviceType == ScannedDeviceRecord.TYPE_IBEACON;
    }

    public boolean isEddystone_UID() {
        return this.mDeviceType == ScannedDeviceRecord.TYPE_EDDYSTONE_UID;
    }

    public boolean isAvailable(Long timeBeforeUnavailable) {
        if (mIsAvailable){
            if(Calendar.getInstance().getTimeInMillis() - timeStamp_scan > timeBeforeUnavailable) {
                setAvailable(false);
            }
        }
        return mIsAvailable;
    }

    public boolean isAvailable() {
        return isAvailable(5000L);
    }

    public void setAvailable(boolean available) {
        this.mIsAvailable = available;
        if (available) {
            if (timeStamp_scan_start == 0L)
                timeStamp_scan_start = timeStamp_scan;
        } else {
            resetScanCountPerScanPeriod();
            timeStamp_scan_start = 0L;
            availableTime = -1L;
        }
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    public void setConnected(boolean connected) {
        this.mIsConnected = connected;
    }

    public ScannedDeviceRecord clone(){
        ScannedDeviceRecord newDevice = new ScannedDeviceRecord();
        newDevice.name = this.name;
        newDevice.minor = this.minor;
        newDevice.major = this.major;
        newDevice.txPower = this.txPower;
        newDevice.batteryLevel = this.batteryLevel;
        newDevice.isRadBeaconConfigurable = this.isRadBeaconConfigurable;
        newDevice.android_id = this.android_id;
        newDevice.availableTime = this.availableTime;
        newDevice.mac_beacon = this.mac_beacon;
        newDevice.device = this.device;
        newDevice.mGattStatus = this.mGattStatus.clone();
        newDevice.timeStamp_scan = this.timeStamp_scan;
        newDevice.mDeviceType = this.mDeviceType;
        newDevice.mDeviceTypeName = this.mDeviceTypeName;
        newDevice.mIsAvailable = this.mIsAvailable;
        newDevice.mIsConnected = this.mIsConnected;
        newDevice.scanRecord = this.scanRecord;
        newDevice.RSSI = this.RSSI;
        newDevice.UUID = this.UUID;


        newDevice.radAdvertisingInterval = this.radAdvertisingInterval;
        newDevice.radAdvertisingRate = this.radAdvertisingRate;
        newDevice.radAdvertisingInterval = this.radAdvertisingRate;
        newDevice.radBeaconTypes = this.radBeaconTypes;
        newDevice.radDeviceID = this.radDeviceID;
        newDevice.radEddystoneUIDCalibratedPower = this.radEddystoneUIDCalibratedPower;
        newDevice.radEddystoneURLCalibratedPower = this.radEddystoneURLCalibratedPower;
        newDevice.radEncodedURL = this.radEncodedURL;
        newDevice.radHardwareAddress = this.radHardwareAddress;
        newDevice.radHasBattery = this.radHasBattery;
        newDevice.radInstanceID = this.radInstanceID;
        newDevice.radIBeaconCalibratedPower = this.radIBeaconCalibratedPower;
        newDevice.radModel = this.radModel;
        newDevice.radName = this.radName;
        newDevice.radNamespaceID = this.radNamespaceID;
        newDevice.radSupportedActions = this.radSupportedActions;
        newDevice.radVersion = this.radVersion;
        newDevice.radTransmitPowerIndex = this.radTransmitPowerIndex;
        newDevice.radUUIDString = this.radUUIDString;

        return newDevice;
    }

    public void updateDeviceData(ScannedDeviceRecord otherDevice) {
        //This is necessary because the BluetoothDevice object passed at the moment of the record creation might not have the name
        //In that moment, this is due do the advertisement  and scan-response data, so if we are scanning again the same device
        //We.ll try to get its name again, hopefully we'll get the name now!
        //The line below can be used if we want to keep a reference to this object, otherwise we can parse the advertised data and comment the line below
        this.setName(this.device.getName());
        this.RSSI = otherDevice.RSSI;
        this.distance = otherDevice.distance;
        this.updateAvailableTime();
        this.setTimeStamp_scan(otherDevice.timeStamp_scan, otherDevice.deltaStart_global);
        this.updateScanCountPerScanPeriod();
    }

    public void updateDeviceData(String name, Integer rssi, ScanRecord scanRecord, byte[] scanRecordBytes, long timeStamp_scan_start_global) {
        //This is necessary because the BluetoothDevice object passed at the moment of the record creation might not have the name
        //In that moment, this is due do the advertisement  and scan-response data, so if we are scanning again the same device
        //We.ll try to get its name again, hopefully we'll get the name now!
        //The line below can be used if we want to keep a reference to this object, otherwise we can parse the advertised data and comment the line below
        this.setName(name);
        this.RSSI = rssi;
        this.updateAvailableTime();
        this.setTimeStamp_scan(Calendar.getInstance().getTimeInMillis(), timeStamp_scan_start_global);
        this.updateScanCountPerScanPeriod();
        setDeviceSpecificValues(scanRecordBytes);
        setManifacturerSpecificData(scanRecord);
        getServicesUUIDS();
    }

    //This is to be able to test if a BluetoothDevice object is equal to a ScannedDeviceRecord object
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ScannedDeviceRecord) {
            return mac_beacon.equals(((ScannedDeviceRecord) obj).getMac_beacon());
        } else if (obj instanceof BluetoothDevice) {
            return mac_beacon.equals(((BluetoothDevice) obj).getAddress());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mac_beacon.hashCode();
    }


    private void setValue_IBeacon(byte[] scanRecord) {
        this.UUID = String.valueOf(Utils.bytesToUuid(Arrays.copyOfRange(scanRecord, 9, 25)));
        this.major = (scanRecord[25] & 0xff) * 0x100 + (scanRecord[26] & 0xff);
        this.minor = (scanRecord[27] & 0xff) * 0x100 + (scanRecord[28] & 0xff);
        this.txPower = (int) scanRecord[29];
        this.distance = Calculator.calculateDistance(this.txPower, this.RSSI, false);
//        Log.d(TAG, "DISTENCE: " + this.txPower + " " + this.RSSI + " " + this.distance);
    }

    private void setValue_EddystoneUID(byte[] scanRecord) {
        this.namespace_id = Utils.byteArrayToHexString(Arrays.copyOfRange(scanRecord, 13, 23));
        this.instance_id = Utils.byteArrayToHexString(Arrays.copyOfRange(scanRecord, 23, 29));
        this.txPower = (int) scanRecord[12];
        this.distance = Calculator.calculateDistance(this.txPower, this.RSSI, true);
//        Log.d(TAG, "DISTENCE: " + this.txPower + " " + this.RSSI + " " + this.distance);
    }

    private void setValue_EddystoneURL(byte[] scanRecord) {
//        TODO setValue_EddystoneURL
    }

    private void setValue_EddystoneEID(byte[] scanRecord) {
//        TODO setValue_EddystoneEID
    }

    private void setValue_EddystoneTLM(byte[] scanRecord) {
//        TODO setValue_EddystoneTLM
    }

    private void setValue_default(BluetoothDevice device, int RSSI, String android_id, Long timeStamp_scan_start_global) {
        this.android_id = android_id;
        this.RSSI = RSSI;
        this.setName(device.getName()); //To avoid "null" names
//        this.accuracy = Calculator.calculateAccuracy(this.txPower, this.RSSI);
        this.mac_beacon = device.getAddress();
        this.setTimeStamp_scan(Calendar.getInstance().getTimeInMillis(), timeStamp_scan_start_global);
        //this.test_delta = getTimestamp_scan() - getLatestTimestamp(); //We are already doing this inside setTimeStamp_scan

    }

    private void getServicesUUIDS(){
        List<ParcelUuid> uuidList = this.scanRecord.getServiceUuids();
        ParcelUuid[] uuids = this.device.getUuids();
        String uuidsString = "";
        if(uuids != null) {
            for (int i = 0; i < uuids.length; i++) {
                uuidsString += uuids[i] + "\n";
            }
            //TrackerLog.e(TAG, "UUIDS " + this.getMinor() + " " + uuidsString);
        }
        else{
            //TrackerLog.e(TAG, "NO UUIDS " + this.getMinor());
        }

        uuidsString = "";
        if(uuidList != null) {
            for (int i = 0; i < uuidList.size(); i++) {
                uuidsString += uuidList.get(i) + "\n";
            }
            //TrackerLog.e(TAG, "Service UUIDS " + this.getMinor() + " " + uuidsString);
        }
        else{
            //TrackerLog.e(TAG, "NO Service UUIDS " + this.getMinor());
        }

//        boolean wasConfigurable = this.isRadBeaconConfigurable;
        this.isRadBeaconConfigurable =  (uuidList == null) ? false : uuidList.contains(new ParcelUuid(java.util.UUID.fromString(RADBEACON_DOT_V11_GATT_SERVICE_UUID)));

    }

    private Long getLatestTimestamp() {
        //TODO select db to get latest ts (same device / not same devicess)
        return 0L;
    }

    public static String getDeviceTypeName(int deviceTypeCode) {
        switch (deviceTypeCode) {
            case TYPE_IBEACON:
                return TYPE_NAME_IBEACON;
            case TYPE_EDDYSTONE_UID:
                return TYPE_NAME_EDDYSTONE_UID;
            case TYPE_EDDYSTONE_URL:
                return TYPE_NAME_EDDYSTONE_URL;
            case TYPE_EDDYSTONE_EID:
                return TYPE_NAME_EDDYSTONE_EID;
            case TYPE_EDDYSTONE_TLM:
                return TYPE_NAME_EDDYSTONE_TLM;
        }
        return "UNKNOWN";
    }

    public boolean addGattStatus(GattStatus newStatus){
        return mGattStatus.add(newStatus);
    }

    public boolean setGattStatus(GattStatus status, boolean state){
        if(state) return addGattStatus(status);
        return removeGattStatus(status);
    }

    public boolean removeGattStatus(GattStatus newStatus){
        return mGattStatus.remove(newStatus);
    }

    public boolean hasGattStatus(GattStatus newStatus){
        return mGattStatus.contains(newStatus);
    }
}
