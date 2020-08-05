/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.unimelb.marinig.bletracker.Fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.unimelb.marinig.bletracker.Interfaces.ToolbarFragment;
import com.unimelb.marinig.bletracker.Logger.LogFile;
import com.unimelb.marinig.bletracker.Logger.LogView;
import com.unimelb.marinig.bletracker.Logger.TrackerLog;
import com.unimelb.marinig.bletracker.Model.TrackerPreferences;
import com.unimelb.marinig.bletracker.R;

/**
 * Simple fraggment which contains a LogView and uses is to output log data it receives
 * through the LogNode interface.
 */
public class TrackerLogFragment extends ToolbarFragment {

    private LogView mLogView;
    private ScrollView mScrollView;
    private View mFragmentView;
    private TrackerPreferences mPreferences;

    public TrackerLogFragment() {
        Log.e("LOGTEST", "New trackerlog fragment");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TrackerLog.d("Fragments", "Creating Fragment");
        mPreferences = new TrackerPreferences(getContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //BLETrackerApplication.getRefWatcher(getActivity()).watch(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        TrackerLog.e("LOGTEST", "Saving instance state");
        outState.putCharSequence("log", mLogView.getText());
    }


    public View inflateViews() {
        mScrollView = new ScrollView(getActivity());
        ViewGroup.LayoutParams scrollParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mScrollView.setLayoutParams(scrollParams);
        mScrollView.setFillViewport(true);


        mLogView = new LogView(getContext());
        TrackerLog.e("LOGTEST", "Creating mLogView");
        ViewGroup.LayoutParams logParams = new ViewGroup.LayoutParams(scrollParams);
        logParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        mLogView.setLayoutParams(logParams);
        mLogView.setClickable(true);
        mLogView.setFocusable(true);
        mLogView.setTypeface(Typeface.MONOSPACE);

        // Want to set padding as 16 dips, setPadding takes pixels.  Hooray math!
        int paddingDips = 16;
        double scale = getResources().getDisplayMetrics().density;
        int paddingPixels = (int) ((paddingDips * (scale)) + .5);
        mLogView.setPadding(paddingPixels, paddingPixels, paddingPixels, paddingPixels);
        mLogView.setCompoundDrawablePadding(paddingPixels);

        mLogView.setGravity(Gravity.BOTTOM);
        mLogView.setTextSize(12);
        mLogView.setTextColor(Color.parseColor("#333333"));

        mScrollView.addView(mLogView);

        return mScrollView;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mFragmentView == null) {
            Log.e("LOGTEST", "Creating fragment view");
            View result = inflater.inflate(R.layout.fragment_log, container, false);
            LinearLayout logLayout = result.findViewById(R.id.log_linear_layout);
            logLayout.setBackgroundColor(Color.parseColor("#EAEAEA"));
            View scrollView = inflateViews();
            if (savedInstanceState != null && savedInstanceState.containsKey("log"))
                mLogView.setText(savedInstanceState.getCharSequence("log"));

            mLogView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });

            //It works, so why doesn't it show any message?
            //TextView Paper = new TextView(logLayout.getContext());
            //Paper.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            //Paper.setText("Inserted TestText");
            //logLayout.addView(Paper);
            logLayout.addView(scrollView);
            mFragmentView = result;

            //TrackerLog.printNodeList("NODE_LIST3");
            //So now that the Activity is starting and all the Fragment elements have been created
            //I can attach the LogViewer in the LogFragment view and be sure that it will be already created and not null

            //LogNode node = TrackerLog.getLogNode();
            //while(node.getNext() != null && !(node.getNext() instanceof LogFile)){
            //    node = node.getNext();
            //}

            //LogNode temp = node.getNext();
            //node.setNext(mLogView);


            if(mPreferences.getBool(TrackerPreferences.Settings.ENABLE_LOG_VIEW)) {
                TrackerLog.insertNodeBefore(mLogView, LogFile.class);
                TrackerLog.flushToNode(mLogView);
                TrackerLog.printNodeList("NODE_LIST4");
                TrackerLog.i("LOGTEST", "LogView Ready");
                TrackerLog.stopLogRecording();
                TrackerLog.clearLogRecord();
            }
        }
        return mFragmentView;
    }

    @Override
    public void updateToolbar(Toolbar pToolbar){
        pToolbar.setTitle("Log");
        pToolbar.setSubtitle("");
        MenuItem refreshItem = pToolbar.getMenu().findItem(R.id.action_refresh);
        if(refreshItem != null)
            refreshItem.setVisible(false);
    }
}