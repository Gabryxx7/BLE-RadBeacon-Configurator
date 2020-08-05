package com.unimelb.marinig.bletracker.Utils;

import android.content.Context;
import android.os.Environment;

import com.unimelb.marinig.bletracker.Logger.TrackerLog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FileUtils {



    public static byte[] compress(String string) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
        GZIPOutputStream gos = new GZIPOutputStream(os);
        gos.write(string.getBytes());
        gos.close();
        byte[] compressed = os.toByteArray();
        os.close();
        return compressed;
    }

    public static String decompress(byte[] compressed) throws IOException {
        final int BUFFER_SIZE = 32;
        ByteArrayInputStream is = new ByteArrayInputStream(compressed);
        GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
        StringBuilder string = new StringBuilder();
        byte[] data = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = gis.read(data)) != -1) {
            string.append(new String(data, 0, bytesRead));
        }
        gis.close();
        is.close();
        return string.toString();
    }

    public static boolean decompressGzipFile(String gzipFile, String newFile) {
        try {
            FileInputStream fis = new FileInputStream(gzipFile);
            GZIPInputStream gis = new GZIPInputStream(fis);
            FileOutputStream fos = new FileOutputStream(newFile);
            byte[] buffer = new byte[1024];
            int len;
            while((len = gis.read(buffer)) != -1){
                fos.write(buffer, 0, len);
            }
            //close resources
            fos.close();
            gis.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            new File(gzipFile).delete();
            return false;
        }
    }

    public static boolean compressGzipFile(String file, String gzipFile) {
        try {
            FileInputStream fis = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream(gzipFile);
            GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
            byte[] buffer = new byte[1024];
            int len;
            while((len=fis.read(buffer)) != -1){
                gzipOS.write(buffer, 0, len);
            }
            //close resources
            gzipOS.close();
            fos.close();
            fis.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            new File(gzipFile).delete();
            return false;
        }
    }

    public static File getRoot(Context context, String childFolder){
        File mRoot;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            //Log.e("LOGTEST", "EXTERNAL");
            mRoot = new File(context.getExternalFilesDir(""), childFolder);
        } else {
            //Log.e("LOGTEST", "INTERNAL");
            mRoot = new File(context.getFilesDir(), childFolder);
        }

        checkCreateFolder(mRoot);

        return mRoot;
    }

    public static File getRoot(Context context){
        return getRoot(context, "");
    }

    public static void moveFile(File fileToMove, String toFolderPath){
        moveFile(fileToMove, new File(toFolderPath));
    }

    public static void moveFile(File fileToMove, File toFolderPathFile){
        try{
            //File toFolderFile = new File(mRoot.getAbsolutePath() +"/to_upload/");
            checkCreateFolder(toFolderPathFile);

            File movedFile =new File(toFolderPathFile.getAbsolutePath() +"/" +fileToMove.getName());
            fileToMove.renameTo(movedFile);
            TrackerLog.e("File", "file movedTo: " +fileToMove.getAbsolutePath() +" - " +movedFile.getAbsolutePath());

        }catch(Exception e){
            TrackerLog.e("File", "Error in moving file " +fileToMove.getName() +": " +e.getMessage());
            e.printStackTrace();
        }
    }

    public static void checkCreateFolder(String folderPath){
        checkCreateFolder(new File(folderPath));
    }

    public static void checkCreateFolder(File folder){
        if (!folder.exists()) {
            //Log.e("LOGTEST", "Creating Dir");
            if (!folder.mkdirs()) {
                //Log.e("LOGTEST", " Dir Not created");
            }
        }
    }

    public static String getFileExtension(File file){
        return getFileExtension(file.getName());
    }

    public static String getFileExtension(String fileName){
        return fileName.substring(fileName.lastIndexOf(".")).trim();
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
