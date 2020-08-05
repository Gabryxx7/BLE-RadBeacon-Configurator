/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.unimelb.marinig.bletracker.Logger;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/** Simple TextView which is used to output log data received through the LogNode interface.
 */
public class LogFile implements LogNode {

    // The next LogNode in the chain.
    LogNode mNext;

    private static final boolean mCleanFileBeforeWriting = true;

    private String mFileName = "trackerLog.txt";
    private static String mMaximumTimePeriod = "3 days";

    private File mLogFile;
    private FileWriter mFileWriter;

    public LogFile(Context context, String pFileName, String maximumTimePeriod) {
        if(pFileName != null)
            mFileName = pFileName;
        if(maximumTimePeriod != null)
            mMaximumTimePeriod = maximumTimePeriod;

        generateFileOnSD(context, mFileName);
    }

    public LogFile(Context context){
        generateFileOnSD(context, mFileName);
    }

    /**
     * Formats the log data and prints it out to the LogFile.
     * @param priority Log level of the data being logged.  Verbose, Error, etc.
     * @param tag Tag for for the log data.  Can be used to organize log statements.
     * @param msg The actual message to be logged. The actual message to be logged.
     * @param tr If an exception was thrown, this can be sent along for the logging facilities
     *           to extract and print useful information.
     */
    @Override
    public void println(int priority, String date, String tag, String msg, Throwable tr) {
        //Log.e("LOGTEST", "printLN of LogFile");

        String priorityStr = null;

        // For the purposes of this View, we want to print the priority as readable text.
        switch(priority) {
            case android.util.Log.VERBOSE:
                priorityStr = "VERBOSE";
                break;
            case android.util.Log.DEBUG:
                priorityStr = "DEBUG";
                break;
            case android.util.Log.INFO:
                priorityStr = "INFO";
                break;
            case android.util.Log.WARN:
                priorityStr = "WARN";
                break;
            case android.util.Log.ERROR:
                priorityStr = "ERROR";
                break;
            case android.util.Log.ASSERT:
                priorityStr = "ASSERT";
                break;
            default:
                break;
        }

        // Handily, the Log class has a facility for converting a stack trace into a usable string.
        String exceptionStr = null;
        if (tr != null) {
            exceptionStr = android.util.Log.getStackTraceString(tr);
        }

        // Take the priority, tag, message, and exception, and concatenate as necessary
        // into one usable line of text.
        final StringBuilder outputBuilder = new StringBuilder();

        appendIfNotNull(outputBuilder, date, "\t");
        appendIfNotNull(outputBuilder, priorityStr.substring(0,1), "/\t");
        appendIfNotNull(outputBuilder, tag, ": \t");
        appendIfNotNull(outputBuilder, msg, "\t");
        appendIfNotNull(outputBuilder, exceptionStr, "\t");

        // In case this was originally called from an AsyncTask or some other off-UI thread,
        // make sure the update occurs within the UI thread.
        appendToLog(outputBuilder.toString());


        if (mNext != null) {
            mNext.println(priority, date, tag, msg, tr);
        }

        //Log.e("LOGTEST", "PRINTING " + msg);
    }

    public LogNode getNext() {
        return mNext;
    }

    public void setNext(LogNode node) {
        mNext = node;
    }

    /** Takes a string and adds to it, with a separator, if the bit to be added isn't null. Since
     * the logger takes so many arguments that might be null, this method helps cut out some of the
     * agonizing tedium of writing the same 3 lines over and over.
     * @param source StringBuilder containing the text to append to.
     * @param addStr The String to append
     * @param delimiter The String to separate the source and appended strings. A tab or comma,
     *                  for instance.
     * @return The fully concatenated String as a StringBuilder
     */
    private StringBuilder appendIfNotNull(StringBuilder source, String addStr, String delimiter) {
        if (addStr != null) {
            if (addStr.length() == 0) {
                delimiter = "";
            }
            return source.append(addStr).append(delimiter);
        }
        return source;
    }

    /** Outputs the string as a new line of log data in the LogFile. */
    private void appendToLog(String s) {
        try{
            mFileWriter.append("\n" + s);
            mFileWriter.flush();
            //mFileWriter.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateFileOnSD(Context context, String pFileName) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
        pFileName = "TrackerLog" + dateFormat.format(new Date(Calendar.getInstance().getTimeInMillis())) +".txt";

        try {
            File root;
            if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
                Log.e("LOGTEST", "EXTERNAL");
                root = new File(context.getExternalFilesDir(""), "TrackerLog");
            }
            else {
                Log.e("LOGTEST", "INTERNAL");
                root = new File(context.getFilesDir(), "TrackerLog");
            }

            if (!root.exists()) {
                Log.e("LOGTEST", "Creating Dir");
                if(!root.mkdirs()){
                    Log.e("LOGTEST", " Dir Not created");
                }
            }

            mLogFile = new File(root, pFileName);
            Log.e("LOGTEST", "Creating File");
            if(mCleanFileBeforeWriting) {
                mFileWriter = new FileWriter(mLogFile, false);
                mFileWriter.write("");
                mFileWriter.flush();
                mFileWriter.close();
            }

            mFileWriter = new FileWriter(mLogFile, true);
            Log.e("LOGTEST", "Creating FileWriter");
        } catch (IOException e) {
            Log.e("LOGTEST", "FILE NOT CREATED");
            e.printStackTrace();
        }
    }
}
