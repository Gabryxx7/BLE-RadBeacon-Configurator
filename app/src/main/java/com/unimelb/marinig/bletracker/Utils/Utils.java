package com.unimelb.marinig.bletracker.Utils;

import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.bluetooth.le.ScanRecord;
import android.content.ComponentName;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.opencsv.CSVWriter;
import com.unimelb.marinig.bletracker.Logger.TrackerLog;
import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;
import com.unimelb.marinig.bletracker.Model.TrackerPreferences;
import com.unimelb.marinig.bletracker.Model.TrackerPreferences.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Utils {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static Gson gson = new Gson();

    public static UUID UUIDFrom16BitString(String paramString) { return UUID.fromString(String.format("0000%s-0000-1000-8000-00805F9B34FB", new Object[] { paramString })); }

    public static UUID UUIDFromBytes(byte[] paramArrayOfByte) {
        ByteBuffer byteBuffer2;
        ByteBuffer byteBuffer1 = (byteBuffer2 = ByteBuffer.wrap(paramArrayOfByte, 0, 8)).wrap(paramArrayOfByte, 8, 8);
        return new UUID(byteBuffer2.getLong(), byteBuffer1.getLong());
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] bytesFromUUID(UUID paramUUID) {
        byte[] arrayOfByte = new byte[16];
        ByteBuffer byteBuffer = ByteBuffer.wrap(arrayOfByte);
        byteBuffer.putLong(paramUUID.getMostSignificantBits());
        byteBuffer.putLong(paramUUID.getLeastSignificantBits());
        return arrayOfByte;
    }


    public static byte[] bytesFromChar(char paramChar) { return ByteBuffer.allocate(2).putChar(paramChar).array(); }

    public static byte[] bytesFromInt(int paramInt) { return ByteBuffer.allocate(4).putInt(paramInt).array(); }

    public static byte[] bytesFromShort(short paramShort) { return ByteBuffer.allocate(2).putShort(paramShort).array(); }


    public static byte[] bytesFromUUIDString(String paramString) { return bytesFromUUID(UUID.fromString(paramString)); }

    public static short shortFromBytes(byte[] paramArrayOfByte) { return ByteBuffer.wrap(paramArrayOfByte, 0, 2).getShort(); }

    public static String stringFromByteArray(byte[] paramArrayOfByte) {
        String str = null;
        if (paramArrayOfByte != null) {
            byte b;
            for (b = 0; b < paramArrayOfByte.length && paramArrayOfByte[b] != 0; b++);
            try {
                return new String(paramArrayOfByte, 0, b, "UTF-8");
            } catch (UnsupportedEncodingException paramArrayOfByte2) {
                return null;
            }
        }
        return str;
    }


    public static int getEddystoneType(byte[] scanRecord) {
        if (scanRecord[11] == 0x00)
            return ScannedDeviceRecord.TYPE_EDDYSTONE_UID;
        else if (scanRecord[11] == 0x10)
            return ScannedDeviceRecord.TYPE_EDDYSTONE_URL;
        else if (scanRecord[11] == 0x20)
            return ScannedDeviceRecord.TYPE_EDDYSTONE_TLM;
        else if (scanRecord[11] == 0x30)
            return ScannedDeviceRecord.TYPE_EDDYSTONE_EID;
        else
            return ScannedDeviceRecord.TYPE_DEVICE;
    }

    public static int getFrameType(byte[] scanRecord) {
        if (Utils.isBLE(scanRecord)) {
            if (Utils.isIBeacon(scanRecord)) {
                return ScannedDeviceRecord.TYPE_IBEACON;
            }else {
                if (Utils.isEddyStone(scanRecord))
                    return getEddystoneType(scanRecord);
                else
                    return ScannedDeviceRecord.TYPE_DEVICE;
            }
        } else
            return -1;
    }

    public static Boolean isBLE(byte[] scanRecord) {
        return scanRecord[0] == 0x02 && scanRecord[1] == 0x01 && scanRecord[2] == 0x06;
    }

    public static Boolean isEddyStone(byte[] scanRecord) {
//        return scanRecord[3] == 0x03
//                && scanRecord[4] == 0x03
//                && scanRecord[5] == 0xaa
//                && scanRecord[6] == 0xfe
//                && scanRecord[8] == 0x16
//                && scanRecord[9] == 0xaa
//                && scanRecord[10] == 0xfe;
        return Utils.byteArrayToHexString(Arrays.copyOfRange(scanRecord, 3, 7)).equals("0303aafe")
                && Utils.byteArrayToHexString(Arrays.copyOfRange(scanRecord, 8, 11)).equals("16aafe");
    }

    public static  Boolean isIBeacon(byte[] scanRecord) {
//        return scanRecord[3] == 0x1a
//                && scanRecord[4] == 0xff
//                && scanRecord[5] == 0x4c
//                && scanRecord[6] == 0x00;
        return Utils.byteArrayToHexString(Arrays.copyOfRange(scanRecord, 3, 7)).equals("1aff4c00");
    }

    public static boolean isUUIDInList(String UUID, String UUIDList){
        return isUUIDInList(UUID, UUIDList.split(","));
    }

    public static boolean isUUIDInList(String UUID, String[] UUIDList) {
        for (String namespaceID : UUIDList)
            if (namespaceID.toLowerCase().equals(UUID.toLowerCase()))
                return true;

        return false;
    }

    public static boolean isNamespaceInList(String namespace, String namespacesList){
        return isNamespaceInList(namespace, namespacesList.split(","));
    }

    public static boolean isNamespaceInList(String namespace, String[] namespacesList) {
        for (String namespaceID : namespacesList)
            if (namespaceID.toLowerCase().equals(namespace.toLowerCase()))
                return true;

        return false;
    }

    public static Gson getGson(){
        if (gson == null) {
            gson = new Gson();
        }

        return gson;
    }


    public static String getDeviceId(Context context){
        //DEVICE_BT_MAC_ADDRESS = mBluetoothAdapter.getAddress(); //THIS WILL RETURN the default mac address 02:00:00:00:00 since Android 6.0
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getImei();
            else
                return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        }
        catch (SecurityException e){
            TrackerLog.e("Utilities", "NO PERMISSION TO GET IMEI");
            return "UNKNOWN_DevID";
        }
    }


    public static boolean isMyServiceRunning(Context context, String className) {
        ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        //TrackerLog.d("MainActivity", "Class name: " +className +" Running: " +manager.getRunningServices(Integer.MAX_VALUE).size());
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (className.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String getAndroidID(Context context){
        return android.provider.Settings.Secure.getString(context.getApplicationContext().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    }


    public static String getNameFromAdvertisedData(byte[] advertisedData) {
        List<java.util.UUID> uuids = new ArrayList<>();
        String name = null;
        if( advertisedData == null ){
            return null;
        }

        ByteBuffer buffer = ByteBuffer.wrap(advertisedData).order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.remaining() > 2) {
            byte length = buffer.get();
            if (length == 0) break;

            byte type = buffer.get();
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (length >= 2) {
                        uuids.add(java.util.UUID.fromString(String.format(
                                "%08x-0000-1000-8000-00805f9b34fb", buffer.getShort())));
                        length -= 2;
                    }
                    break;
                case 0x06: // Partial list of 128-bit UUIDs
                case 0x07: // Complete list of 128-bit UUIDs
                    while (length >= 16) {
                        long lsb = buffer.getLong();
                        long msb = buffer.getLong();
                        uuids.add(new UUID(msb, lsb));
                        length -= 16;
                    }
                    break;
                case 0x09:
                    byte[] nameBytes = new byte[length-1];
                    buffer.get(nameBytes);
                    try {
                        name = new String(nameBytes, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    buffer.position(buffer.position() + length - 1);
                    break;
            }
        }
        return name;
    }

    public static String byteArrayToHexString(byte[] array) {
        StringBuffer hexString = new StringBuffer();
        for (byte b : array) {
            int intVal = b & 0xff;
            if (intVal < 0x10)
                hexString.append("0");
            hexString.append(Integer.toHexString(intVal));
        }
        return hexString.toString();

    }


    /**
     * Converts byte[] to an iBeacon {@link UUID}.
     * From http://stackoverflow.com/a/9855338.
     *
     * @param bytes Byte[] to convert
     * @return UUID
     */
    public static UUID bytesToUuid(@NonNull final byte[] bytes)
    {
        final char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ )
        {
            final int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        final String hex = new String(hexChars);

        return UUID.fromString(hex.substring(0, 8) + "-" +
                hex.substring(8, 12) + "-" +
                hex.substring(12, 16) + "-" +
                hex.substring(16, 20) + "-" +
                hex.substring(20, 32));
    }

    /**
     * Converts a {@link UUID} to a byte[]. This is used to create a {@link android.bluetooth.le.ScanFilter}.
     * From http://stackoverflow.com/questions/29664316/bluetooth-le-scan-filter-not-working.
     *
     * @param uuid UUID to convert to a byte[]
     * @return byte[]
     */
    public static byte[] UuidToByteArray(@NonNull final UUID uuid)
    {
        final String hex = uuid.toString().replace("-","");
        final int length = hex.length();
        final byte[] result = new byte[length / 2];

        for (int i = 0; i < length; i += 2)
        {
            result[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i+1), 16));
        }

        return result;
    }

    /**
     * Convert major or minor to hex byte[]. This is used to create a {@link android.bluetooth.le.ScanFilter}.
     *
     * @param value major or minor to convert to byte[]
     * @return byte[]
     */
    public static byte[] integerToByteArray(final int value)
    {
        final byte[] result = new byte[2];
        result[0] = (byte) (value / 256);
        result[1] = (byte) (value % 256);

        return result;
    }

    /**
     * Convert major and minor byte array to integer.
     *
     * @param byteArray that contains major and minor byte
     * @return integer value for major and minor
     */
    public static int byteArrayToInteger(final byte[] byteArray)
    {
        return (byteArray[0] & 0xff) * 0x100 + (byteArray[1] & 0xff);
    }

    public static void exportDeltasCsv(List<ScannedDeviceRecord> devices, Context context, Boolean isDelta_csv_isProjectBeacon) {
        TrackerPreferences mPreferences = new TrackerPreferences(context);
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+"/DeltaTesting";
        new File(baseDir).mkdirs();
        String fileName = "DeltaList_" + (new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss", Locale.getDefault())).format(new Date())+ ".csv";
        String filePath = baseDir + File.separator + fileName;
        File f = new File(filePath);
        CSVWriter writer = null;
        FileWriter mFileWriter;
        // File exist
        if (f.exists() && !f.isDirectory()) {
            try {
                mFileWriter = new FileWriter(filePath, true);
                writer = new CSVWriter(mFileWriter);
            }
            catch (Exception e){
                TrackerLog.e("CSV", "Error in opening csv file: " +e.getMessage());

            }
        } else {
            try {
                writer = new CSVWriter(new FileWriter(filePath));
            }
            catch (Exception e){
                TrackerLog.e("CSV", "Error in opening csv file: " +e.getMessage());
            }
        }

        List<String[]> data = new ArrayList<String[]>();
        data.add(new String[] {"beacon_mac", "minor", "instance", "bleType", "deltaStart", "deltaEnd", "deltaTime", "deltaStart_global", "deltaTime_global", "deltaRSSI", "TestCode"});
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss.SSS", Locale.getDefault());
        for(ScannedDeviceRecord dev : devices){
            if(!isDelta_csv_isProjectBeacon || isUUIDInList(dev.getUUID(), mPreferences.getString(Settings.SCAN_UUIDS)) ||  isNamespaceInList(dev.getNamespace_id(), mPreferences.getString(Settings.SCAN_EDDYSTONE_NAMESPACES))) {
                for (ScannedDeviceRecord.DeltaTime delta : dev.getDeltaList()) {
                    String deltaString = Long.toString(TimeUnit.MILLISECONDS.toSeconds(delta.deltaTime)) + "." + Long.toString(TimeUnit.MILLISECONDS.toMillis(delta.deltaTime));
                    String deltaString_global = Long.toString(TimeUnit.MILLISECONDS.toSeconds(delta.deltaTime_gobal)) + "." + Long.toString(TimeUnit.MILLISECONDS.toMillis(delta.deltaTime_gobal));
                    data.add(new String[]{dev.getMac_beacon(), Integer.toString(dev.getMinor()), dev.getInstance_id(), dev.getDeviceTypeName(), df.format(delta.deltaStart), df.format(delta.deltaEnd),
                            deltaString, df.format(delta.deltaStart_gobal), deltaString_global, Integer.toString(delta.RSSI), dev.getTest_code()});

                }
            }
        }
        writer.writeAll(data);

        //String[] data = {"Ship Name", "Scientist Name", "..."};
        //writer.writeNext(data);

        try {
            writer.close();
        }
        catch (Exception e){

        }

        TrackerLog.e("CSV", "Exported csv in: " +filePath);
        Toast.makeText(context, "Exported csv in: " +filePath, Toast.LENGTH_SHORT).show();

    }

///Found here: https://android.googlesource.com/platform/frameworks/base/+/414a486e4c721f0f8f9f86823a05422acb1c509f/core/java/android/bluetooth/BluetoothLeAdvertiseScanData.java#540
    /**
     * Get a {@link Parser} to parse the scan record byte array into {@link ScanRecord}.
     */
    public static Parser getParser() {
        return new Parser();
    }
    /**
     * A parser class used to parse a Bluetooth LE scan record to
     * {@link BluetoothLeAdvertiseScanData}. Note not all field types would be parsed.
     */
    public static final class Parser {
        private static final String PARSER_TAG = "BluetoothLeAdvertiseDataParser";
        // The following data type values are assigned by Bluetooth SIG.
        // For more details refer to Bluetooth 4.0 specification, Volume 3, Part C, Section 18.
        private static final int DATA_TYPE_FLAGS = 0x01;
        private static final int DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL = 0x02;
        private static final int DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE = 0x03;
        private static final int DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL = 0x04;
        private static final int DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE = 0x05;
        private static final int DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL = 0x06;
        private static final int DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE = 0x07;
        private static final int DATA_TYPE_LOCAL_NAME_SHORT = 0x08;
        private static final int DATA_TYPE_LOCAL_NAME_COMPLETE = 0x09;
        private static final int DATA_TYPE_TX_POWER_LEVEL = 0x0A;
        private static final int DATA_TYPE_SERVICE_DATA = 0x16;
        private static final int DATA_TYPE_MANUFACTURER_SPECIFIC_DATA = 0xFF;
        // Helper method to extract bytes from byte array.
        private static byte[] extractBytes(byte[] scanRecord, int start, int length) {
            byte[] bytes = new byte[length];
            System.arraycopy(scanRecord, start, bytes, 0, length);
            return bytes;
        }
        /**
         * Parse scan record to {@link BluetoothLeAdvertiseScanData.ScanRecord}.
         * <p>
         * The format is defined in Bluetooth 4.0 specification, Volume 3, Part C, Section 11
         * and 18.
         * <p>
         * All numerical multi-byte entities and values shall use little-endian
         * <strong>byte</strong> order.
         *
         * @param scanRecord The scan record of Bluetooth LE advertisement and/or scan response.
         */
        /* public ScannedDeviceRecord parseFromScanRecord(byte[] scanRecord) {
            if (scanRecord == null) {
                return null;
            }
            int currentPos = 0;
            int advertiseFlag = -1;
            List<ParcelUuid> serviceUuids = new ArrayList<ParcelUuid>();
            String localName = null;
            int txPowerLevel = Integer.MIN_VALUE;
            ParcelUuid serviceDataUuid = null;
            byte[] serviceData = null;
            int manufacturerId = -1;
            byte[] manufacturerSpecificData = null;
            try {
                while (currentPos < scanRecord.length) {
                    // length is unsigned int.
                    int length = scanRecord[currentPos++] & 0xFF;
                    if (length == 0) {
                        break;
                    }
                    // Note the length includes the length of the field type itself.
                    int dataLength = length - 1;
                    // fieldType is unsigned int.
                    int fieldType = scanRecord[currentPos++] & 0xFF;
                    switch (fieldType) {
                        case DATA_TYPE_FLAGS:
                            advertiseFlag = scanRecord[currentPos] & 0xFF;
                            break;
                        case DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL:
                        case DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE:
                            parseServiceUuid(scanRecord, currentPos,
                                    dataLength, BluetoothUuid.UUID_BYTES_16_BIT, serviceUuids);
                            break;
                        case DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL:
                        case DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE:
                            parseServiceUuid(scanRecord, currentPos, dataLength,
                                    BluetoothUuid.UUID_BYTES_32_BIT, serviceUuids);
                            break;
                        case DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL:
                        case DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE:
                            parseServiceUuid(scanRecord, currentPos, dataLength,
                                    BluetoothUuid.UUID_BYTES_128_BIT, serviceUuids);
                            break;
                        case DATA_TYPE_LOCAL_NAME_SHORT:
                        case DATA_TYPE_LOCAL_NAME_COMPLETE:
                            localName = new String(
                                    extractBytes(scanRecord, currentPos, dataLength));
                            break;
                        case DATA_TYPE_TX_POWER_LEVEL:
                            txPowerLevel = scanRecord[currentPos];
                            break;
                        case DATA_TYPE_SERVICE_DATA:
                            serviceData = extractBytes(scanRecord, currentPos, dataLength);
                            // The first two bytes of the service data are service data uuid.
                            int serviceUuidLength = BluetoothUuid.UUID_BYTES_16_BIT;
                            byte[] serviceDataUuidBytes = extractBytes(scanRecord, currentPos,
                                    serviceUuidLength);
                            serviceDataUuid = BluetoothUuid.parseUuidFrom(serviceDataUuidBytes);
                            break;
                        case DATA_TYPE_MANUFACTURER_SPECIFIC_DATA:
                            manufacturerSpecificData = extractBytes(scanRecord, currentPos,
                                    dataLength);
                            // The first two bytes of the manufacturer specific data are
                            // manufacturer ids in little endian.
                            manufacturerId = ((manufacturerSpecificData[1] & 0xFF) << 8) +
                                    (manufacturerSpecificData[0] & 0xFF);
                            break;
                        default:
                            // Just ignore, we don't handle such data type.
                            break;
                    }
                    currentPos += dataLength;
                }
                if (serviceUuids.isEmpty()) {
                    serviceUuids = null;
                }
                return new ScannedDeviceRecord(serviceUuids, serviceDataUuid, serviceData,
                        manufacturerId, manufacturerSpecificData, advertiseFlag, txPowerLevel,
                        localName);
            } catch (IndexOutOfBoundsException e) {
                Log.e(PARSER_TAG,
                        "unable to parse scan record: " + Arrays.toString(scanRecord));
                return null;
            }
        }
        // Parse service uuids.
        private int parseServiceUuid(byte[] scanRecord, int currentPos, int dataLength,
                                     int uuidLength, List<ParcelUuid> serviceUuids) {
            while (dataLength > 0) {
                byte[] uuidBytes = extractBytes(scanRecord, currentPos,
                        uuidLength);
                serviceUuids.add(BluetoothUuid.parseUuidFrom(uuidBytes));
                dataLength -= uuidLength;
                currentPos += uuidLength;
            }
            return currentPos;
        } */
    }
}
