package com.blustream.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.blustream.view.base.BaseViewModel;
import com.blustream.view.base.BaseViewModelFactory;
import com.blustream.view.details.SensorDetailsFragment;
import com.blustream.view.sensors.PinnableSensorWrapper;
import com.blustream.view.sensors.SensorsAdapter;
import com.blustream.view.sensors.SensorsListFragment;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.blustream.logger.Log;
import io.blustream.sulley.models.ManufacturerData;
import io.blustream.sulley.repository.data.ProximityLogEvent;
import io.blustream.sulley.sensor.Sensor;
import io.blustream.sulley.sensor.SensorLifecycleListener;
import io.blustream.sulley.sensor.SensorManager;
import io.blustream.sulley.sensor.SensorManagerImpl;
import io.blustream.sulley.sensor.UhOhRemedyListener;
import io.blustream.sulley.service.ForegroundProximityService;
import io.blustream.sulley.service.ProximityServiceCompanion;
import io.blustream.sulley.utilities.PermissionChecker;

import static io.blustream.sulley.repository.data.ProximityLogEvent.Type.ADV;
import static io.blustream.sulley.repository.data.ProximityLogEvent.Type.IBEACON;

public class TestDataIntegrityActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, SensorsAdapter.OnSensorClickListener {

    public static final String SENSOR_KEY = "SENSOR_KEY";
    private static final int PERMISSION_CODE = 1;
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;
    @BindView(R.id.nav_view)
    protected NavigationView navigationView;
    @BindView(R.id.drawer_layout)
    protected DrawerLayout drawer;
    @BindView(R.id.containerFrame)
    protected FrameLayout containerFrame;
    protected TextView tv_version;
    private boolean isAdvertising, isProxMonitoring;
    private Menu menu;
    private SensorsListFragment sensorsListFragment;
    private BaseViewModel mainViewModel;
    private List<Sensor> advertisedSensors;
    private SensorManager mSensorManager;
    private HashSet<String> serialsToScan = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        mainViewModel = new ViewModelProvider(this, new BaseViewModelFactory(getApplication())).get(BaseViewModel.class);
        SensorManagerImpl.init(this);
        mSensorManager = SensorManagerImpl.getInstance();
        setupPermissions();
        navigationView.setNavigationItemSelectedListener(this);
        tv_version = navigationView.getHeaderView(0).findViewById(R.id.tv_app_version);
        tv_version.setText(String.format(Locale.getDefault(), "%s (%s)",
                BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        setUhOhRemedyListener();
        setListeners();
    }

    private void setupPermissions() {
        PermissionChecker permissionChecker = new PermissionChecker(this);
        if (!permissionChecker.checkPermissions()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_CODE);
        } else {
            loadFragment(sensorsListFragment = SensorsListFragment.newInstance());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadFragment(sensorsListFragment = SensorsListFragment.newInstance());
            } else {
                Toast.makeText(this, "Permission denied. BLE functions won't work", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setUhOhRemedyListener() {
        mSensorManager.setUhOhRemedyListener(new UhOhRemedyListener() {
            @Override
            public void onRecycleConnection() {
                Log.d("onRecycleConnection");
            }

            @Override
            public void onWaitAndSee() {
                Log.d("onWaitAndSee");
            }

            @Override
            public void onResetBle() {
                Log.d("onResetBle");
            }

            @Override
            public void onRestartPhone() {
                Log.d("onRestartPhone");
            }
        });
    }

    private void setListeners() {
        mSensorManager.setLifecycleListener(new SensorLifecycleListener() {
            @Override
            public void sensorDidAdvertise(Sensor sensor, ManufacturerData manufacturerData, int rssi) {
                onSensorDetected(sensor, rssi, ADV);
            }

            @Override
            public void onBeaconReceived(Sensor sensor, int rssi) {
                onSensorDetected(sensor, rssi, IBEACON);
            }
        });
    }

    private void onSensorDetected(Sensor sensor, int RSSI, ProximityLogEvent.Type type) {
        Log.d("sensorDidAdvertise" + sensor + " RSSI = " + RSSI);
        advertisedSensors.remove(sensor);
        advertisedSensors.add(sensor);
        serialsToScan.add(sensor.getSerialNumber());
        sensorsListFragment.updateSensorList(advertisedSensors);
        mSensorManager.getSensorCache().addSensor(sensor);
        Log.d("sensorDidAdvertise. advertisedSensors.size()  = " + advertisedSensors.size() + " RSSI = " + RSSI);
        ProximityLogEvent proximityLogEvent
                = new ProximityLogEvent(sensor.getSerialNumber(), new Date(), type);
        mainViewModel.getRepository().addLog(proximityLogEvent);
    }

    private void startAdvScan() {
        isAdvertising = true;
        advertisedSensors = new ArrayList<>();
        mSensorManager.startAdvScanning();
    }

    @Override
    protected String getActivityIdentifier() {
        return getClass().getName();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        updateProxItem(mainViewModel.getRepository().getIsProximityScanning().getValue());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_remove_all_logs) {
            mainViewModel.getRepository().removeAllLogs();
            return true;
        } else if (id == R.id.action_scan) {
            updateScanItem(startStopADV());
            return true;
        } else if (id == R.id.action_proximity) {
            updateProxItem(startStopProximity());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void postSerialsToMonitorWhenReady() {
        ProximityServiceCompanion.getIsReceiverRegistered().observe(this, aBoolean -> {
            if (aBoolean) {
                for (String serial : serialsToScan) {
                    Intent serialIntent = ProximityServiceCompanion.getAddSerialIntent(serial);//getAddMacIntent(mac);
                    LocalBroadcastManager.getInstance(TestDataIntegrityActivity.this).sendBroadcast(serialIntent);
                }
            }
        });
    }

    private void startProximityService() {
        Log.d("startProximityService");
        if (!serialsToScan.isEmpty()) {
            Intent intent = ForegroundProximityService.getStartIntent(this, R.drawable.ic_launcher_foreground);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            postSerialsToMonitorWhenReady();
            isProxMonitoring = true;
        } else {
            Log.d("No macs to monitor");
            isProxMonitoring = false;
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void updateScanItem(Boolean isScanning) {
        menu.findItem(R.id.action_scan).setTitle(isScanning ? R.string.stop_scan : R.string.start_scan);
    }

    public void updateProxItem(Boolean isScanning) {
        if (menu != null) {
            menu.findItem(R.id.action_proximity).setTitle(isScanning ? R.string.stop_proximity : R.string.start_proximity);
        }
    }

    @Override
    public void onSensorClick(Sensor sensor) {
        serialsToScan.clear();
        serialsToScan.add(sensor.getSerialNumber());
        SensorDetailsFragment fragment = SensorDetailsFragment.newInstance(sensor.getSerialNumber());
        toolbar.setTitle(sensor.getSerialNumber());
        loadFragment(fragment);
    }

    @Override
    public void onSensorCheckedChanged(PinnableSensorWrapper sensor) {
        //do nothing
    }

    private void stopAdvScan() {
        isAdvertising = false;
        if (mSensorManager.isAdvScanning()) {
            mSensorManager.stopAdvScanning();
        }
    }

    public boolean startStopADV() {
        if (isAdvertising) {
            stopAdvScan();
        } else {
            startAdvScan();
        }
        return isAdvertising;
    }

    public boolean startStopProximity() {
        if (isProxMonitoring) {
            stopProximityService();
        } else {
            startProximityService();
        }
        return isProxMonitoring;
    }

    private void stopProximityService() {
        Log.d("stopProximityService");
        Intent serviceIntent = ForegroundProximityService.getStopIntent(this);
        stopService(serviceIntent);
        isProxMonitoring = false;
    }

    public void setScanActionVisible(boolean visible) {
        if (menu != null) {
            menu.findItem(R.id.action_scan).setVisible(visible);
        }
    }

    public void setTitle(String title) {
        toolbar.setTitle(title);
    }

    public void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.containerFrame, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
