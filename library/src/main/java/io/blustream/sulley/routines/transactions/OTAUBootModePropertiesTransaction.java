package io.blustream.sulley.routines.transactions;

import androidx.annotation.NonNull;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.blustream.logger.Log;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.otau.model.FirmwareProperties;
import io.blustream.sulley.otau.model.PsKeyDatabase;
import io.blustream.sulley.routines.BleCharacteristic;
import io.blustream.sulley.routines.BleService;
import io.blustream.sulley.routines.OTAUBootModeBleDefinitions;
import io.blustream.sulley.routines.transactions.subtransactions.BootModeOTAUVersionSubTransaction;
import io.blustream.sulley.routines.transactions.subtransactions.BuildIdSubtransaction;
import io.blustream.sulley.routines.transactions.subtransactions.CrystalTrimSubtransaction;
import io.blustream.sulley.routines.transactions.subtransactions.MacAddressSubtransaction;
import io.blustream.sulley.routines.transactions.subtransactions.Subtransaction;
import io.blustream.sulley.routines.transactions.subtransactions.UserKeySubtransaction;
import io.blustream.sulley.routines.transactions.subtransactions.WriteNotifyResponseSubtransaction;

public class OTAUBootModePropertiesTransaction extends QueuedTransaction {
    private FirmwareProperties mFirmwareProperties;
    private BuildIdSubtransaction mBuildIdTransaction;
    private MacAddressSubtransaction mMacAddressTransaction;
    private CrystalTrimSubtransaction mCrystalTrimTransaction;
    private UserKeySubtransaction mUserKeyTransaction;
    private BootModeOTAUVersionSubTransaction mBootModeOtauVersionSubtransaction;
    private Integer mBuildId;

    public OTAUBootModePropertiesTransaction(@NonNull OTAUBootModeBleDefinitions definitions, @NonNull PsKeyDatabase psKeyDatabase) {
        super();

        WriteNotifyResponseSubtransaction.BleDefinitions definitionsForwarder = GetDefinitionsForwarder(definitions);

        ConcurrentLinkedQueue<Subtransaction> subtransactions = new ConcurrentLinkedQueue<>();

        mBuildIdTransaction = new BuildIdSubtransaction(definitionsForwarder);
        subtransactions.add(mBuildIdTransaction);

        mMacAddressTransaction = new MacAddressSubtransaction(definitionsForwarder, psKeyDatabase) {
            @Override
            public Integer getBuildId() {
                return mBuildId;
            }
        };
        subtransactions.add(mMacAddressTransaction);

        setSubtransactionQueue(subtransactions);
    }

    public interface BleDefinitions {
        @NonNull
        @BleService
        UUID getBootModeOTAUService();

        @NonNull
        @BleCharacteristic
        UUID getBootModeKeyBlockCharacteristic();

        @NonNull
        @BleCharacteristic
        UUID getBootModeDataTransferCharacteristic();
    }

    public FirmwareProperties getFirmwareProperties() {
        return mFirmwareProperties;
    }

    private static WriteNotifyResponseSubtransaction.BleDefinitions GetDefinitionsForwarder(OTAUBootModeBleDefinitions definitions) {
        return new WriteNotifyResponseSubtransaction.BleDefinitions() {
            @NonNull
            @Override
            public UUID getService() {
                return definitions.getBootModeOTAUPropertiesService();
            }

            @NonNull
            @Override
            public UUID getWriteCharacteristic() {
                return definitions.getBootModeOTAUPropertiesKeyBlockCharacteristic();
            }

            @NonNull
            @Override
            public UUID getNotifyResponseCharacteristic() {
                return definitions.getBootModeOTAUPropertiesDataTransferCharacteristic();
            }
        };
    }

    @Override
    public void ended(Subtransaction subtransaction, boolean success, boolean cancelPending) {
        if (mFirmwareProperties == null) {
            mFirmwareProperties = new FirmwareProperties();
        }

        boolean successOverride = success;

        if (subtransaction == mBuildIdTransaction) {
            try {
                mBuildId = mBuildIdTransaction.getBuildId();
            } catch (MapperException e) {
                Log.e(getDevice().getName_override() + " Could not get build id!", e);
                successOverride = false;
            }
        }
        else if (subtransaction == mMacAddressTransaction) {
            try {
                mFirmwareProperties.setMacAddress(mMacAddressTransaction.getMacAddress());
            } catch (MapperException e) {
                Log.e(getDevice().getName_override() + " Could not get mac address!", e);
                successOverride = false;
            }
        }
        else if (subtransaction == mCrystalTrimTransaction) {
            try {
                mFirmwareProperties.setCrystalTrim(mCrystalTrimTransaction.getCrystalTrim());
            } catch (MapperException e) {
                Log.e(getDevice().getName_override() + " Could not get crystal trim!", e);
                successOverride = false;
            }
        }
        else if (subtransaction == mUserKeyTransaction) {
            try {
                mFirmwareProperties.setUserKeys(mUserKeyTransaction.getUserKeys());
            } catch (MapperException e) {
                Log.e(getDevice().getName_override() + " Could not get user keys!", e);
                successOverride = false;
            }
        }
        else if (subtransaction == mBootModeOtauVersionSubtransaction) {
            try {
                Log.d(String.format(Locale.getDefault(), "OTAU Version = %s", mBootModeOtauVersionSubtransaction.getOtauVersion()));
            } catch (Exception e) {
                Log.e(getDevice().getName_override() + " Could not get OTAU version!", e);
                successOverride = false;
            }
        }

        super.ended(subtransaction, successOverride, cancelPending);
    }
}
