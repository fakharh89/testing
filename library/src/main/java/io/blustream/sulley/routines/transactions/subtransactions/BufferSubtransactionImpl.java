package io.blustream.sulley.routines.transactions.subtransactions;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.utils.Utils_Byte;

import java.util.List;

import io.blustream.logger.Log;
import io.blustream.sulley.mappers.BufferMapper;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.models.Sample;

public class BufferSubtransactionImpl<T extends Sample> extends AbstractSubtransaction implements BufferSubtransaction<T> {
    public static final short DEFAULT_MAX_SAMPLES = 24;
    private static final String MAPPER_SUFFIX = "Mapper";
    private final BleDefinitions mDefinitions;
    private final short mMaxSamples;
    private final BufferMapper<T> mBufferMapper;
    private final String mSampleType;
    private final boolean mSucceedOnDisconnectAfterDelete;
    private List<T> mResults;
    private Listener<T> mListener;

    public BufferSubtransactionImpl(@NonNull BleDefinitions definitions,
                                    BufferMapper<T> bufferMapper,
                                    boolean succeedOnDisconnectAfterDelete) {
        this(definitions, bufferMapper, succeedOnDisconnectAfterDelete, DEFAULT_MAX_SAMPLES);
    }

    public BufferSubtransactionImpl(@NonNull BleDefinitions definitions,
                                    BufferMapper<T> bufferMapper,
                                    boolean succeedOnDisconnectAfterDelete,
                                    short maxSamples) {
        mDefinitions = definitions;
        mBufferMapper = bufferMapper;
        mSucceedOnDisconnectAfterDelete = succeedOnDisconnectAfterDelete;
        mMaxSamples = maxSamples;

        mSampleType = getSampleType();
    }

    @Override
    protected boolean startFirstAction() {
        readQuantityAvailable();
        return true;
    }

    private String getSampleType() {
        Class cls = getBufferMapper().getSubsampleByteMapper().getClass();
        String longClassName = cls.getName();
        String className = longClassName.substring(longClassName.lastIndexOf(".") + 1);
        if (className.endsWith(MAPPER_SUFFIX)) {
            className = className.substring(0, className.length() - MAPPER_SUFFIX.length());
        }
        return className;
    }

    private void readQuantityAvailable() {
        BleDevice.ReadWriteListener listener = readWriteEvent -> {
            if (readWriteEvent.wasSuccess()) {
                short samplesAvailable = readWriteEvent.data_short(true);
                Log.i(getDeviceName() + " " + samplesAvailable + " " + mSampleType + " samples available");
                if (samplesAvailable == 0) {
                    if (mListener != null) {
                        mListener.clearedBuffer();
                    }
                    succeed(false);
                } else {
                    prepareBuffer(samplesAvailable);
                }
            } else if (failedBecauseSensorBuffersAreEmpty(readWriteEvent)) {
                if (mListener != null) {
                    mListener.clearedBuffer();
                }
                succeed(true);
            } else {
                Log.e(getDeviceName() + " Failed to get size of " + mSampleType + " buffer");
                fail();
            }
        };

        getDevice().read(mDefinitions.getBufferService(), mDefinitions.getBufferSizeCharacteristic(),
                listener);
    }

    private void prepareBuffer(short samplesAvailable) {
        short samplesToPrepare = getQuantityToRead(samplesAvailable);

        Log.i(getDeviceName() + " Preparing " + samplesToPrepare + " " + mSampleType + " samples.");

        byte[] bytes = Utils_Byte.shortToBytes(samplesToPrepare);
        Utils_Byte.reverseBytes(bytes);

        BleDevice.ReadWriteListener listener = readWriteEvent -> {
            if (readWriteEvent.wasSuccess()) {
                Log.i(getDeviceName() + " Prepared " + samplesToPrepare + " " + mSampleType + " samples.");
                readBuffer();
            } else if (failedBecauseSensorBuffersAreEmpty(readWriteEvent)) {
                if (mListener != null) {
                    mListener.clearedBuffer();
                }
                succeed(true);
            } else {
                Log.e(getDeviceName() + " Failed to prepare " + mSampleType + " buffer.");
                fail();
            }
        };

        getDevice().write(mDefinitions.getBufferService(), mDefinitions.getBufferCharacteristic(),
                bytes, listener);
    }

