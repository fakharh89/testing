package io.blustream.sulley.routines.transactions;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleConnectionPriority;
import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleTransaction;
import io.blustream.sulley.routines.OTAUBootModeBleDefinitions;


public class BootModeWriteFirmwareImageTransaction extends BleTransaction.Ota {

    private final static int DATA_LENGTH = 20;

    private boolean simulateFailure;
    private BleDevice device = null;
    private OTAUBootModeBleDefinitions mDefinitions;
    private byte[] imageToWrite;
    private int totalLength;
    private int currentIndex = 0;
    private int lengthToWrite = DATA_LENGTH;
    private boolean isCanceled = false;

    private Listener publicListener;
    private FastModeListener fastModeListener = new FastModeListener() {
        @Override
        public void fastModeSuccess(boolean isEnabled) {
            if (isEnabled) {
                beginImageWriting();
            }
        }

        @Override
        public void fastModeError() {
            fail();

        }
    };

    public BootModeWriteFirmwareImageTransaction(@NonNull byte[] image, @NonNull OTAUBootModeBleDefinitions definitions, @NonNull Listener listener, boolean simulateFailure ) {
        imageToWrite = new byte[image.length];
        System.arraycopy(image, 0, imageToWrite, 0, image.length);
        totalLength = imageToWrite.length;
        publicListener = listener;
        mDefinitions = definitions;
        this.simulateFailure = simulateFailure;
    }

    @Override
    protected void start(BleDevice device) {
        this.device = device;
        setFastMode(true, fastModeListener);
        isCanceled = false;
    }

    private void beginImageWriting() {
        if (isCanceled) {
            cancel();
            return;
        }
        currentIndex = 0;

        final byte[] data = new byte[lengthToWrite];

        System.arraycopy(imageToWrite, currentIndex, data, 0, lengthToWrite);

        getDevice().write(mDefinitions.getBootModeWriteFirmwareService(), mDefinitions.getBootModeWriteFirmwareControlTransferCharacteristic(), new byte[]{(byte) 2}, prepWriteBufferEvent -> {
            if (prepWriteBufferEvent.wasSuccess()) {
                device.write(mDefinitions.getBootModeWriteFirmwareService(), mDefinitions.getBootModeWriteFirmwareDataTransferCharacteristic(), data, readWriteEvent -> {
                    if (!readWriteEvent.wasSuccess()) {
                        fail();
                        return;
                    }

                    currentIndex += lengthToWrite;
                    publicListener.onProgress((int)((float)currentIndex / (float)totalLength * 100));
                    writeNextData();
                });
                return;
            }

            fail();
        });
    }

    private void writeNextData() {
        if (isCanceled) {
            cancel();
            return;
        }

        if ((currentIndex + DATA_LENGTH) > totalLength) {
            lengthToWrite = totalLength - currentIndex;
        }

        final byte[] data = new byte[lengthToWrite];

        System.arraycopy(imageToWrite, currentIndex, data, 0, lengthToWrite);

        device.write(mDefinitions.getBootModeWriteFirmwareService(), mDefinitions.getBootModeWriteFirmwareDataTransferCharacteristic(), data, readWriteEvent -> {
            if (!readWriteEvent.wasSuccess()) {
                fail();
                return;
            }

            currentIndex += lengthToWrite;

            if (currentIndex >= totalLength) {
                // We're done, reboot sensor in application mode.
                setFastMode(false, new FastModeListener() {
                    @Override
                    public void fastModeSuccess(boolean isEnabled) {
                        getDevice().write(mDefinitions.getBootModeWriteFirmwareService(), mDefinitions.getBootModeWriteFirmwareControlTransferCharacteristic(), new byte[]{(byte) 4}, rebootWriteEvent -> {
                            if (rebootWriteEvent.wasSuccess()) {
                                succeed();
                                return;
                            }

                            fail();
                        });
                    }

                    @Override
                    public void fastModeError() {
                        fail();
                    }
                });

                return;
            }

            int currentProgress = (int)((float)currentIndex / (float)totalLength * 100);
            publicListener.onProgress(currentProgress);
            if (simulateFailure && currentProgress == 15) {
                fail();
                return;
            }
            writeNextData();
        });
    }

    public void cancelTransaction() {
        isCanceled = true;
    }

    private void setFastMode(boolean enabled, @NonNull FastModeListener listener) {
        BleConnectionPriority priority = enabled ? BleConnectionPriority.HIGH : BleConnectionPriority.MEDIUM;

        device.setConnectionPriority(priority, readWriteEvent -> {
            if (readWriteEvent.wasSuccess()) {
               listener.fastModeSuccess(enabled);
                return;
            }

            listener.fastModeError();
        });
    }

    /**
     * Default is {@link Boolean#FALSE}. Optionally override if you want your transaction's reads/writes to execute "atomically".
     * This means that if you're connected to multiple devices only the reads/writes of this transaction's device
     * will be executed until this transaction is finished.
     */
    @Override
    protected boolean needsAtomicity() {
        return true;
    }

    private interface FastModeListener {
        void fastModeSuccess(boolean isEnabled);
        void fastModeError();
    }

    public interface Listener {
        default void onProgress(int percentComplete) {
        }
    }

}