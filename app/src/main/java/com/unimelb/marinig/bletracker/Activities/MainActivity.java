package com.unimelb.marinig.bletracker.Activities;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.unimelb.marinig.bletracker.Adapters.FilterPopupWindow;
import com.unimelb.marinig.bletracker.Adapters.SortPopupWindow;
import com.unimelb.marinig.bletracker.Adapters.Data.ServiceContent;
import com.unimelb.marinig.bletracker.Adapters.Data.ServiceBootstrapper;
import com.unimelb.marinig.bletracker.Adapters.RelativePopupWindow;
import com.unimelb.marinig.bletracker.Adapters.ServicesListRecyclerViewAdapter;
import com.unimelb.marinig.bletracker.BuildConfig;
import com.unimelb.marinig.bletracker.Events.OrderUpdatedEvent;
import com.unimelb.marinig.bletracker.Fragments.BeaconBatchConfigFragment;
import com.unimelb.marinig.bletracker.Fragments.ConfigFragment;
import com.unimelb.marinig.bletracker.Fragments.TrackerLogFragment;
import com.unimelb.marinig.bletracker.Interfaces.ToolbarFragment;
import com.unimelb.marinig.bletracker.Model.ScannedDeviceRecord;
import com.unimelb.marinig.bletracker.Model.TrackerPreferences;
import com.unimelb.marinig.bletracker.Fragments.ScannedDevicesFragment;
import com.unimelb.marinig.bletracker.Fragments.ServicesListFragment;
import com.unimelb.marinig.bletracker.Services.BLEScanner;
import com.unimelb.marinig.bletracker.R;
import com.unimelb.marinig.bletracker.Logger.LogFile;
import com.unimelb.marinig.bletracker.Logger.LogWrapper;
import com.unimelb.marinig.bletracker.Logger.TrackerLog;
import com.unimelb.marinig.bletracker.Utils.GpsUtils;
import com.unimelb.marinig.bletracker.Utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, NavigationView.OnNavigationItemSelectedListener, ServicesListFragment.OnListFragmentInteractionListener, ScannedDevicesFragment.OnListFragmentInteractionListener, ConfigFragment.OnFragmentInteractionListener , BeaconBatchConfigFragment.OnFragmentInteractionListener {
    public static final String TAG = "MainActivity";
    //private final static String TAG = BeaconScanActivity.class.getSimpleName(); //Same thing
    private final static String FRAGMENT_DEVICES_LIST_TAG = "devices_list_frag";
    private final static String FRAGMENT_HOME_TAG = "home_frag";
    private final static String FRAGMENT_SERVICE_LIST_TAG = "services_list_frag";
    private final static String FRAGMENT_LOG_TAG= "log_frag";
    private final static String FRAGMENT_CONFIG_TAG = "config_frag";
    private final static String FRAGMENT_CONFIG_BEACONS_TAG = "batch_config_frag";

    private TrackerPreferences mPreferences;
    private Toolbar mToolbar;
    private ToolbarFragment lastFragment;

    //Background Services
    private ServiceContent mBLEScanner;
    private boolean mShouldUnbind; // Don't attempt to unbind from the service unless the client has received some information about the service's state.
    private ArrayList<ServiceContent> mServicesList = new ArrayList<ServiceContent>();
    private ServicesListRecyclerViewAdapter mServicesListAdapter;

    //Permissions and request codes
    public final static int REQUEST_CODE = 200;
    public static final int LOCATION_REQUEST = 1000;
    public static final int GPS_REQUEST = 1001;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;
    public static final int REQUEST_ENABLE_BT = 1;
    private int PERMISSION_ALL = 1;
    private ArrayList<String> mPermission_list;
    private String[] mPermission_array;

    //Views elements
    private TextView mTextMessage;
    private BottomNavigationView mNavigation;
//    private NavigationView mNavigation;
    private View mMainActivityView;

    public static int mBottomNavigationHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        mPreferences = new TrackerPreferences(this);
        if(savedInstanceState == null) {
            //Initialize logging here so we can log even before showing the Log Fragment, eventually when the LogFragment will attach the logview to the list of loggers
            //It will flush to it all the past logs
            mPreferences.init(this,false); //true to clear preferences
            mPreferences.set(TrackerPreferences.Settings.DEPLOY_MODE, TrackerPreferences.DeployMode.DEBUG); //Disable log
            mPreferences.set(TrackerPreferences.Settings.ENABLE_LOG_VIEW, false);
            mPreferences.set(TrackerPreferences.Settings.NOTIFICATION_CHANNEL_ID, "ble_notif_id");
            mPreferences.set(TrackerPreferences.Settings.NOTIFICATION_CHANNEL_NAME, "Service Channel");
            mPreferences.set(TrackerPreferences.Settings.NOTIFICATION_SERVER_CHANNEL_ID, "ble_notif_server_id");
            mPreferences.set(TrackerPreferences.Settings.NOTIFICATION_SERVER_CHANNEL_NAME, "Server Channel");
            mPreferences.set(TrackerPreferences.Settings.APP_VERSION, BuildConfig.VERSION_NAME);
            initializeLogging();

            setContentView(R.layout.activity_main);
            mMainActivityView = this.findViewById(R.id.mainContainer);

            mTextMessage = (TextView) findViewById(R.id.message);
            mNavigation = (BottomNavigationView) findViewById(R.id.navigation);
//            mNavigation = (NavigationView) findViewById(R.id.navigation);

            mBottomNavigationHeight = mNavigation.getItemIconSize();
//            if(!mPreferences.getBool(TrackerPreferences.Settings.ENABLE_LOG_VIEW))
//                mNavigation.getMenu().removeItem(R.id.navigation_log);

            //Hold on! What's up with this weird nonsense?? Well, I wanted a list of active services with relative buttons to start/stop the service, for testing and debugging
            //So I created RecyclerViewAdapter with all that it needed, in a fragment. The adapter takes a list of ServiceContent which are simply services
            mServicesListAdapter = new ServicesListRecyclerViewAdapter(mServicesList, (ServicesListFragment.OnListFragmentInteractionListener) MainActivity.this);

            //And now THE MAGIC!! I am adding new services to the list, all of them have a custom initializator function
            //So I am not passing Context or Intent, and instead I am only passing its name, and the ServiceBootstrapper which calls the method in this class
            //to start the ServerService or the SensorService
            mBLEScanner = new ServiceContent("BLE_Scanner", BLEScanner.class.getName(), new ServiceBootstrapper() {
                @Override
                public void startService() {
                    TrackerLog.d("YO", "YO it's the start of BLE Scanner here");
                    Intent serviceIntent = new Intent(MainActivity.this, BLEScanner.class);
                    ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
                }

                @Override
                public void stopService() {
                    Intent serviceIntent = new Intent(MainActivity.this, BLEScanner.class);
                    serviceIntent.putExtra("stop", "stop");
                    ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
                }

                @Override
                public boolean isRunning() {
                    return Utils.isMyServiceRunning(MainActivity.this, BLEScanner.class.getName());
                }
            });

            mServicesList.add(mBLEScanner);

            //Permissions request
            mPermission_list = new ArrayList<String>();
            mPermission_list.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
            mPermission_list.add(Manifest.permission.BLUETOOTH_ADMIN);
            mPermission_list.add(Manifest.permission.BLUETOOTH);
            mPermission_list.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            mPermission_list.add(Manifest.permission.READ_PHONE_STATE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                TrackerLog.e("ActivityPermission", "Adding coarse location permission request");
                mPermission_list.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                mPermission_list.add(Manifest.permission.ACCESS_FINE_LOCATION);
                //requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }

            mPermission_array = new String[mPermission_list.size()];
            mPermission_array = mPermission_list.toArray(mPermission_array);
            if (!hasAllPermissions(this, mPermission_array)) {
                TrackerLog.i("PERMISSIONS_CHECK", "Asking for permissions before starting the services");
                //The requestPermissions call is asynchronous so we cannot wait for the permission granted callback to create the array of bluetooth device
                requestPermissions(mPermission_array, PERMISSION_ALL);
            } else {
                TrackerLog.i("PERMISSIONS_CHECK", "Already got all permissions, starting services");
                startAllServices();
            }

            new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
                @Override
                public void gpsStatus(boolean isGPSEnable) {
                    // turn on GPS
                    TrackerLog.e(TAG, "GPS ENABLED");
                }
            });


            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            //Toolbar will now take on default Action Bar characteristics
            setSupportActionBar(mToolbar);
            //You can now use and reference the ActionBar
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            mToolbar.setTitleTextColor(Color.WHITE);
            mToolbar.setSubtitleTextColor(Color.WHITE);
        }

        if(getIntent() != null){
            String fragmentToOpen = getIntent().getStringExtra("openFragment");
            TrackerLog.d(TAG, "Open from notification: " +fragmentToOpen);
            if(fragmentToOpen != null && (fragmentToOpen.equals(ServicesListFragment.class.getSimpleName()) || fragmentToOpen.equals("test"))){
                ServicesListFragment servicesListFragment = new ServicesListFragment();
                servicesListFragment.setServicesList(mServicesList);
                servicesListFragment.setServicesListAdapter(mServicesListAdapter); //The adapter listening to changes on the list of services is then set in the fragment which has the RecyclerView
                servicesListFragment.updateServicesList(); //Notify that something has changed in the list of services, so the adapter will update the RecyclerView list
                getSupportFragmentManager().beginTransaction().replace(R.id.container, servicesListFragment, "servicesListFrag").commit();
            }
        }

        //Reminder: Another way of doing this would be
        //private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener(){...}
        //And then pass mOnNavigationItemSelectedListener as parameter here below.
        //Same goes for the ServiceBootstrapper for the ServiceContents
        mNavigation.setOnNavigationItemSelectedListener(this); //Bottom
