package com.blustream.view.sensors;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blustream.demo.Const;
import com.blustream.demo.R;
import com.blustream.demo.TestDataIntegrityActivity;
import com.blustream.view.base.BaseViewModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.blustream.logger.Log;
import io.blustream.sulley.sensor.Sensor;
import io.blustream.sulley.sensor.SensorManagerImpl;

public class SensorsListFragment extends Fragment implements SensorsAdapter.OnSensorClickListener {

    private static String PINNED_SENSORS_KEY = "PinnedSensors";
    private final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 123;
    @BindView(R.id.sensorsRecyclerView)
    protected RecyclerView sensorsRecyclerView;

    private SensorsAdapter adapter;

    private WeakReference<TestDataIntegrityActivity> parentActivity;
    private Set<String> pinnedSensorsSerials = new HashSet<>();
    private SharedPreferences preferences;
    private List<PinnableSensorWrapper> pinnableSensors = new ArrayList<>();
    private Handler handler;
    private BaseViewModel mViewModel;

    public static SensorsListFragment newInstance() {
        return new SensorsListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.sensors_list_fragment, container, false);
        ButterKnife.bind(this, root);
        preferences = getContext().getSharedPreferences(Const.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SensorManagerImpl.init(getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(sensorsRecyclerView.getContext(),
                layoutManager.getOrientation());
        sensorsRecyclerView.addItemDecoration(dividerItemDecoration);
        sensorsRecyclerView.setLayoutManager(layoutManager);
        if (getActivity() instanceof TestDataIntegrityActivity) {
            adapter = new SensorsAdapter(this);
            sensorsRecyclerView.setAdapter(adapter);
        }
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("onResume");
        handler = new Handler(Looper.myLooper());
        parentActivity.get().setScanActionVisible(true);
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateSensorList(SensorManagerImpl.getInstance().getSensorCache().getSensors());
                handler.postDelayed(this, 1000);
            }
        });
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d("onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        parentActivity = new WeakReference<>((TestDataIntegrityActivity) getActivity());
        if (parentActivity != null) {
            parentActivity.get().setTitle(getString(R.string.sensors));
        }
        if (Build.VERSION.SDK_INT >= 23) {
            // Marshmallow+ Permission APIs
            requestPermission();
        }
        // TODO: Use the ViewModel
    }


    @Override
    public void onPause() {
        super.onPause();
        parentActivity.get().setScanActionVisible(false);
        handler.removeCallbacksAndMessages(null);
    }

    public void updateSensorList(List<Sensor> sensors) {
        pinnableSensors.clear();
        getPinnedSensorsFromShPref();
        for (Sensor sensor : sensors) {
            PinnableSensorWrapper pinnableSensor = new PinnableSensorWrapperImpl(sensor);
            if (pinnedSensorsSerials.contains(sensor.getSerialNumber())) {
                pinnableSensor.setPinned(true); // restored from shPref
            }
            pinnableSensors.add(pinnableSensor);
        }
        sortSensors(pinnableSensors);
        adapter.setSensorList(pinnableSensors);
    }

    private void sortSensors(List<PinnableSensorWrapper> pinnableSensors) {
        Collections.sort(pinnableSensors, (sensorWrapper, t1) -> {
            Boolean isFirstPinned = sensorWrapper.isPinned();
            Boolean isSecondChecked = t1.isPinned();
            int compareByPinned = 0;
            compareByPinned = isSecondChecked.compareTo(isFirstPinned);
            if (compareByPinned == 0) {
                compareByPinned = Integer.compare(t1.getSensor().getAdvertisedRssi(), sensorWrapper.getSensor().getAdvertisedRssi());
            }
            return compareByPinned;
        });
    }

    private void savePinnedSensorsToShPref() {
        SharedPreferences.Editor edit = preferences.edit();
        edit.clear();
        edit.putStringSet(PINNED_SENSORS_KEY, pinnedSensorsSerials);
        edit.apply();
        getPinnedSensorsFromShPref();
        pinnedSensorsSerials.size();
    }

    private void getPinnedSensorsFromShPref() {
        pinnedSensorsSerials = preferences.getStringSet(PINNED_SENSORS_KEY, new HashSet<>());
    }

    @Override
    public void onSensorClick(Sensor sensor) {
        if (getActivity() != null && getActivity() instanceof TestDataIntegrityActivity)
            ((TestDataIntegrityActivity) getActivity()).onSensorClick(sensor);
    }

    @Override
    public void onSensorCheckedChanged(PinnableSensorWrapper sensorWrapper) {
        if (sensorWrapper.isPinned()) {
            pinnableSensors.remove(sensorWrapper);
            pinnableSensors.add(sensorWrapper);
            pinnedSensorsSerials.add(sensorWrapper.getSensor().getSerialNumber());
        } else {
            pinnedSensorsSerials.remove(sensorWrapper.getSensor().getSerialNumber());
        }
        savePinnedSensorsToShPref();
        sortSensors(pinnableSensors);
        sensorsRecyclerView.post(() -> {
            adapter.setSensorList(pinnableSensors);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                // Initial
                perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++) {
                    perms.put(permissions[i], grantResults[i]);
                }

                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Permission Denied
                    Toast.makeText(getContext(), "One or More Permissions are DENIED Exiting App.", Toast.LENGTH_SHORT)
                            .show();
                    parentActivity.get().finish();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermission() {
        List<String> permissionsNeeded = new ArrayList<>();
        final List<String> permissionsList = new ArrayList<>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            permissionsNeeded.add("Show Location");
        }
        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                return;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (parentActivity.get().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            return shouldShowRequestPermissionRationale(permission);
        }
        return true;
    }
}
