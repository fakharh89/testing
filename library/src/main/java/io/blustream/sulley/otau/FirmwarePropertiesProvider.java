package io.blustream.sulley.otau;

import android.content.Context;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.utils.Interval;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import io.blustream.sulley.otau.database.PsKeyDatabaseWrapper;
import io.blustream.sulley.otau.model.FirmwareProperties;
import io.blustream.sulley.otau.model.PsKeysDetails;
import io.blustream.sulley.utilities.ByteUtils;
import io.blustream.sulley.utilities.Bytes;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;


import static io.blustream.sulley.utilities.Uuids.Otau.Application.APPLICATION_OTAU_SERVICE;
import static io.blustream.sulley.utilities.Uuids.Otau.Application.DATA_TRANSFER_CHARACTERISTIC;
import static io.blustream.sulley.utilities.Uuids.Otau.Application.KEY_BLOCK_CHARACTERISTIC;
import static io.blustream.sulley.utilities.Uuids.Otau.Application.OTAU_VERSION_CHARACTERISTIC;

@Deprecated
// Use OTAUPropertiesTransaction instead
public class FirmwarePropertiesProvider {

    private static final int REQUEST_DATA_LENGTH = 4;

    private static final int BUILD_ID_REQUEST = 0x20000;
    private static final int MAC_ADDRESS_ID = 1;
    private static final int CRYSTAL_TRIM_ID = 2;
    private static final int USER_KEYS_ID = 4;

    private static final int DEFAULT_OTAU_VERSION = 0x06; // 6
    private static final int DEFAULT_BUILD_ID = 0x4e; // 78
    private static final int EXPECTED_CRYSTAL_TRIM = 0x20; // 32
    private static final String EXPECTED_USER_KEYS = "29:D1:E1:6B:2E:70:4B:5B:A9:B8:5F:A5:C4:83:5A:95";

    private static final Interval pollInterval = Interval.secs(1);

    private BleDevice device;
    private PsKeyDatabaseWrapper psKeyDatabaseWrapper;
    private FirmwareProperties properties;

    public FirmwarePropertiesProvider(BleDevice device, Context context) {
        this.device = device;
        this.psKeyDatabaseWrapper = new PsKeyDatabaseWrapper(context);
        this.properties = new FirmwareProperties();
    }

    public void getProperties(Listener listener) {
        checkOtauVersion()
                .andThen(Single.defer(this::requestBuildId))
                .flatMap((Function<Bytes, SingleSource<Integer>>) this::checkBuildId)
                .flatMapCompletable(this::prepareDatabase)
                .andThen(Single.defer(() -> readFirmwareProperty(MAC_ADDRESS_ID)))
                .flatMapCompletable(this::checkMacAddress)
                .andThen(Single.defer(() -> readFirmwareProperty(CRYSTAL_TRIM_ID)))
                .flatMapCompletable(this::checkCrystalTrim)
                .andThen(Single.defer(() -> readFirmwareProperty(USER_KEYS_ID)))
                .flatMapCompletable(this::checkUserKeys)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                    }