//        mNavigation.setNavigationItemSelectedListener(this);

        //Finally showing the main fragment
        mNavigation.setSelectedItemId(R.id.navigation_log);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent != null){
            String fragmentToOpen = intent.getStringExtra("openFragment");
            TrackerLog.d(TAG, "Open from notification: " +fragmentToOpen);
            if(fragmentToOpen != null && (fragmentToOpen.equals(ServicesListFragment.class.getSimpleName()) || fragmentToOpen.equals("test"))){
                ServicesListFragment servicesListFragment = new ServicesListFragment();
                servicesListFragment.setServicesList(mServicesList);
                servicesListFragment.setServicesListAdapter(mServicesListAdapter); //The adapter listening to changes on the list of services is then set in the fragment which has the RecyclerView
                servicesListFragment.updateServicesList(); //Notify that something has changed in the list of services, so the adapter will update the RecyclerView list
                getSupportFragmentManager().beginTransaction().replace(R.id.container, servicesListFragment, "servicesListFrag").commitNow();
            }
        }
    }

    public void startAllServices(){
        //Checking permissions and support
        checkDrawOverlayPermission();
        checkBLESupport();

        mBLEScanner.startServiceContent();

        if(TrackerLog.isEnabled()) {
            //Now that we have permission we can add the log on the file
            TrackerLog.printNodeList("NODE_LIST1");
            LogFile logFile = new LogFile(this);
            TrackerLog.insertNodeAfter(logFile, LogWrapper.class);
            TrackerLog.flushToNode(logFile);
            TrackerLog.printNodeList("NODE_LIST2");
            TrackerLog.i(TAG, "LogFile Ready");
        }

        //Creating notification
        //ServiceNotificationManager.createNotificationChannel(mPreferences, this);
    }

    public void stopAllServices(){
        mBLEScanner.stopServiceContent();
    }


    @Override
    protected void onStart(){
        super.onStart();
    }

    public boolean hasAllPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        TrackerLog.e("PERMISSIONS_CHECK","Got permissions, code: " + requestCode);
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                TrackerLog.e("PERMISSIONS","COARSE PERMISSION");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //btDeviceListAdapter = new BTDevicesRecyclerViewAdapter(bluetoothDeviceList, (ScannedDevicesFragment.OnListFragmentInteractionListener) MainActivity.this);
                } else {
                    TrackerLog.i(TAG, "Fail to get PERMISSION_REQUEST_COARSE_LOCATION");
                }
            }
        }
        TrackerLog.i("PERMISSIONS_CHECK", "Starting services");
        startAllServices();
    }

    @Override
    protected void  onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == this.REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void openOptionsMenu() {
        super.openOptionsMenu();
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh: {
                ScannedDevicesFragment frag = ((ScannedDevicesFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_DEVICES_LIST_TAG));
                if(frag != null && frag.isVisible()) {
                    frag.refreshList();
                }
                else{
                    BeaconBatchConfigFragment frag2 = ((BeaconBatchConfigFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_CONFIG_BEACONS_TAG));
                    if(frag2 != null && frag2.isVisible()) {
                        frag2.refreshList();
                    }

                }
                return false;
            }
            case R.id.sort_menu:{
                showSortMenu();
                return false;
            }
            case R.id.filter_menu:{
                showFilterMenu();
                return false;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /** Set up targets to receive log data */
    public void initializeLogging() {
        TrackerLog.setEnabled(mPreferences.isDebug());
        if(!TrackerLog.isEnabled())
            return;
        //This is actually pretty cool! It's a chain of loggers, the first that will receive the log message is logFIle in this case, which is set through setLogNode
        //All the other loggers will receive an output from the previous node, allowing to filter
        //The log before sending it to the next node, in my case I don't filter anything cause I want the log to be printed
        LogWrapper logWrapper = new LogWrapper(); // Wraps Android's native log framework.
        TrackerLog.setLogNode(logWrapper);
        if(mPreferences.getBool(TrackerPreferences.Settings.ENABLE_LOG_VIEW))
            TrackerLog.startLogRecording();

        TrackerLog.i(TAG, "LogWrapper Ready");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkDrawOverlayPermission() {
        /** check if we already  have permission to draw over other apps */
        if (!Settings.canDrawOverlays(this)) {
            /** if not construct intent to request permission */
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            /** request permission via start activity for result */
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    public void checkBLESupport() {
        BluetoothAdapter mBluetoothAdapter = ((BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        if (!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
            this.finish();
        }

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }

        if (!mBluetoothAdapter.isEnabled() || mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
            //Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //this.startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
            mBluetoothAdapter.enable();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = null;
        ToolbarFragment frag = null;
        String tag = "";
        switch (item.getItemId()) {
            case R.id.navigation_services:
                tag = FRAGMENT_SERVICE_LIST_TAG;
                ServicesListFragment servListFrag = (ServicesListFragment) fm.findFragmentByTag(tag);
                if(servListFrag == null){
                    servListFrag = new ServicesListFragment();
                    servListFrag.setServicesList(mServicesList);
                    servListFrag.setServicesListAdapter(mServicesListAdapter); //The adapter listening to changes on the list of services is then set in the fragment which has the RecyclerView
                    servListFrag.updateServicesList(); //Notify that something has changed in the list of services, so the adapter will update the RecyclerView list
                    ft = fm.beginTransaction().add(R.id.container, servListFrag, tag);
                }
                frag = servListFrag;
                break;
            case R.id.navigation_scan:
                tag = FRAGMENT_DEVICES_LIST_TAG;
                ScannedDevicesFragment devListFrag = (ScannedDevicesFragment) fm.findFragmentByTag(tag);
                if(devListFrag == null){
                    devListFrag = new ScannedDevicesFragment();
                    devListFrag.setBLEServiceContent(mBLEScanner);
                    devListFrag.initializeAdapter(MainActivity.this);
                    ft = fm.beginTransaction().add(R.id.container, devListFrag, tag);
                }
                frag = devListFrag;
                break;
            case R.id.navigation_log:
                tag = FRAGMENT_LOG_TAG;
                TrackerLogFragment logFrag = (TrackerLogFragment) fm.findFragmentByTag(tag) ;
                if(logFrag == null){
                    logFrag = new TrackerLogFragment();
                    ft = fm.beginTransaction().add(R.id.container, logFrag, tag);
                }
                frag = logFrag;
                break;
            case R.id.navigation_config:
                tag = FRAGMENT_CONFIG_TAG;
                ConfigFragment configFrag = (ConfigFragment) fm.findFragmentByTag(tag);
                if(configFrag == null){
                    configFrag = new ConfigFragment();
                    ft = fm.beginTransaction().add(R.id.container, configFrag, tag);
                }
                frag = configFrag;
                break;
            case R.id.navigation_config_beacons:
                tag = FRAGMENT_CONFIG_BEACONS_TAG;
                BeaconBatchConfigFragment batchConfigFrag = (BeaconBatchConfigFragment) fm.findFragmentByTag(tag);
                if(batchConfigFrag == null){
                    batchConfigFrag = new BeaconBatchConfigFragment();
                    batchConfigFrag.setBLEServiceContent(mBLEScanner);
                    batchConfigFrag.initializeAdapter(MainActivity.this);
                    ft = fm.beginTransaction().add(R.id.container, batchConfigFrag, tag);
                }
                frag = batchConfigFrag;
                break;
        }

        if(frag != null){
            frag.updateToolbar(mToolbar);
            if(ft == null)
                ft = fm.beginTransaction();

            if(lastFragment != null) {
                ft.hide(lastFragment);
            }

            ft.show(frag).commitNow();
            lastFragment = frag;
//            if(mNavigation){
//                DrawerLayout drawer = findViewById(R.id.drawer_layout);
//                drawer.closeDrawer(GravityCompat.START);
//            }
            return true;
        }
        return false;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        //doUnbindService();
    }

    @Override
    public void onListFragmentInteraction(ServiceContent item) {

    }

    @Override
    public void onListFragmentInteraction(ScannedDeviceRecord item) {
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }


    private void showSortMenu(){
        final SortPopupWindow menu = new SortPopupWindow(this );
        menu.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        menu.setWidth(getPxFromDp(200));
        menu.setOutsideTouchable(true);
        menu.setFocusable(true);
        //menu.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        menu.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        menu.setElevation(20);
        menu.setTouchable(true);
        menu.showOnAnchor(findViewById(R.id.sort_menu), RelativePopupWindow.VerticalPosition.BELOW, RelativePopupWindow.HorizontalPosition.ALIGN_RIGHT);
        menu.setSortSelectedListener(new SortPopupWindow.SortSelectedListener() {
            @Override
            public void onSortSelected(int position, SortPopupWindow.SortItem sortItem, TextView view) {
                EventBus.getDefault().post(new OrderUpdatedEvent(sortItem.sortId));
                ScannedDevicesFragment frag = ((ScannedDevicesFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_DEVICES_LIST_TAG));
                if(frag != null && frag.isVisible())
                    frag.updateSorting(sortItem.sortId);
                if(sortItem.sortOrder <= 0) {
                    sortItem.sortOrder = 1;
                }
                else{
                    sortItem.sortOrder = 0;
                }
            }
        });
    }

    private void showFilterMenu(){
        final FilterPopupWindow menu = new FilterPopupWindow(this );
        menu.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        menu.setWidth(getPxFromDp(200));
        menu.setOutsideTouchable(true);
        menu.setFocusable(true);
        menu.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        menu.setElevation(10);
        menu.showOnAnchor(findViewById(R.id.filter_menu), RelativePopupWindow.VerticalPosition.BELOW, RelativePopupWindow.HorizontalPosition.ALIGN_RIGHT);
        menu.setFilterClickedListener(new FilterPopupWindow.FilterClickedListener() {
            @Override
            public void onFilterSelected(int position, FilterPopupWindow.FilterDropdownAdapter.FilterMenuItem filterItem, boolean isChecked) {
                //menu.dismiss();
                //EventBus.getDefault().post(new FilterUpdatedEvent(filterItem.id, isChecked)); //BLEScanner.FILTER_PROJECT_BEACONS
                ScannedDevicesFragment frag = ((ScannedDevicesFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_DEVICES_LIST_TAG));
                if(frag != null && frag.isVisible())
                    frag.updateFilter(filterItem.id, isChecked);
                Toast toast = Toast.makeText(getBaseContext(), "Filter " +filterItem.filterTitle +" " +isChecked, Toast.LENGTH_SHORT);
                toast.show();
                TrackerLog.d("DeviceList", "Filter " +filterItem.filterTitle +" " +isChecked);
            }
        });
    }

    //Convert DP to Pixel
    private int getPxFromDp(int dp){
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
