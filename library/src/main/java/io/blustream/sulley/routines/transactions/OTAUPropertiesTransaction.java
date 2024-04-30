package io.blustream.sulley.routines.transactions;

import androidx.annotation.NonNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.blustream.logger.Log;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.otau.model.FirmwareProperties;
import io.blustream.sulley.otau.model.PsKeyDatabase;
import io.blustream.sulley.routines.OTAUBleDefinitions;
import io.blustream.sulley.routines.transactions.subtransactions.BuildIdSubtransaction;
import io.blustream.sulley.routines.transactions.subtransactions.CrystalTrimSubtransaction;
import io.blustream.sulley.routines.transactions.subtransactions.MacAddressSubtransaction;
import io.blustream.sulley.routines.transactions.subtransactions.Subtransaction;
import io.blustream.sulley.routines.transactions.subtransactions.UserKeySubtransaction;
import io.blustream.sulley.routines.transactions.subtransactions.WriteNotifyResponseSubtransaction;

public class OTAUPropertiesTransaction extends QueuedTransaction {
    private FirmwareProperties mFirmwareProperties;
    private BuildIdSubtransaction mBuildIdTransaction;
    private MacAddressSubtransaction mMacAddressTransaction;
    private CrystalTrimSubtransaction mCrystalTrimTransaction;
    private UserKeySubtransaction mUserKeyTransaction;
    private Integer mBuildId;

    public OTAUPropertiesTransaction(@NonNull OTAUBleDefinitions definitions, @NonNull PsKeyDatabase psKeyDatabase) {
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

        mCrystalTrimTransaction = new CrystalTrimSubtransaction(definitionsForwarder, psKeyDatabase) {
            @Override
            public Integer getBuildId() {
                return mBuildId;
            }
        };
        subtransactions.add(mCrystalTrimTransaction);

        mUserKeyTransaction = new UserKeySubtransaction(definitionsForwarder, psKeyDatabase) {
            @Override
            public Integer getBuildId() {
                return mBuildId;
            }
        };
        subtransactions.add(mUserKeyTransaction);

        setSubtransactionQueue(subtransactions);
    }

    public FirmwareProperties getFirmwareProperties() {
        return mFirmwareProperties;
    }

    private static WriteNotifyResponseSubtransaction.BleDefinitions GetDefinitionsForwarder(OTAUBleDefinitions definitions) {
        return new WriteNotifyResponseSubtransaction.BleDefinitions() {
            @NonNull
            @Override
            public UUID getService() {
                return definitions.getApplicationService();
            }

            @NonNull
            @Override
            public UUID getWriteCharacteristic() {
                return definitions.getKeyBlockCharacteristic();
            }

            @NonNull
            @Override
            public UUID getNotifyResponseCharacteristic() {
                return definitions.getDataTransferCharacteristic();
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

        super.ended(subtransaction, successOverride, cancelPending);
    }
}
