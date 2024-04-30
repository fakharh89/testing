package com.blustream.view.proximity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.blustream.demo.R;
import com.blustream.view.base.BaseViewModel;
import com.blustream.view.base.BaseViewModelFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.blustream.logger.Log;
import io.blustream.sulley.repository.data.ProximityLogEvent;
import io.blustream.sulley.sensor.Sensor;
import io.blustream.sulley.sensor.SensorManagerImpl;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

import static com.blustream.demo.TestDataIntegrityActivity.SENSOR_KEY;

public class ProximityFragment extends Fragment {

    private final int SHOWN_LOGS_QUANTITY = 30;
    @BindView(R.id.iBeaconLogCb)
    protected CheckBox iBeaconLogCb;
    @BindView(R.id.advLogCb)
    protected CheckBox advLogCb;
    @BindView(R.id.connectionLogCb)
    protected CheckBox connectionLogCb;
    @BindView(R.id.dataReceivedLogCb)
    protected CheckBox dataReceivedLogCb;
    @BindView(R.id.logContentTextView)
    protected TextView logContentTextView;
    @BindView(R.id.lastSeenTextView)
    protected TextView lastSeenTextView;
    @BindView(R.id.longestIntervalTextView)
    protected TextView longestIntervalTextView;
    @BindView(R.id.averageIntervalTextView)
    protected TextView averageIntervalTextView;
    @BindView(R.id.logLabelTextViewTv)
    protected TextView logLabelTextViewTv;
    @BindView(R.id.lastScanSessionDurationTv)
    protected TextView lastScanSessionDurationTv;
    @BindView(R.id.missedSensorPercentageTv)
    protected TextView missedSensorPercentageTv;
    @BindView(R.id.logsTimeoutsSwitch)
    protected Switch logsTimeoutsSwitch;

    private BaseViewModel mViewModel;
    private Sensor sensor;
    private long longestIntervalMs, logsQuantity, shownLogs, lastTimeLogsQuantity, lossesSumMs, scanDuration;
    private int iBeaconQuantity, advQuantity, connectQuantity, dataReceivedQuantity, lossesQuantity;
    private float lossesPercentage, averageIntervalMs;
    private Handler handler;
    private boolean shouldShowADV, shouldShowBeacons, shouldShowConnectionEvents, shouldShowDataReceivedEvents;
    private TreeMap<Long, Date> timeouts, sortedTimeouts;
    private List<Long> intervals;

