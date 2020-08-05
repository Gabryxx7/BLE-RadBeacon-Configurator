package com.unimelb.marinig.bletracker.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.unimelb.marinig.bletracker.Events.SettingsUpdatedEvent;
import com.unimelb.marinig.bletracker.Interfaces.ToolbarFragment;
import com.unimelb.marinig.bletracker.Model.TrackerPreferences;
import com.unimelb.marinig.bletracker.Model.TrackerPreferences.Settings;
import com.unimelb.marinig.bletracker.R;
import com.unimelb.marinig.bletracker.Utils.ViewUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link androidx.fragment.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConfigFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ConfigFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfigFragment extends ToolbarFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private TrackerPreferences mPreferences;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private View mView;
    private ConstraintLayout mScanOptions;
    private ConstraintLayout mRow1;
    private Button mApplyButton;
    private EditText mScanTimeEdit;
    private EditText mScanWaitTimeEdit;
    private Switch mContinuousScanningSwitch;
    private Switch mSettingsSyncSwitch;
    private TextView mVersionText;
    private TextView mUpdatedSettingsDateText;
    private TextView mDeviceInfo;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SettingsUpdatedEvent event){
        this.updateData();
    }


    public ConfigFragment() {
        // Required empty public constructor
    }


    public static ConfigFragment newInstance(String param1, String param2) {
        ConfigFragment fragment = new ConfigFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
        mPreferences = new TrackerPreferences(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();

        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        if(mView == null) {
            mView = inflater.inflate(R.layout.fragment_config, container, false);
            mContinuousScanningSwitch = mView.findViewById(R.id.scanning_switch);
            mSettingsSyncSwitch = mView.findViewById(R.id.sync_settings_switch);
            mRow1 = mView.findViewById(R.id.row1);
            mScanOptions = mView.findViewById(R.id.scanning_options);
            mScanTimeEdit = mScanOptions.findViewById(R.id.SCAN_PERIOD_TIME_text);
            mScanWaitTimeEdit = mScanOptions.findViewById(R.id.wait_time_text);
            mVersionText = mView.findViewById(R.id.version);
            mUpdatedSettingsDateText = mView.findViewById(R.id.updated_date);
            mApplyButton = (Button) mView.findViewById(R.id.apply_button);
            mDeviceInfo = (TextView) mView.findViewById(R.id.device_info);
            mContinuousScanningSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if(isChecked){
                        mRow1.removeView(mScanOptions);
                    }
                    else{
                        mRow1.addView(mScanOptions);
                    }
                }
            });
        }
        updateData();

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
    public void onHiddenChanged(boolean hidden) {
        if(!hidden){
            updateData();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //BLETrackerApplication.getRefWatcher(this.getContext()).watch(this);
    }

    @Override
    public void onStop() {
        if(EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onStop();
    }

    @Override
    public void updateToolbar(Toolbar pToolbar) {
        super.updateToolbar(pToolbar);
        pToolbar.setTitle("Configuration");
        pToolbar.setSubtitle("");
        MenuItem refreshItem = pToolbar.getMenu().findItem(R.id.action_refresh);
        if(refreshItem != null)
            refreshItem.setVisible(false);
    }

    public void updateData(){
        if(mPreferences.getInt(Settings.SCAN_TYPE) == TrackerPreferences.ScanType.SCAN_CONTINUOUS){
            mContinuousScanningSwitch.setChecked(true);
            mRow1.removeView(mScanOptions);
        }

        mScanTimeEdit.setText(Long.toString(mPreferences.getLong(Settings.SCAN_PERIOD_TIME)));
        mScanWaitTimeEdit.setText(Long.toString(mPreferences.getLong(Settings.SCAN_WAIT_TIME)));
        mVersionText.setText(""+mPreferences.getVersion());
        mUpdatedSettingsDateText.setText(""+dateFormat.format(new Date(mPreferences.getLong(Settings.LAST_SETTINGS_UPDATE_TIME))));
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
}
