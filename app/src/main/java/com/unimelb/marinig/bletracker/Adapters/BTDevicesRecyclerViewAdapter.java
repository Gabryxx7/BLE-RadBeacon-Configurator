package com.unimelb.marinig.bletracker.Adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.unimelb.marinig.bletracker.Events.ReadBeaconEvent;
import com.unimelb.marinig.bletracker.Fragments.BeaconConfigDialog;
import com.unimelb.marinig.bletracker.Fragments.ScannedDevicesFragment.OnListFragmentInteractionListener;
import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;
import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord.GattStatus;
import com.unimelb.marinig.bletracker.Model.TrackerPreferences;
import com.unimelb.marinig.bletracker.R;
import com.unimelb.marinig.bletracker.Utils.Utils;
import com.unimelb.marinig.bletracker.Utils.ViewUtils;


import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ScannedDeviceRecord} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class BTDevicesRecyclerViewAdapter extends RecyclerView.Adapter<BTDevicesRecyclerViewAdapter.ViewHolder> {
    private final OnListFragmentInteractionListener mListener;
    private final Context mAppContext;
    private final TrackerPreferences mPreferences;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private List<ScannedDeviceRecord> mDeviceList = new ArrayList<>();
    private List<ScannedDeviceRecord> mDeviceToShowList = new ArrayList<>();
    private Map<String, Boolean> filters = new HashMap<>();
    private Comparator mComparator = ScannedDeviceRecord.Comparators.DEVICE_TYPE;
    private int mSortId;

    public BTDevicesRecyclerViewAdapter(Context appContext, OnListFragmentInteractionListener listener) {
        mListener = listener;
        mAppContext = appContext;
        mPreferences = new TrackerPreferences(mAppContext);
        mSortId = TrackerPreferences.Ordering.ORDER_RSSI;
        filters.put(TrackerPreferences.Settings.FILTER_PROJECT_BEACONS, mPreferences.getBool(TrackerPreferences.Settings.FILTER_PROJECT_BEACONS));
        filters.put(TrackerPreferences.Settings.FILTER_EDDYSTONE, mPreferences.getBool(TrackerPreferences.Settings.FILTER_EDDYSTONE));
        filters.put(TrackerPreferences.Settings.FILTER_IBEACONS, mPreferences.getBool(TrackerPreferences.Settings.FILTER_IBEACONS));
        filters.put(TrackerPreferences.Settings.FILTER_CONFIGURABLE_RADBEACONS, mPreferences.getBool(TrackerPreferences.Settings.FILTER_CONFIGURABLE_RADBEACONS));
    }

    public void clearList(){
        mDeviceList.clear();
        mDeviceToShowList.clear();
    }

    public void setDevicesList(List<ScannedDeviceRecord> deviceList) {
        mDeviceList = deviceList;
        updateViewList();
    }

    public void updateFilter(String filter, boolean filterState){
        filters.put(filter, filterState);

        if(filter.equals(TrackerPreferences.Settings.FILTER_EDDYSTONE)){
            mPreferences.set(TrackerPreferences.Settings.FILTER_EDDYSTONE, filterState);
        }

        if(filter.equals(TrackerPreferences.Settings.FILTER_IBEACONS)){
            mPreferences.set(TrackerPreferences.Settings.FILTER_IBEACONS, filterState);
        }

        if(filter.equals(TrackerPreferences.Settings.FILTER_PROJECT_BEACONS)){
            mPreferences.set(TrackerPreferences.Settings.FILTER_PROJECT_BEACONS, filterState);
        }

        if(filter.equals(TrackerPreferences.Settings.FILTER_CONFIGURABLE_RADBEACONS)){
            mPreferences.set(TrackerPreferences.Settings.FILTER_CONFIGURABLE_RADBEACONS, filterState);
        }
        this.updateViewList();
    }

    public int updateDevice(ScannedDeviceRecord device, boolean updateView){
        for(int i =0; i < mDeviceList.size(); i++){
            if(mDeviceList.get(i).getMac_beacon().equals(device.getMac_beacon())){
                mDeviceList.set(i, device);
                if(updateView){
                    notifyItemChanged(i);
                    notifyDataSetChanged();
                }
                return i;
            }
        }
        mDeviceList.add(device);
        return mDeviceList.size()-1;
    }

    public void updateSorting(int sortId){
        if(mSortId == sortId)
            return;

        mSortId = sortId;
        switch(mSortId){
            case TrackerPreferences.Ordering.ORDER_MAC: mComparator = ScannedDeviceRecord.Comparators.MAC; break;
            case TrackerPreferences.Ordering.ORDER_TYPE: mComparator = ScannedDeviceRecord.Comparators.DEVICE_TYPE; break;
            case TrackerPreferences.Ordering.ORDER_MAJOR: mComparator = ScannedDeviceRecord.Comparators.MAJOR; break;
            case TrackerPreferences.Ordering.ORDER_MINOR: mComparator = ScannedDeviceRecord.Comparators.MINOR; break;
            case TrackerPreferences.Ordering.ORDER_NAME: mComparator = ScannedDeviceRecord.Comparators.NAME_ASC; break;
            case TrackerPreferences.Ordering.ORDER_RSSI: mComparator = ScannedDeviceRecord.Comparators.RSSI; break;
            case TrackerPreferences.Ordering.ORDER_TIMESCAN: mComparator = ScannedDeviceRecord.Comparators.TIME_SCAN; break;
        }
        this.updateViewList();
    }


    public void updateViewList() {
        //For every device
        String UUIDList = mPreferences.getString(TrackerPreferences.Settings.SCAN_UUIDS);
        String namespaceList = mPreferences.getString(TrackerPreferences.Settings.SCAN_EDDYSTONE_NAMESPACES);
        if(mDeviceList.size() > 0) {
            Stream<ScannedDeviceRecord> str = mDeviceList.stream();

            if (filters.get(TrackerPreferences.Settings.FILTER_CONFIGURABLE_RADBEACONS)) {
                str = str.filter(t -> t.isRadBeaconConfigurable);
            }
            if (filters.get(TrackerPreferences.Settings.FILTER_IBEACONS)) {
                str = str.filter(t -> t.isIbeacon());
            }
            if (filters.get(TrackerPreferences.Settings.FILTER_EDDYSTONE)) {
                str = str.filter(t -> t.isEddystone_UID());
            }
            if (filters.get(TrackerPreferences.Settings.FILTER_PROJECT_BEACONS)) {
                str = str.filter(t -> Utils.isUUIDInList(t.getUUID(), UUIDList));
            }
            str = str.sorted(mComparator);

            mDeviceToShowList = (ArrayList<ScannedDeviceRecord>) str.collect(Collectors.toList());
            notifyDataSetChanged();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Keep the parent to use match_parent and keep the width fixes
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.bt_device_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ScannedDeviceRecord device = getItem(position);
        int textcolor = -1;
        int backgroundColor = -1;
        holder.mItem = device;
        holder.mView.setElevation(7);

        holder.mRecord_name.setText(device.getRadName() == null ? device.getName() : device.getRadName());
        holder.mDevice_MAC.setText("MAC · " + device.getMac_beacon());
        holder.mRecord_RSSI.setText("RSSI " + device.getRSSI());
        holder.mProgress_RSSI.setProgress(100 + device.getRSSI()); //RSSI is negative from -100 (weak) to 0 (strong) so 100 + (RSSI) would result in 0 (weak) or 100 (strong)
        holder.mBattery_level.setText("Battery " + device.getBatteryLevel() + "%");
        holder.mBattery_level.setVisibility(device.getBatteryLevel() < 0 ? View.GONE : View.VISIBLE);
        holder.mConfigurable.setVisibility(device.isRadBeaconConfigurable ? View.VISIBLE : View.GONE);
        holder.mDevice_uuid.setText("UUID · " + device.getUUID());
        holder.mRecord_distance.setText("Distance · " + String.format(("%.6f"), device.getDistance()));

        if(TimeUnit.MILLISECONDS.toSeconds(device.getAvailableTime()) > 0) {
            float frequency = ((float) device.getScanCountPerScanPeriod()) / ((float) TimeUnit.MILLISECONDS.toSeconds(device.getAvailableTime()));
            holder.mDevice_scanCount.setText("" + device.getScanCountPerScanPeriod() + " ~ " + String.format(java.util.Locale.US,"%.2f", frequency) + " Hz");
        } else {
            holder.mDevice_scanCount.setText("" + device.getScanCountPerScanPeriod());
        }

        long hours = TimeUnit.MILLISECONDS.toHours(device.getAvailableTime());
        long minutes = TimeUnit.MILLISECONDS.toMinutes(device.getAvailableTime()) % TimeUnit.HOURS.toMinutes(1);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(device.getAvailableTime()) % TimeUnit.MINUTES.toSeconds(1);
        String availTime = String.format("%02ds", seconds);
        if(hours > 0){
            availTime =  String.format("%02dh %02dm %02ds", hours, minutes, seconds);
        }
        else if(minutes > 0){
            availTime =  String.format("%02dm %02ds", minutes, seconds);
        }
        holder.mDevice_availableTime.setText(availTime);

        if(device.isAvailable()){
            holder.mRecord_ts.setText(dateFormat.format(new Date(device.getTimestamp_scan())));
            //holder.mRecord_ts.setText("Available");
            //holder.mRecord_ts.setTextColor(Color.GREEN);
        }
        else {
            holder.mRecord_ts.setText(dateFormat.format(new Date(device.getTimestamp_scan())));
            //holder.mRecord_ts.setText("Last seen:\n" + dateFormat.format(new Date(device.getTimestamp_scan())));
            //holder.mRecord_ts.setTextColor(Color.RED);
        }

        if(device.getDeviceType() == ScannedDeviceRecord.TYPE_IBEACON){
            Log.e("IBEACON", "SHOWING IBEACON " + device.getMinor());
            if(device.isAvailable()){
                backgroundColor = mAppContext.getColor(R.color.colorIBeaconAvailable);
                textcolor = ContextCompat.getColor(mAppContext, R.color.colorIBeaconAvailableText);
            }
            else {
                backgroundColor = mAppContext.getColor(R.color.colorIBeaconUnavailable);
                textcolor = ContextCompat.getColor(mAppContext, R.color.colorIBeaconUnavailableText);
            }
            holder.mDevice_id1_title.setText("Major · " +device.getMajor());
            holder.mDevice_id2_title.setText("minor · " +device.getMinor());

            holder.mDevice_id1_title.setVisibility(View.VISIBLE);
            holder.mDevice_id2_title.setVisibility(View.VISIBLE);
            holder.mDevice_uuid.setVisibility(View.VISIBLE);
            holder.mRecord_distance.setVisibility(View.VISIBLE);
            holder.mDevice_id1.setVisibility(View.GONE);
            holder.mDevice_id2.setVisibility(View.GONE);
        }
        else if (device.getDeviceType() == ScannedDeviceRecord.TYPE_EDDYSTONE_UID) {

            if(device.isAvailable()){
                backgroundColor = mAppContext.getColor(R.color.colorEddystoneUIDAvailable);
                textcolor = ContextCompat.getColor(mAppContext, R.color.colorEddystoneUIDAvailableText);
            }
            else {
                backgroundColor = mAppContext.getColor(R.color.colorEddystoneUIDUnavailable);
                textcolor = ContextCompat.getColor(mAppContext, R.color.colorEddystoneUIDUnavailableText);
            }
            holder.mDevice_id1_title.setText("NamespaceID");
            holder.mDevice_id2_title.setText("InstanceID");
            holder.mDevice_id1.setText(device.getNamespace_id());
            holder.mDevice_id2.setText(device.getInstance_id());

            holder.mDevice_id1_title.setVisibility(View.VISIBLE);
            holder.mDevice_id2_title.setVisibility(View.VISIBLE);
            holder.mDevice_id1.setVisibility(View.VISIBLE);
            holder.mDevice_id2.setVisibility(View.VISIBLE);
            holder.mDevice_uuid.setVisibility(View.GONE);
            holder.mRecord_distance.setVisibility(View.VISIBLE);
        }
        else{
            if(device.isAvailable()){
                backgroundColor = mAppContext.getColor(R.color.colorDeviceAvailable);
                textcolor = ContextCompat.getColor(mAppContext, R.color.colorDeviceAvailableText);
            }
            else {
                backgroundColor = mAppContext.getColor(R.color.colorDeviceUnavailable);
                textcolor = ContextCompat.getColor(mAppContext, R.color.colorDeviceUnavailableText);
            }
            holder.mDevice_id1_title.setVisibility(View.GONE);
            holder.mDevice_id2_title.setVisibility(View.GONE);
            holder.mDevice_id1.setVisibility(View.GONE);
            holder.mDevice_id2.setVisibility(View.GONE);
            holder.mDevice_uuid.setVisibility(View.GONE);
            holder.mRecord_distance.setVisibility(View.GONE);
        }

        holder.mAdvRate.setVisibility(device.hasGattStatus(GattStatus.READ) ? View.VISIBLE : View.GONE);
        holder.mAdvRate.setText("Adv Rate: " + device.getAdvertisingRate() + " Hz");
        holder.mTxPower.setVisibility(device.hasGattStatus(GattStatus.READ) ? View.VISIBLE : View.GONE);
        holder.mTxPower.setText("Tx Power: " + ScannedDeviceRecord.DOT_TRANSMIT_POWER_VALUES[device.getRadBeaconTransmitPowerIndex()] + " dBm");

        holder.mReadButton.setEnabled(device.isRadBeaconConfigurable);
        if(device.hasGattStatus(GattStatus.READING)){
            holder.mReadButton.setEnabled(false);
//            holder.mReadButton.setCompoundDrawablesWithIntrinsicBounds(null, null, holder.mWriteButtonDrawable, null);
            if(!holder.mReadButtonAnimator.isRunning())
                holder.mReadButtonAnimator.start();
        }
        else{
//            holder.mReadButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            holder.mReadButton.setEnabled(true);
            holder.mReadButtonAnimator.cancel();
        }

//        holder.mWriteButton.setEnabled(device.isRadBeaconConfigurable);
        holder.mWriteButton.setEnabled(device.hasGattStatus(GattStatus.READ));
        if(device.hasGattStatus(GattStatus.UPDATING)){
            holder.mWriteButton.setEnabled(false);
//            holder.mWriteButton.setCompoundDrawablesWithIntrinsicBounds(null, null, holder.mWriteButtonDrawable, null);
            if(!holder.mWriteButtonAnimator.isRunning())
                holder.mWriteButtonAnimator.start();
        }
        else{
//            holder.mWriteButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            holder.mWriteButton.setEnabled(true);
            holder.mWriteButtonAnimator.cancel();
        }
        holder.mConfigContainer.setVisibility(device.isRadBeaconConfigurable ? View.VISIBLE : View.GONE);


        holder.mView.findViewById(R.id.device_container_layout).getBackground().setTint(backgroundColor);
        ViewUtils.changeAllViewsTextColor(holder.mContainer, textcolor, new String[]{"keepColor"});

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });


        holder.mWriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //device.setMajor(135);
                //EventBus.getDefault().post(new UpdateBeaconEvent(device, "00000000"));
                BeaconConfigDialog dialog = new BeaconConfigDialog(mAppContext, device);
                dialog.show();
            }
        });

        holder.mReadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new ReadBeaconEvent(device));
            }
        });
    }



    public ScannedDeviceRecord getItem(int position){
        return mDeviceToShowList.get(position);
    }

    @Override
    public int getItemCount() {
        return mDeviceToShowList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final TextView mRecord_name;
        private final TextView mRecord_RSSI;
        private final ProgressBar mProgress_RSSI;
        private final TextView mBattery_level;
        private final ImageView mConfigurable;
        private final TextView mRecord_ts;
        private final ImageView mRecord_ts_image;
        private final TextView mRecord_distance;
        private final TextView mDevice_id1;
        private final TextView mDevice_id2;
        private final TextView mDevice_id1_title;
        private final TextView mDevice_id2_title;
        private final TextView mDevice_MAC;
        private final TextView mDevice_uuid;
        private final TextView mDevice_scanCount;
        private final ImageView mDevice_scanCount_image;
        private final TextView mDevice_availableTime;
        private final ImageView mDevice_availableTime_image;
        private final TextView mAdvRate;
        private final TextView mTxPower;
        private final Button mReadButton;
        private ObjectAnimator mReadButtonAnimator;
        private Drawable mReadButtonDrawable;
        private final Button mWriteButton;
        private ObjectAnimator mWriteButtonAnimator;
        private Drawable mWriteButtonDrawable;
        private final ViewGroup mContainer;
        private final ViewGroup mConfigContainer;
        public ScannedDeviceRecord mItem;

        private ViewHolder(View view) {
            super(view);
            mView = view;
            mContainer = (ViewGroup) view.findViewById(R.id.device_container_layout);
            mConfigContainer = (ViewGroup) view.findViewById(R.id.config_container);
            mRecord_name = (TextView) view.findViewById(R.id.device_name);
            mRecord_RSSI = (TextView) view.findViewById(R.id.record_rssi);
            mProgress_RSSI = (ProgressBar) view.findViewById(R.id.rssi_progress);
            mBattery_level = (TextView) view.findViewById(R.id.batteryLevel);
            mConfigurable = (ImageView) view.findViewById(R.id.configurable);
            mRecord_ts = (TextView) view.findViewById(R.id.redcord_ts);
            mRecord_ts_image = (ImageView) view.findViewById(R.id.time_image);
            mRecord_distance = (TextView) view.findViewById(R.id.record_distance);
            mDevice_uuid = (TextView) view.findViewById(R.id.device_uuid);
            mDevice_id1 = (TextView) view.findViewById(R.id.device_id1);
            mDevice_id2 = (TextView) view.findViewById(R.id.device_id2);
            mDevice_id1_title = (TextView) view.findViewById(R.id.device_id1_title);
            mDevice_id2_title = (TextView) view.findViewById(R.id.device_id2_title);
            mDevice_MAC = (TextView) view.findViewById(R.id.device_mac);
            mDevice_scanCount = (TextView) view.findViewById(R.id.device_scanCount);
            mDevice_scanCount_image = (ImageView) view.findViewById(R.id.count_image);
            mDevice_availableTime = (TextView) view.findViewById(R.id.device_availableTime);
            mDevice_availableTime_image = (ImageView) view.findViewById(R.id.avail_time_image);
            mAdvRate = (TextView) view.findViewById(R.id.record_adv_rate);
            mTxPower = (TextView) view.findViewById(R.id.record_tx_power);


            mReadButton = (Button) view.findViewById(R.id.readButton);
            mReadButtonDrawable = (Drawable) mAppContext.getResources().getDrawable(R.drawable.rotating_progress_bar);
            mReadButton.setCompoundDrawablesWithIntrinsicBounds(null, null, mReadButtonDrawable, null);
            mReadButtonAnimator = ObjectAnimator.ofInt(mReadButtonDrawable, "level", 0, 10000).setDuration(1000);
            mReadButtonAnimator.setRepeatCount(Animation.INFINITE);
            mReadButtonAnimator.setInterpolator(new LinearInterpolator());

            mWriteButton = (Button) view.findViewById(R.id.writeButton);
            mWriteButtonDrawable = (Drawable) mAppContext.getResources().getDrawable(R.drawable.rotating_progress_bar);
            mWriteButton.setCompoundDrawablesWithIntrinsicBounds(null, null, mWriteButtonDrawable, null);
            mWriteButtonAnimator = ObjectAnimator.ofInt(mWriteButtonDrawable, "level", 0, 10000).setDuration(1000);
            mWriteButtonAnimator.setRepeatCount(Animation.INFINITE);
            mWriteButtonAnimator.setInterpolator(new LinearInterpolator());
        }

//        @Override
//        public String toString() {
//            return super.toString() + " '" + mDeviceMAC.getText() + "'";
//        }
    }
}
