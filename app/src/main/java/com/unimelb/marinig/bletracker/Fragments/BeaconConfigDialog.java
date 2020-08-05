package com.unimelb.marinig.bletracker.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.unimelb.marinig.bletracker.Events.UpdateBeaconEvent;
import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;
import com.unimelb.marinig.bletracker.Model.TrackerPreferences;
import com.unimelb.marinig.bletracker.R;

import org.greenrobot.eventbus.EventBus;

public class BeaconConfigDialog extends Dialog {
    public ScannedDeviceRecord beacon;
    public Context mContext;
    public Dialog dialog;
    public Button mApply, mCancel;
    public SeekBar mAdvRateBar, mTxPowerBar;
    public EditText mUUID, mMajor, mMinor, mBeaconPin, mName;
    public TextView mAdvRateText, mTxPowerText;
    TrackerPreferences mPreferences;

    public BeaconConfigDialog(Context ctx, ScannedDeviceRecord beacon) {
        super(ctx);
        mPreferences = new TrackerPreferences(ctx);
        // TODO Auto-generated constructor stub
        this.beacon = beacon;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.beacon_config_dialog);


        mApply = (Button) findViewById(R.id.apply_button);
        mCancel = (Button) findViewById(R.id.cancel_button);
        mAdvRateBar = (SeekBar) findViewById(R.id.adv_rate_bar);
        mTxPowerBar = (SeekBar) findViewById(R.id.tx_power_bar);
        mAdvRateText = (TextView) findViewById(R.id.adv_rate_text);
        mTxPowerText = (TextView) findViewById(R.id.tx_power_text);
        mUUID = (EditText) findViewById(R.id.uuid_text);
        mName = (EditText) findViewById(R.id.name_text);
        mMajor = (EditText) findViewById(R.id.major_text);
        mMinor = (EditText) findViewById(R.id.minor_text);
        mBeaconPin = (EditText) findViewById(R.id.pin_text);


        mAdvRateBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAdvRateText.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        mTxPowerBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTxPowerText.setText(String.valueOf(ScannedDeviceRecord.DOT_TRANSMIT_POWER_VALUES[progress]));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mApply.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                beacon.setMajor(Integer.parseInt(mMajor.getText().toString()));
                beacon.setMinor(Integer.parseInt(mMinor.getText().toString()));
                beacon.setAdvertisingRate((byte) mAdvRateBar.getProgress());
                beacon.setTransmitPowerIndex((byte) mTxPowerBar.getProgress());
                beacon.setUUIDString(mUUID.getText().toString());
                beacon.setName(mName.getText().toString());
                EventBus.getDefault().post(new UpdateBeaconEvent(beacon, mBeaconPin.getText().toString()));
                dismiss();
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                dismiss();
            }
        });

        mAdvRateBar.setProgress(mPreferences.getInt(TrackerPreferences.Settings.BEACON_CONFIG_ADV_RATE));
        mTxPowerBar.setProgress(mPreferences.getInt(TrackerPreferences.Settings.BEACON_CONFIG_TX_POWER_INDEX));
        mUUID.setText(mPreferences.getString(TrackerPreferences.Settings.BEACON_CONFIG_UUID));
        mName.setText("RBDot"+beacon.getMinor());
        mMajor.setText(""+mPreferences.getInt(TrackerPreferences.Settings.BEACON_CONFIG_MAJOR));
        mMinor.setText(""+beacon.getMinor());
        mBeaconPin.setText(mPreferences.getString(TrackerPreferences.Settings.BEACON_CONFIG_DEFAULT_PIN));
    }

}