    private void readBuffer() {
        BleDevice.ReadWriteListener listener = readWriteEvent -> {
            if (readWriteEvent.wasSuccess()) {
                Log.i(getDeviceName() + " Read " + mSampleType + " buffer!");

                try {
                    mResults = getBufferMapper().fromBytes(readWriteEvent.data());
                } catch (MapperException e) {
                    Log.e(getDeviceName() + " Failed to parse " + mSampleType + " samples! " + e);
                    fail();
                    return;
                }

                Log.i(getDeviceName() + " Results: " + mResults.toString());
                int rawDataSize = readWriteEvent.data().length / getBufferMapper().getSubsampleByteMapper().expectedReadLength();
                if (rawDataSize != mResults.size()) {
                    Log.e(getDeviceName() + " Failed to parse " + (mResults.size() - rawDataSize) + " " + mSampleType + " samples!");
                }
                deleteSamples(rawDataSize);
            } else if (failedBecauseSensorBuffersAreEmpty(readWriteEvent)) {
                if (mListener != null) {
                    mListener.clearedBuffer();
                }
                succeed(true);
            } else {
                Log.e(getDeviceName() + " Failed to read " + mSampleType + " buffer!");
                fail();
            }
        };

        getDevice().read(mDefinitions.getBufferService(), mDefinitions.getBufferCharacteristic(),
                listener);
    }

    private void deleteSamples(int count) {
        Log.i(getDeviceName() + " Deleting " + count + " " + mSampleType + " samples.");

        byte[] bytes = Utils_Byte.intToBytes(count);
        Utils_Byte.reverseBytes(bytes);

        BleDevice.ReadWriteListener listener = readWriteEvent -> {
            if (readWriteEvent.wasSuccess()) {
                Log.i(getDeviceName() + " Deleted " + count + " " + mSampleType + " samples.");
                if (mListener != null) {
                    mListener.didGetData(mResults);
                    mResults = null;
                }
                readQuantityAvailable();
            } else if (failedBecauseSensorBuffersAreEmpty(readWriteEvent)) {
                Log.i(getDeviceName() + " Deleted " + count + " " + mSampleType + " samples.");
                if (mListener != null) {
                    mListener.didGetData(mResults);
                    mListener.clearedBuffer();
                }
                succeed(true);
            } else {
                Log.e(getDeviceName() + " Failed to delete " + mSampleType + " samples.");
                fail();
            }
        };

        getDevice().write(mDefinitions.getBufferService(), mDefinitions.getBufferSizeCharacteristic(),
                bytes, listener);
    }

    private short getQuantityToRead(short samplesAvailable) {
        if (samplesAvailable > getMaxSamples()) {
            return getMaxSamples();
        }

        return samplesAvailable;
    }

    @Override
    public Listener<T> getListener() {
        return mListener;
    }

    @Override
    public void setListener(Listener<T> listener) {
        mListener = listener;
    }

    @Override
    public BleDefinitions getDefinitions() {
        return mDefinitions;
    }

    @Override
    public short getMaxSamples() {
        return mMaxSamples;
    }

    @Override
    public boolean succeedOnDisconnectAfterDelete() {
        return mSucceedOnDisconnectAfterDelete;
    }

    @NonNull
    @Override
    public BufferMapper<T> getBufferMapper() {
        return mBufferMapper;
    }

    private boolean failedBecauseSensorBuffersAreEmpty(BleDevice.ReadWriteListener.ReadWriteEvent readWriteEvent) {
        boolean wasCancelled = readWriteEvent.wasCancelled();
        boolean forceKilled = readWriteEvent.status() == BleDevice.ReadWriteListener.Status.REMOTE_GATT_FAILURE
                && readWriteEvent.gattStatus() == 133;

        return succeedOnDisconnectAfterDelete() && (wasCancelled || forceKilled);
    }
}