    public static ProximityFragment newInstance(String sensorSerial) {
        ProximityFragment fragment = new ProximityFragment();
        Bundle args = new Bundle();
        args.putString(SENSOR_KEY, sensorSerial);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.proximity_fragment, container, false);
        ButterKnife.bind(this, root);
        initLogsView();
        initCheckBoxes();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        startUpdatingTimings();
        mViewModel.getRepository().getLogs().observe(this, (list) -> reloadLogs());
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this, new BaseViewModelFactory(getActivity().getApplication())).get(BaseViewModel.class);
        if (getArguments() != null) {
            String serial = getArguments().getString(SENSOR_KEY);
            sensor = SensorManagerImpl.getInstance().getSensorCache().getExistingSensorFromSerialNumber(serial);
        }
    }

    private void clearEventsQuantity() {
        connectQuantity = 0;
        iBeaconQuantity = 0;
        advQuantity = 0;
        dataReceivedQuantity = 0;

    }

    private void emptySummaryVariables() {
        clearEventsQuantity();
        longestIntervalMs = 0;
        averageIntervalMs = 0;
        logsQuantity = 0;
        //lastTimeLogsQuantity = 0;
        lossesQuantity = 0;
        lossesPercentage = 0;
        lossesSumMs = 0;

    }

    private void showLastNLogs(List<ProximityLogEvent> list, int quantity) {
        Log.d("showLastNLogs " + list.size() + " quantity = " + quantity);
        if (list.size() > SHOWN_LOGS_QUANTITY) { // show last 30 events
            logContentTextView.setText("");
            quantity = SHOWN_LOGS_QUANTITY;
        }

        for (int i = list.size() - quantity; i < list.size(); i++) {
            logContentTextView.append("\n");
            logContentTextView.append(list.get(i).toString());
            logContentTextView.append("\n");
        }
    }

    private void initLogsView() {
        //logContentTextView.setMovementMethod(new ScrollingMovementMethod());
        logContentTextView.setTextIsSelectable(true);
        updateLastSeen();
        // ViewTreeObserver vto = logContentTextView.getViewTreeObserver();
//        vto.addOnGlobalLayoutListener(() -> {
//            final int scrollAmount = logContentTextView.getLayout().getLineTop(logContentTextView.getLineCount()) - logContentTextView.getHeight();
//            logContentTextView.scrollTo(0, Math.max(scrollAmount, 0));
//        });
    }

    private void updateLastSeen() {
        if (sensor != null) {
            long lastSeen = (new Date().getTime() - sensor.getVisibilityStatus().getDate().getTime());
            if (lastSeen > longestIntervalMs) {
                longestIntervalMs = lastSeen;
            }
            longestIntervalTextView.setText(getContext().getString(R.string.longest_interval, longestIntervalMs / 1000));
            lastSeenTextView.setText(getContext().getString(R.string.last_seen, lastSeen / 1000));
            averageIntervalTextView.setText(getContext().getString(R.string.average_interval,
                    String.format(Locale.getDefault(), "%.02f", averageIntervalMs / 1000)));
            logLabelTextViewTv.setText(getContext().getString(R.string.log, logsQuantity));
            iBeaconLogCb.setText(getContext().getString(R.string.ibeacon_events, iBeaconQuantity));
            advLogCb.setText(getContext().getString(R.string.advertisement_events, advQuantity));
            connectionLogCb.setText(getContext().getString(R.string.connection_state_changes, connectQuantity));
            dataReceivedLogCb.setText(getContext().getString(R.string.data_received_events, dataReceivedQuantity));
            missedSensorPercentageTv.setText(getContext().getString(R.string.losses_percentage, String.format(Locale.getDefault(), "%.02f", lossesPercentage), lossesQuantity));
            lastScanSessionDurationTv.setText(getContext().getString(R.string.scan_duration, fromMillisToHumanReadable(scanDuration)));
            Log.d("updateLastSeen. avarageInterval = " + averageIntervalMs + " longestInterval = " + longestIntervalMs / 1000);
        }
    }

    private String fromMillisToHumanReadable(long millis) {
        return String.format(Locale.getDefault(), "%02d hours %02d min, %02d sec",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }

    private synchronized void analyzeEvents(List<ProximityLogEvent> events) {
        long analyzeStartTime = new Date().getTime();
        Log.d("analyzeEvents. events.size = " + events.size());
        intervals = new ArrayList<>();
        timeouts = new TreeMap<>();
        long sumMs = 0L;
        emptySummaryVariables();
        logsQuantity = events.size();
        for (int i = 0; i < events.size() - 1; i++) {
            long interval = (events.get(i + 1).getDate().getTime() - events.get(i).getDate().getTime());
            if (interval > longestIntervalMs) {
                longestIntervalMs = interval;
            }
            if (interval > sensor.getVisibilityStatus().getEventTimeout()) {
                lossesQuantity++;
                lossesSumMs += interval;
                timeouts.put(interval, events.get(i).getDate());
            }
            countEvent(events.get(i));
            intervals.add(interval);
            sumMs += interval;
        }

        sortedTimeouts = new TreeMap<>();
        sortedTimeouts.putAll(timeouts);
        if (!events.isEmpty()) {
            countEvent(events.get(events.size() - 1)); //count last event.
            if (mViewModel.getRepository().getIsProximityScanning().getValue()) {
                scanDuration = new Date().getTime() - events.get(0).getDate().getTime();
            } else {
                scanDuration = events.get(events.size() - 1).getDate().getTime() - events.get(0).getDate().getTime();
            }
            if (sumMs > 0) {
                lossesPercentage = (lossesSumMs * 100f / sumMs); //(int)((n * 100.0f) / v);
            }
        }
        if (!intervals.isEmpty()) {
            averageIntervalMs = sumMs / intervals.size();
        }
        long analyzeFinishedTime = new Date().getTime();

        Log.d("analyzeEvents.\nlongestInterval = " + longestIntervalMs + " avarageInterval = " + averageIntervalMs + "events.size = " + events.size()
                + " duration (ms): " + (analyzeFinishedTime - analyzeStartTime));
    }

    private void countEvent(ProximityLogEvent event) {
        switch (event.getProximityEventTypeAsEnum()) {
            case ADV:
                advQuantity++;
                break;
            case IBEACON:
                iBeaconQuantity++;
                break;
            case CONNECTION:
                connectQuantity++;
                break;
            case DATA_RECEIVED:
                dataReceivedQuantity++;
                break;
        }
    }

    private void startUpdatingTimings() {
        handler = new Handler(Looper.myLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateLastSeen();
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void reloadLogs() {
        Log.d("reloadLogs");
        clearLogTextView();
        if (logsTimeoutsSwitch.isChecked()) {
            showTimeouts();
        } else {
            showLogs(mViewModel.getRepository().getLogs().getValue());
        }
    }

    private void showTimeouts() {
        for (Map.Entry entry : timeouts.entrySet()) {
            logContentTextView.append("\n");
            logContentTextView.append(entry.getKey() + " ms, " + entry.getValue());
            logContentTextView.append("\n");
        }
    }

    private void showLogs(List<ProximityLogEvent> proximityLogEvents) {
        Log.d("showLogs");
        if (proximityLogEvents.isEmpty()) {
            clearLogTextView();
            emptySummaryVariables();
        }
        Disposable subscribe = Observable
                .fromIterable(proximityLogEvents)
                .filter(proximityLogEvent -> proximityLogEvent.getSensorSerial().equals(ProximityFragment.this.sensor.getSerialNumber()))
                .filter(event -> (shouldShowADV || !event.getProximityEventTypeAsEnum().equals(ProximityLogEvent.Type.ADV)) &&
                        (shouldShowBeacons || !event.getProximityEventTypeAsEnum().equals(ProximityLogEvent.Type.IBEACON)) &&
                        (shouldShowConnectionEvents || !event.getProximityEventTypeAsEnum().equals(ProximityLogEvent.Type.CONNECTION)) &&
                        (shouldShowDataReceivedEvents || !event.getProximityEventTypeAsEnum().equals(ProximityLogEvent.Type.DATA_RECEIVED)))
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .toList()
                .subscribe((list) -> {
                    long newLogs = list.size() - lastTimeLogsQuantity;
                    lastTimeLogsQuantity = list.size();
                    showLastNLogs(list, ((Long) newLogs).intValue());
                    analyzeEvents(list);
                    updateLastSeen();
                    Log.d("onSubscribe.newLogs = " + newLogs);
                }, (throwable) -> {
                    Log.e("showLogs. onError = " + throwable);
                    logContentTextView.append("\n");
                    logContentTextView.append(throwable.getMessage());
                    logContentTextView.append("\n");
                });
    }

    private void initCheckBoxes() {
        shouldShowBeacons = iBeaconLogCb.isChecked();
        shouldShowADV = advLogCb.isChecked();
        shouldShowConnectionEvents = connectionLogCb.isChecked();
        shouldShowDataReceivedEvents = dataReceivedLogCb.isChecked();
        iBeaconLogCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            shouldShowBeacons = isChecked;
            reloadLogs();
        });
        advLogCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            shouldShowADV = isChecked;
            reloadLogs();
        });
        connectionLogCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            shouldShowConnectionEvents = isChecked;
            reloadLogs();
        });
        dataReceivedLogCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            shouldShowDataReceivedEvents = isChecked;
            reloadLogs();
        });
        logsTimeoutsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reloadLogs();
            }
        });
    }

    private void clearLogTextView() {
        Log.d("clearLogTextView");
        logContentTextView.setText("");
        lastTimeLogsQuantity = 0;
    }
}
