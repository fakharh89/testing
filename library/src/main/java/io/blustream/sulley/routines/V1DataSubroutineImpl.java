package io.blustream.sulley.routines;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;

import java.util.ArrayList;
import java.util.List;

import io.blustream.logger.Log;
import io.blustream.sulley.mappers.V1HumidTempMapper;
import io.blustream.sulley.mappers.V1ImpactMapper;
import io.blustream.sulley.mappers.V1MotionMapper;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.models.HumidTempSample;
import io.blustream.sulley.models.ImpactSample;
import io.blustream.sulley.models.MotionSample;
import io.blustream.sulley.sensor.Sensor;

class V1DataSubroutineImpl implements V1DataSubroutine {
    @NonNull
    private final Sensor mSensor;

    @NonNull
    private final BleDefinitions mDefinitions;

    private Listener mListener;

    public V1DataSubroutineImpl(@NonNull Sensor sensor, @NonNull BleDefinitions definitions) {
        mSensor = sensor;
        mDefinitions = definitions;
    }

    @Override
    public boolean isSensorCompatible(Sensor sensor) {
        boolean areCharsComp = DefinitionCheckHelper.checkSensor(mDefinitions, sensor);
        boolean isV1 = sensor.getSoftwareVersion().startsWith("1");
        return areCharsComp && isV1;
    }

    @Override
    public boolean start() {
        if (!isSensorCompatible(mSensor)) {
            return false;
        }
        mSensor.getBleDevice().enableNotify(mDefinitions.getDataService(),
                mDefinitions.getHumidTempDataCharacteristic(), e -> {
                    if (!e.wasSuccess()) {
                        Log.e(getSensor().getSerialNumber() + " Humid temp data notify failed! " + e.status());
                        return;
                    }

                    if (e.type() == BleDevice.ReadWriteListener.Type.ENABLING_NOTIFICATION) {
                        Log.i(getSensor().getSerialNumber() + " Enabled humid temp notify!");
                        return;
                    }

                    Log.i(getSensor().getSerialNumber() + " Got humid temp notify!");

                    try {
                        V1HumidTempMapper mapper = new V1HumidTempMapper();
                        HumidTempSample sample = mapper.fromBytes(e.data());

                        Log.i(getSensor().getSerialNumber() + " Humid Temp: " + sample);
                        List<HumidTempSample> samples = new ArrayList<>();
                        samples.add(sample);

                        if (mListener != null) {
                            mListener.didGetHumidTempSamples(samples);
                        }
                    } catch (MapperException ex) {
                        Log.e(getSensor().getSerialNumber() + " Failed to map humid temp! " + e);
                    }
                });

        mSensor.getBleDevice().enableNotify(mDefinitions.getDataService(),
                mDefinitions.getImpactDataCharacteristic(), e -> {
                    if (!e.wasSuccess()) {
                        Log.e(getSensor().getSerialNumber() + " Impact data notify failed! " + e.status());
                        return;
                    }

                    if (e.type() == BleDevice.ReadWriteListener.Type.ENABLING_NOTIFICATION) {
                        Log.i(getSensor().getSerialNumber() + " Enabled impact notify!");
                        return;
                    }

                    Log.i(getSensor().getSerialNumber() + " Got impact notify!");

                    try {
                        V1ImpactMapper mapper = new V1ImpactMapper();
                        ImpactSample sample = mapper.fromBytes(e.data());

                        Log.i(getSensor().getSerialNumber() + " Impact: " + sample);
                        List<ImpactSample> samples = new ArrayList<>();
                        samples.add(sample);

                        if (mListener != null) {
                            mListener.didGetImpactSamples(samples);
                        }
                    } catch (MapperException ex) {
                        Log.e(getSensor().getSerialNumber() + " Failed to map impact! " + e);
                    }
                });

        mSensor.getBleDevice().enableNotify(mDefinitions.getDataService(),
                mDefinitions.getMotionDataCharacteristic(), e -> {
                    if (!e.wasSuccess()) {
                        Log.e(getSensor().getSerialNumber() + " Motion data notify failed! " + e.status());
                        return;
                    }

                    if (e.type() == BleDevice.ReadWriteListener.Type.ENABLING_NOTIFICATION) {
                        Log.i(getSensor().getSerialNumber() + " Enabled motion notify!");
                        return;
                    }

                    Log.i(getSensor().getSerialNumber() + " Got motion notify!");

                    try {
                        V1MotionMapper mapper = new V1MotionMapper();
                        MotionSample sample = mapper.fromBytes(e.data());

                        Log.i(getSensor().getSerialNumber() + " Motion: " + sample);
                        List<MotionSample> samples = new ArrayList<>();
                        samples.add(sample);

                        if (mListener != null) {
                            mListener.didGetMotionSamples(samples);
                        }
                    } catch (MapperException ex) {
                        Log.e(getSensor().getSerialNumber() + " Failed to map motion! " + e);
                    }
                });

        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    public Listener getListener() {
        return mListener;
    }

    @Override
    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public Sensor getSensor() {
        return mSensor;
    }
}
