package com.unimelb.marinig.bletracker.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.unimelb.marinig.bletracker.Activities.MainActivity;
import com.unimelb.marinig.bletracker.Adapters.BTDevicesRecyclerViewAdapter;
import com.unimelb.marinig.bletracker.Adapters.Data.ServiceContent;
import com.unimelb.marinig.bletracker.Events.DeviceListFragmentEvent;
import com.unimelb.marinig.bletracker.Events.NewDeviceScannedEvent;
import com.unimelb.marinig.bletracker.Events.ScannerUpdateEvent;
import com.unimelb.marinig.bletracker.Events.ScanStartEvent;
import com.unimelb.marinig.bletracker.Events.ScanStopEvent;
import com.unimelb.marinig.bletracker.Events.UpdatedScannedDeviceEvent;
import com.unimelb.marinig.bletracker.Interfaces.ToolbarFragment;
import com.unimelb.marinig.bletracker.Logger.TrackerLog;
import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;
import com.unimelb.marinig.bletracker.Model.TrackerPreferences;
import com.unimelb.marinig.bletracker.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ScannedDevicesFragment extends ToolbarFragment implements SwipeRefreshLayout.OnRefreshListener {
    // TODO: Customize parameter argument names
    private static final String TAG = "ScannedDevicesFragment";
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private SwipeRefreshLayout mListContainerSwipe;
    private OnListFragmentInteractionListener mListener;
    private View mView = null;
    private FloatingActionButton mFab = null;
    private ProgressBar mScanningBar = null;
    private RecyclerView mRecyclerView = null;
    private LinearLayoutManager mLinearLayoutManager = null;
    private BTDevicesRecyclerViewAdapter btDeviceListAdapter;
    private Toolbar mToolbar;
    private ServiceContent mBLEScanner;
    private static volatile boolean mIsScanning = false;
    private static volatile int mItemCount = 0;
    private static final int VERTICAL_ITEM_SPACE = 35;
    private TrackerPreferences mPreferences;

    private Handler mHandler;
    private long lastViewUpdate = 0;

    private Runnable mUpdateViewListRunnable = new Runnable() {
        @Override
        public void run() {
            if(mIsScanning) {
                if (System.currentTimeMillis() - lastViewUpdate > mPreferences.getLong(TrackerPreferences.Settings.SCAN_PERIOD_TIME)) {
                    refreshList();
                    lastViewUpdate = System.currentTimeMillis();
                }
                long diff = System.currentTimeMillis() - lastViewUpdate;
                double progress = 100.0d * (((double) diff) / ((double) mPreferences.getLong(TrackerPreferences.Settings.SCAN_PERIOD_TIME)));
                mScanningBar.setProgress((int) Math.round(progress), true);
                mHandler.postDelayed(mUpdateViewListRunnable, 100);
            }
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ScannedDevicesFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ScannedDevicesFragment newInstance(int columnCount) {
        ScannedDevicesFragment fragment = new ScannedDevicesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        EventBus.getDefault().post(new DeviceListFragmentEvent(isVisible()));
        refreshList();
        updateScanningState();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //BLETrackerApplication.getRefWatcher(this.getContext()).watch(this);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Initialise stuff and add click listeners
    }

    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {
        refreshList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(mView == null) {

            mView = inflater.inflate(R.layout.fragment_devices_list, container, false);
            mListContainerSwipe = mView.findViewById(R.id.devices_list_container);

            mRecyclerView = mView.findViewById(R.id.device_list);
            mFab = mView.findViewById(R.id.fab);
            mScanningBar = mView.findViewById(R.id.progressBar);
            mScanningBar.setIndeterminate(false);
            mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));
            mRecyclerView.setAdapter(btDeviceListAdapter);
            mRecyclerView.setHasFixedSize(true);

            mLinearLayoutManager = new LinearLayoutManager(getActivity());
//            mLinearLayoutManager.setAutoMeasureEnabled(false);
            mRecyclerView.setLayoutManager(mLinearLayoutManager);
//            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mRecyclerView.getLayoutParams();
//            marginLayoutParams.setMargins(0, 0, 0, 0); //This is for the margins from the bottom
//            mRecyclerView.setLayoutParams(marginLayoutParams);


            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mIsScanning) {
                        mBLEScanner.stopServiceContent();
                        updateScanningState(false);
                    }
                    else{
                        mBLEScanner.startServiceContent();
                        updateScanningState(true);
                    }
                }
            });
            mPreferences = new TrackerPreferences(getContext());
            mHandler = new Handler();
            mHandler.post(mUpdateViewListRunnable);

            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dy > 0 && mFab.getVisibility() == View.VISIBLE) {
                        mFab.hide();
                        if(mIsScanning) {
                            mScanningBar.animate().alpha(0.0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    mScanningBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    } else if (dy < 0 && mFab.getVisibility() != View.VISIBLE) {
                        mFab.show();
                        if(mIsScanning) {
                            mScanningBar.setVisibility(View.VISIBLE);
                            mScanningBar.animate().alpha(1.0f).setDuration(200).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    mScanningBar.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                }
            });

            //Optional:  Decoration and divider
            //DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
            //itemDecorator.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.divider));
            //mRecyclerView.addItemDecoration(itemDecorator);

            // Set the adapter
            /*
            if (mView instanceof RecyclerView) {
                Context context = mView.getContext();
                mRecyclerView = (RecyclerView) mView;
                if (mColumnCount <= 1) {
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                } else {
                    mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
                }
                mRecyclerView.setAdapter(btDeviceListAdapter);
            }*/
        }
        return mView;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ScanStartEvent event){
        TrackerLog.e(TAG, "SCAN STARTED FRAGMENT");
        updateScanningState(true);
        clearList();
        refreshList();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ScanStopEvent event){
        TrackerLog.e(TAG, "SCAN STOPPED FRAGMENT");
        updateScanningState(false);
        refreshList();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NewDeviceScannedEvent event) {
        //TrackerLog.e(TAG, "NEW DEVICE FRAGMENT");
        btDeviceListAdapter.updateDevice(event.device, false);
        mToolbar.setSubtitle(btDeviceListAdapter.getItemCount() +" Devices");
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ScannerUpdateEvent event) {
        //TrackerLog.e(TAG, "NEW SCANNER UPDATE FRAGMENT");
        btDeviceListAdapter.setDevicesList(event.deviceList);
        mToolbar.setSubtitle(btDeviceListAdapter.getItemCount() +" Devices");
        updateScanningState(true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UpdatedScannedDeviceEvent event) {
        TrackerLog.e(TAG, "UPDATED DEVICE FRAGMENT");
        btDeviceListAdapter.updateDevice(event.device, event.updateView);
        mToolbar.setSubtitle(btDeviceListAdapter.getItemCount() +" Devices");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onHiddenChanged(boolean isHidden) {
        super.onHiddenChanged(isHidden);
        EventBus.getDefault().post(new DeviceListFragmentEvent(!isHidden));
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

    public void updateFilter(String filter, boolean filterState){
        btDeviceListAdapter.updateFilter(filter, filterState);
    }

    public void updateSorting(int sortId){
        btDeviceListAdapter.updateSorting(sortId);
    }

    public void initializeAdapter(MainActivity mainActivity){
        btDeviceListAdapter = new BTDevicesRecyclerViewAdapter(mainActivity, mainActivity);
    }

    public void setBLEServiceContent(ServiceContent bleScanner){
        mBLEScanner = bleScanner;
    }

    //This is just a way to update the scanning state without actually changing it
    public void updateScanningState(){
        updateScanningState(mIsScanning);
    }

    public void updateScanningState(boolean isScanning){
        if(mIsScanning && !isScanning){
            mIsScanning = false;
            mHandler.removeCallbacks(mUpdateViewListRunnable);
            lastViewUpdate = 0;
        }
        else if(!mIsScanning && isScanning){
            mIsScanning = true;
            mHandler.post(mUpdateViewListRunnable);
        }
        this.mFab.setImageResource(mIsScanning ? R.drawable.ic_pause_black_24dp : R.drawable.ic_play_arrow_black_24dp);
        mFab.hide(); //Thanks to a bug introduced in design lib 28.0.0 I have to call hide and how after setImageResource to make the icon visible
        mFab.show(); //This is because I am using mFab.show() and hide() when the view has been scrolled which causes the icon to not visible afterwards
        this.mScanningBar.setVisibility(mIsScanning ? View.VISIBLE : View.GONE);
    }

    @Override
    public void updateToolbar(Toolbar pToolbar){
        super.updateToolbar(pToolbar);
        pToolbar.setTitle("Nearby Devices");
        pToolbar.setSubtitle(btDeviceListAdapter.getItemCount() +" Devices");
        MenuItem refreshItem = pToolbar.getMenu().findItem(R.id.action_refresh);
        if(refreshItem != null)
            refreshItem.setVisible(true);
        this.mToolbar = pToolbar;
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



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(ScannedDeviceRecord item);
    }
}
