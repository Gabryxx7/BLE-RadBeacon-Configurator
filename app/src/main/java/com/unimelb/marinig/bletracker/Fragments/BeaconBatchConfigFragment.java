package com.unimelb.marinig.bletracker.Fragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.unimelb.marinig.bletracker.Activities.MainActivity;
import com.unimelb.marinig.bletracker.Adapters.BTDevicesRecyclerViewAdapter;
import com.unimelb.marinig.bletracker.Adapters.Data.ServiceContent;
import com.unimelb.marinig.bletracker.Events.BatchConfigurationUpdate;
import com.unimelb.marinig.bletracker.Events.UpdatedScannedDeviceEvent;
import com.unimelb.marinig.bletracker.Interfaces.ToolbarFragment;
import com.unimelb.marinig.bletracker.Model.BeaconConfig;
import com.unimelb.marinig.bletracker.Model.BeaconConfigurator;
import com.unimelb.marinig.bletracker.Model.BeaconConfigurator.*;
import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;
import com.unimelb.marinig.bletracker.Model.TrackerPreferences;
import com.unimelb.marinig.bletracker.R;
import com.unimelb.marinig.bletracker.Utils.FileUtils;
import com.unimelb.marinig.bletracker.Utils.ViewUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BeaconBatchConfigFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BeaconBatchConfigFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BeaconBatchConfigFragment  extends ToolbarFragment {
    private static final String TAG = "BatchConfigFragment";
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final int READ_REQUEST_CODE = 42;

    private int mColumnCount = 1;
    private SwipeRefreshLayout mListContainerSwipe;
    private OnFragmentInteractionListener mListener;
    private View mView = null;
    private RecyclerView mRecyclerView = null;
    private LinearLayoutManager mLinearLayoutManager = null;
    private BTDevicesRecyclerViewAdapter btDeviceListAdapter;
    private Toolbar mToolbar;
    private ServiceContent mBLEScanner;
    private EditText mMinorEditText;
    private EditText mExtraEditText;
    private Button mStartPauseButton;
    private Button mCancelButton;
    private TextView mConfigStatus;
    private TextView mFilename;
    private TextView mBrowseButton;
    private TextView mNoBeaconsConfigText;
    private ImageView mConfigProgress;
    private Spinner mUpdateMinorsSpinner;
    private Spinner mAdvRateSpinner;
    private Spinner mAdvRateSpinnerValues;
    private Spinner mExtraSpinner;
    private TextView mHertzText;
    private Spinner mTxPowerSpinner;
    private Spinner mTxPowerSpinnerValues;
    private TextView mDBmText;
    private ObjectAnimator mConfigProgressAnim;
    private static volatile boolean mIsScanning = false;
    private static volatile int mItemCount = 0;
    private static final int VERTICAL_ITEM_SPACE = 35;
    private TrackerPreferences mPreferences;
    private Uri mFileUri = null;
    DateFormat mDateFormat = new SimpleDateFormat("HH:mm:ss");

    private TextView mPrevText;
    private TextView mCurrentText;

    private BeaconConfigurator mConfigurator = null;

    private BeaconConfig mCurrentConfig = null;
    private ScannedDeviceRecord mCurrentBeacon = null;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UpdatedScannedDeviceEvent event) {
        //TrackerLog.e(TAG, "UPDATED DEVICE FRAGMENT");
        if(event.device.isBatchUpdated) {
            Log.e(TAG, "Updated device, adding to list: "+BeaconConfigurator.beaconToString(event.device));
            btDeviceListAdapter.updateDevice(event.device, event.updateView);
            btDeviceListAdapter.notifyDataSetChanged();
            mNoBeaconsConfigText.setVisibility(btDeviceListAdapter.getItemCount() > 0 ? View.INVISIBLE : View.VISIBLE);
            refreshList();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BatchConfigurationUpdate event) {
        Log.e(TAG, "ConfigUpdate"+(event.fromScanner ? "(from scanner)":"")+ ": " +event.configurator.getStates().toString());
        if(!event.fromScanner){
            return;
        }
        //TrackerLog.e(TAG, "UPDATED DEVICE FRAGMENT")
        if(event.configurator.hasState(BatchConfigState.STARTED)){
            this.mConfigStatus.setText("Running");
        }
        if(event.configurator.hasState(BatchConfigState.PAUSED)){
            this.mConfigStatus.setText("Paused");
            ViewUtils.setAllViewsEnabled(true, mView.findViewById(R.id.config_data));
        }
        if(event.configurator.hasState(BatchConfigState.WAITING)){
            this.mConfigStatus.setText("Waiting for user");
            mStartPauseButton.setText("Continue");
            mConfigProgressAnim.cancel();

            if(event.configurator.getTxSettings().equals(TxPowerSettings.ASK)){
                mTxPowerSpinnerValues.setEnabled(true);
            }
            if(event.configurator.getAdvSettings().equals(AdvRateSetting.ASK)){
                mAdvRateSpinnerValues.setEnabled(true);
            }
            if(event.configurator.getMinorSettings().equals(MinorSetting.ASK)){
                mMinorEditText.setEnabled(true);
                if(event.beacon != null){
                    mMinorEditText.setText(event.beacon.getMinor()+"");
                }
                else{
                    mMinorEditText.setText("");
                }
            }
            if(event.configurator.getExtraSettings().equals(ExtraSetting.ASK)){
                mExtraEditText.setEnabled(true);
            }

            if(event.beacon != null && mCurrentText != null){
                mCurrentText.setText(getLogText(event.beacon, null));
            }
        }

        if(event.configurator.hasState(BatchConfigState.UPDATING)){
            this.mConfigStatus.setText("Updating");
            if(event.beacon != null) {
                if(mCurrentText != null && mPrevText != null){
                    mPrevText.setText(getLogText(mCurrentBeacon, mCurrentConfig));
                }

                mCurrentBeacon = event.beacon;
                mCurrentConfig = event.configurator.getLast();
                this.mCurrentText.setText(getLogText(mCurrentBeacon, mCurrentConfig));
            }
        }

        if(event.configurator.getLast() != null){
            BeaconConfig last = event.configurator.getLast();
            mAdvRateSpinner.setSelection(event.configurator.getAdvSettings().ordinal());
            if(last.getAdvRateIndex() != null && mAdvRateSpinnerValues!= null)
                mAdvRateSpinnerValues.setSelection(last.getAdvRateIndex());

            mTxPowerSpinner.setSelection(event.configurator.getTxSettings().ordinal());
            if(last.getTxPowerIndex() != null && mTxPowerSpinnerValues != null)
                mTxPowerSpinnerValues.setSelection(last.getTxPowerIndex());

            mUpdateMinorsSpinner.setSelection(event.configurator.getMinorSettings().ordinal());
            if(last.getMinor() != null && mMinorEditText != null)
                mMinorEditText.setText(last.getMinor()+"");

            mExtraSpinner.setSelection(event.configurator.getExtraSettings().ordinal());
            if(last.getExtra() != null && mExtraEditText != null)
                mExtraEditText.setText(last.getExtra()+"");
        }
    }

    public String getLogText(ScannedDeviceRecord beacon, BeaconConfig config){
        String time = mDateFormat.format(new Date(Calendar.getInstance().getTimeInMillis()));
        String str = time+"";
        if(beacon != null) {
            str += " Beacon: " + BeaconConfigurator.beaconToString(beacon);
        }
        if(config != null) {
            str += "\nConfig: " + config.toString();
        }

        return str;
    }



    public BeaconBatchConfigFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        clearList();
        refreshList();
    }


    @Override
    public void onStop() {
        super.onStop();
        if(EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public static BeaconBatchConfigFragment newInstance(String param1, String param2) {
        BeaconBatchConfigFragment fragment = new BeaconBatchConfigFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(mView == null) {

            mView = inflater.inflate(R.layout.fragment_beacon_config, container, false);
            mListContainerSwipe = mView.findViewById(R.id.devices_list_container);

            mRecyclerView = mView.findViewById(R.id.device_list);
            mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));
            mRecyclerView.setAdapter(btDeviceListAdapter);
            mRecyclerView.setHasFixedSize(true);

            mMinorEditText = mView.findViewById(R.id.minor_text);

            mUpdateMinorsSpinner = mView.findViewById(R.id.update_minors_spinner);
            ArrayAdapter<MinorSetting> updateMinorsSpinnerAdapter = new ArrayAdapter<MinorSetting>(getContext(), android.R.layout.simple_spinner_item, MinorSetting.values());
            updateMinorsSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mUpdateMinorsSpinner.setAdapter(updateMinorsSpinnerAdapter);
            mUpdateMinorsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(MinorSetting.values()[position] == MinorSetting.CONSTANT || MinorSetting.values()[position] == MinorSetting.SEQUENTIAL || MinorSetting.values()[position] == MinorSetting.ASK){
                        mMinorEditText.setVisibility(View.VISIBLE);
                    }
                    else{
                        mMinorEditText.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


            mAdvRateSpinner = mView.findViewById(R.id.adv_rate_spinner);
            ArrayAdapter<AdvRateSetting> advRateSpinnerAdapter = new ArrayAdapter<AdvRateSetting>(getContext(), android.R.layout.simple_spinner_item, AdvRateSetting.values());
            advRateSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mAdvRateSpinner.setAdapter(advRateSpinnerAdapter);
            mHertzText = mView.findViewById(R.id.hertz_text);
            mAdvRateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(AdvRateSetting.values()[position] == AdvRateSetting.CONSTANT || AdvRateSetting.values()[position] == AdvRateSetting.SEQUENTIAL || AdvRateSetting.values()[position] == AdvRateSetting.ASK ){
                        mAdvRateSpinnerValues.setVisibility(View.VISIBLE);
                        mHertzText.setVisibility(View.VISIBLE);
                    }
                    else{
                        mAdvRateSpinnerValues.setVisibility(View.GONE);
                        mHertzText.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            mAdvRateSpinnerValues = mView.findViewById(R.id.adv_rate_values_spinner);
            ArrayAdapter<Integer> advRateValuesSpinnerAdapter = new ArrayAdapter<Integer>(getContext(), android.R.layout.simple_spinner_item, ScannedDeviceRecord.ADV_RATE_VALUES);
            advRateValuesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mAdvRateSpinnerValues.setAdapter(advRateValuesSpinnerAdapter);
            mAdvRateSpinnerValues.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


            mTxPowerSpinner = mView.findViewById(R.id.tx_power_spinner);
            ArrayAdapter<TxPowerSettings> txPowerSpinnerAdapter = new ArrayAdapter<TxPowerSettings>(getContext(), android.R.layout.simple_spinner_item, TxPowerSettings.values());
            txPowerSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mTxPowerSpinner.setAdapter(txPowerSpinnerAdapter);
            mDBmText = mView.findViewById(R.id.dbm_text);
            mTxPowerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(TxPowerSettings.values()[position] == TxPowerSettings.CONSTANT || TxPowerSettings.values()[position] == TxPowerSettings.SEQUENTIAL || TxPowerSettings.values()[position] == TxPowerSettings.ASK){
                        mTxPowerSpinnerValues.setVisibility(View.VISIBLE);
                        mDBmText.setVisibility(View.VISIBLE);
                    }
                    else{
                        mTxPowerSpinnerValues.setVisibility(View.GONE);
                        mDBmText.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            mTxPowerSpinnerValues = mView.findViewById(R.id.tx_power_values_spinner);
            ArrayAdapter<Integer> txPowerValuesSpinnerAdapter = new ArrayAdapter<Integer>(getContext(), android.R.layout.simple_spinner_item, ScannedDeviceRecord.DOT_TRANSMIT_POWER_VALUES);
            txPowerValuesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mTxPowerSpinnerValues.setAdapter(txPowerValuesSpinnerAdapter);
            mTxPowerSpinnerValues.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            mExtraEditText = mView.findViewById(R.id.extra_text);
            mExtraSpinner = mView.findViewById(R.id.extra_spinner);
            ArrayAdapter<ExtraSetting> extraSpinnerAdapter = new ArrayAdapter<ExtraSetting>(getContext(), android.R.layout.simple_spinner_item, ExtraSetting.values());
            extraSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mExtraSpinner.setAdapter(extraSpinnerAdapter);
            mExtraSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(ExtraSetting.values()[position] == ExtraSetting.CONSTANT || ExtraSetting.values()[position] == ExtraSetting.ASK){
                        mExtraEditText.setVisibility(View.VISIBLE);
                    }
                    else{
                        mExtraEditText.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


            mLinearLayoutManager = new LinearLayoutManager(getActivity());
//            mLinearLayoutManager.setAutoMeasureEnabled(false);
            mRecyclerView.setLayoutManager(mLinearLayoutManager);
//            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mRecyclerView.getLayoutParams();
//            marginLayoutParams.setMargins(0, 0, 0, 0); //This is for the margins from the bottom
//            mRecyclerView.setLayoutParams(marginLayoutParams);

            mFilename = mView.findViewById(R.id.file_selected);
            mBrowseButton = mView.findViewById(R.id.browse_button);
            mBrowseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performFileSearch();
                }
            });
            mPreferences = new TrackerPreferences(getContext());

            mConfigStatus = mView.findViewById(R.id.batch_config_status);
            mConfigProgress = mView.findViewById(R.id.batch_config_progress);
            mConfigProgress.setVisibility(View.GONE);
            mConfigProgressAnim = ObjectAnimator.ofFloat(mConfigProgress, View.ROTATION, 0f, 360f);
            mConfigProgressAnim.setDuration(300);
            mConfigProgressAnim.setRepeatCount(ValueAnimator.INFINITE);
            mNoBeaconsConfigText = mView.findViewById(R.id.no_config_beacons);
            mNoBeaconsConfigText.setVisibility(btDeviceListAdapter.getItemCount() > 0 ? View.INVISIBLE : View.VISIBLE);

            mStartPauseButton = mView.findViewById(R.id.pause_config);
            mStartPauseButton.setText("Start");
            mStartPauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TxPowerSettings tx = (TxPowerSettings) mTxPowerSpinner.getSelectedItem();
                    Integer txValueIndex = mTxPowerSpinnerValues.getSelectedItemPosition();
                    AdvRateSetting adv = (AdvRateSetting) mAdvRateSpinner.getSelectedItem();
                    Integer advValueIndex =  mAdvRateSpinnerValues.getSelectedItemPosition();
                    MinorSetting minorOpt = (MinorSetting) mUpdateMinorsSpinner.getSelectedItem();
                    Integer minorValue = 0;
                    ExtraSetting extraOpt = (ExtraSetting) mExtraSpinner.getSelectedItem();
                    String extra = mExtraEditText.getText().toString();
                    try {
                        minorValue = Integer.valueOf(mMinorEditText.getText().toString());
                    }
                    catch(Exception e){
                        Log.e(TAG, "Error getting minor text: "+e.getMessage());
                    }

                    if(mConfigurator != null && mConfigurator.hasState(BatchConfigState.STARTED)) {
                        if (mConfigurator.hasState(BatchConfigState.PAUSED) || mConfigurator.hasState(BatchConfigState.WAITING)) {
                            if(mConfigurator.hasState(BatchConfigState.WAITING)){
                                mConfigStatus.setText("Updating config data...");
                                mConfigurator.setAskedValues(txValueIndex, advValueIndex, minorValue, extra);
                            }
                            else if(mConfigurator.removeState(BatchConfigState.PAUSED)){
                                mConfigurator.setTxSettings(tx);
                                mConfigurator.getInitialConfig().setTxPowerIndex(txValueIndex);
                                mConfigurator.setAdvSettings(adv);
                                mConfigurator.getInitialConfig().setAdvRateIndex(advValueIndex);
                                mConfigurator.setMinorSettings(minorOpt);
                                mConfigurator.getInitialConfig().setMinor(minorValue);
                                mConfigurator.setExtraSettings(extraOpt);
                                mConfigurator.getInitialConfig().setExtra(extra);
                            }
                            mConfigurator.removeState(BatchConfigState.PAUSED);
                            mConfigurator.removeState(BatchConfigState.WAITING);

                            EventBus.getDefault().post(new BatchConfigurationUpdate(false, mConfigurator));
                            mCancelButton.setVisibility(View.VISIBLE);
                            mStartPauseButton.setText("Pause");
                            mConfigProgress.setVisibility(View.VISIBLE);
                            mConfigProgressAnim.start();
                            ViewUtils.setAllViewsEnabled(false, mView.findViewById(R.id.config_data));
                        } else {
                            mConfigurator.addState(BatchConfigState.PAUSED);
                            EventBus.getDefault().post(new BatchConfigurationUpdate(false, mConfigurator));
                            mStartPauseButton.setText("Resume");
                            mConfigProgressAnim.cancel();
                        }
                    }
                    else{
//                        updateConfigList();
//                        if(mConfigList.size() > 0) {
                        mCancelButton.setVisibility(View.VISIBLE);
                        mStartPauseButton.setText("Pause");
                        mConfigProgress.setVisibility(View.VISIBLE);
                        mConfigProgressAnim.start();
                        ViewUtils.setAllViewsEnabled(false, mView.findViewById(R.id.config_data));
                        if (mFileUri != null) {
                            mConfigurator = new BeaconConfigurator(mFileUri, getContext(), tx, txValueIndex , adv, advValueIndex , minorOpt, minorValue, extraOpt, extra);
                        }
                        else{
                            mConfigurator = new BeaconConfigurator(tx, adv, minorOpt, extraOpt);
                            mConfigurator.getInitialConfig().setTxPowerIndex(txValueIndex);
                            mConfigurator.getInitialConfig().setAdvRateIndex(advValueIndex);
                            mConfigurator.getInitialConfig().setMinor(minorValue);
                            mConfigurator.getInitialConfig().setExtra(extra);
                        }

                        Log.e("BATCHCONFIG", "STARTING BatchConfig: + "+ mConfigurator);
                        mConfigurator.addState(BatchConfigState.STARTED);
                        clearList();
                        EventBus.getDefault().post(new BatchConfigurationUpdate(false, mConfigurator));
                    }
                    refreshList();
                }
            });
            mCancelButton = mView.findViewById(R.id.cancel_config);
            mCancelButton.setVisibility(View.GONE);
            mCancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mConfigurator != null && mConfigurator.hasState(BatchConfigState.STARTED)) {
                        mConfigurator.removeState(BatchConfigState.STARTED);
                        EventBus.getDefault().post(new BatchConfigurationUpdate(false, mConfigurator));
                        mCancelButton.setVisibility(View.GONE);
                        mConfigProgress.setVisibility(View.GONE);
                        mStartPauseButton.setText("Start");
                        mConfigProgressAnim.cancel();
                        ViewUtils.setAllViewsEnabled(true, mView.findViewById(R.id.config_data));
                    }
                }
            });

            mPrevText =  mView.findViewById(R.id.prev_config);
            mCurrentText =  mView.findViewById(R.id.current_config);

        }
        return mView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public void clearList(){
        mListContainerSwipe.setRefreshing(true);
        btDeviceListAdapter.clearList();
        mListContainerSwipe.setRefreshing(false);
    }

    public void refreshList(){
        mListContainerSwipe.setRefreshing(true);
        btDeviceListAdapter.updateViewList();
        mListContainerSwipe.setRefreshing(false);
    }

    public void initializeAdapter(MainActivity mainActivity){
        btDeviceListAdapter = new BTDevicesRecyclerViewAdapter(mainActivity, mainActivity);
    }


    public void setBLEServiceContent(ServiceContent bleScanner){
        mBLEScanner = bleScanner;
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        //Little trick to show internal storage: https://issuetracker.google.com/issues/72053350
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
        intent.putExtra("android.content.extra.FANCY", true);
        intent.putExtra("android.content.extra.SHOW_FILESIZE", true);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("*/*");

        if (Build.VERSION.SDK_INT >= 19) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"*/*"});
        }

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                mFileUri = uri;
                // The query, since it only applies to a single document, will only return
                // one row. There's no need to filter, sort, or select fields, since we want
                // all fields for one document.
                Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null, null);
                try {
                    // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
                    // "if there's anything to look at, look at it" conditionals.
                    if (cursor != null && cursor.moveToFirst()) {

                        // Note it's called "Display Name".  This is
                        // provider-specific, and might not necessarily be the file name.
                        String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                        // If the size is unknown, the value stored is null.  But since an
                        // int can't be null in Java, the behavior is implementation-specific,
                        // which is just a fancy term for "unpredictable".  So as
                        // a rule, check if it's null before assigning to an int.  This will
                        // happen often:  The storage API allows for remote files, whose
                        // size might not be locally known.
                        Long size = null;
                        if (!cursor.isNull(sizeIndex)) {
                            // Technically the column stores an int, but cursor.getString()
                            // will do the conversion automatically.
                            size = cursor.getLong(sizeIndex);
                        } else {
                            size = 0L;
                        }

                        mFilename.setText(displayName + " ("+ FileUtils.humanReadableByteCount(size, false)+")");
                        mTxPowerSpinner.setSelection(TxPowerSettings.FROM_FILE.ordinal());
                        mAdvRateSpinner.setSelection(AdvRateSetting.FROM_FILE.ordinal());
                        mUpdateMinorsSpinner.setSelection(MinorSetting.FROM_FILE.ordinal());
                        mExtraSpinner.setSelection(ExtraSetting.FROM_FILE.ordinal());

                    }
                } catch(Exception e){
                    Log.e(TAG, "Error getting selected file: "+e.getMessage());
                }
                finally {
                    cursor.close();
                }
            }
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    public class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {

        private final int verticalSpaceHeight;

        public VerticalSpaceItemDecoration(int verticalSpaceHeight) {
            this.verticalSpaceHeight = verticalSpaceHeight;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.bottom = verticalSpaceHeight;
            if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
                outRect.bottom = (int) Math.round(verticalSpaceHeight*0.5);
            }

            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = (int) Math.round(verticalSpaceHeight*0.5);
            }

        }
    }
}