                    @Override
                    public void onComplete() {
                        listener.onPropertiesReady(properties);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        listener.onFailure(throwable);
                    }
                });
    }

    private Completable checkOtauVersion() {
        return Completable.create(emitter -> device.read(APPLICATION_OTAU_SERVICE, OTAU_VERSION_CHARACTERISTIC, event -> {
            if (event.wasSuccess() && event.data().length > 0) {
                int version = event.data()[0];
                if (version == DEFAULT_OTAU_VERSION) {
                    emitter.onComplete();
                } else {
                    emitter.onError(new IllegalStateException("OTAU version is invalid"));
                }
            } else {
                emitter.onError(new IllegalStateException("Failed to read OTAU version"));
            }
        }));
    }

    private Single<Bytes> requestBuildId() {
        byte[] bytes = ByteBuffer.allocate(REQUEST_DATA_LENGTH)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(BUILD_ID_REQUEST)
                .array();

        return readProperty(bytes);
    }

    private Single<Integer> checkBuildId(Bytes buildIdBytes) {
        return Single.create(emitter -> {
            try {
                int buildId = buildIdBytes.getBytes()[0];
                if (buildId == DEFAULT_BUILD_ID) {
                    emitter.onSuccess(buildId);
                } else {
                    emitter.onError(new IllegalStateException("Build id is invalid"));
                }
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    private Completable prepareDatabase(int buildId) {
        return Completable.create(emitter -> {
            try {
                psKeyDatabaseWrapper.prepareForBuildId(buildId);
            } catch (Exception e) {
                emitter.onError(e);
            }
            emitter.onComplete();
        });
    }

    private Completable checkMacAddress(Bytes macAddressBytes) {
        return Completable.create(emitter -> {
            String macAddress = ByteUtils.byteArrayToHexString(macAddressBytes.getBytes(), ByteOrder.LITTLE_ENDIAN);
            if (macAddress.equals(device.getMacAddress())) {
                properties.setMacAddress(macAddress);
                emitter.onComplete();
            } else {
                emitter.onError(new IllegalStateException("Mac Address is invalid"));
            }
        });
    }

    private Completable checkCrystalTrim(Bytes crystalTrimBytes) {
        return Completable.create(emitter -> {
            int crystalTrim = crystalTrimBytes.getBytes()[0];
            if (crystalTrim == EXPECTED_CRYSTAL_TRIM) {
                properties.setCrystalTrim(crystalTrim);
                emitter.onComplete();
            } else {
                emitter.onError(new IllegalStateException("Crystal Trim is invalid"));
            }
        });
    }

    private Completable checkUserKeys(Bytes userKeysBytes) {
        return Completable.create(emitter -> {
            String userKeys = ByteUtils.byteArrayToHexString(userKeysBytes.getBytes(), ByteOrder.LITTLE_ENDIAN);
            if (userKeys.equals(EXPECTED_USER_KEYS)) {
                properties.setUserKeys(userKeys);
                emitter.onComplete();
            } else {
                emitter.onError(new IllegalStateException("User Keys are invalid"));
            }
        });
    }

    private Single<Bytes> readFirmwareProperty(int id) {
        PsKeysDetails psKeysDetails = psKeyDatabaseWrapper.findPsKeysDetailsById(id);
        byte[] bytes = createWriteByteArray(psKeysDetails.getOffset(), psKeysDetails.getLength());
        return readProperty(bytes);
    }

    private byte[] createWriteByteArray(int offset, int length) {
        byte[] offsetBytes = ByteBuffer.allocate(REQUEST_DATA_LENGTH).order(ByteOrder.LITTLE_ENDIAN).putInt(offset).array();
        byte[] lengthBytes = ByteBuffer.allocate(REQUEST_DATA_LENGTH).order(ByteOrder.LITTLE_ENDIAN).putInt(length * 2).array();
        byte[] bytes = new byte[REQUEST_DATA_LENGTH];
        System.arraycopy(offsetBytes, 0, bytes, 0, REQUEST_DATA_LENGTH / 2);
        System.arraycopy(lengthBytes, 0, bytes, REQUEST_DATA_LENGTH / 2, REQUEST_DATA_LENGTH / 2);

        return bytes;
    }

    private Single<Bytes> readProperty(byte[] bytesToWrite) {
        return Single.create(emitter -> {
            device.startPoll(APPLICATION_OTAU_SERVICE, DATA_TRANSFER_CHARACTERISTIC, pollInterval, event -> {
                if (event.wasSuccess() && event.data().length > 0) {
                    device.stopPoll(APPLICATION_OTAU_SERVICE, DATA_TRANSFER_CHARACTERISTIC);
                    emitter.onSuccess(new Bytes(event.data()));
                }
            });

            device.write(APPLICATION_OTAU_SERVICE, KEY_BLOCK_CHARACTERISTIC, () -> bytesToWrite, event -> {
                if (!event.wasSuccess()) {
                    emitter.onError(new IllegalStateException(event.toString()));
                }
            });
        });
    }

    public interface Listener {

        void onPropertiesReady(FirmwareProperties properties);

        void onFailure(Throwable throwable);
    }
}
