package io.blustream.sulley.routines.transactions.subtransactions;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import io.blustream.sulley.otau.database.PsKeyDatabaseHelper;
import io.blustream.sulley.otau.model.PsKeyDatabase;
import io.blustream.sulley.otau.model.PsKeysDetails;

public abstract class OTAUPropertySubtransaction extends WriteNotifyResponseSubtransaction {
    private static final int REQUEST_DATA_LENGTH = 4;

    @NonNull
    private final PsKeyDatabase mPsKeyDatabase;

    public OTAUPropertySubtransaction(@NonNull BleDefinitions definitions, @NonNull PsKeyDatabase psKeyDatabase) {
        super(definitions);
        mPsKeyDatabase = psKeyDatabase;
    }

    public abstract Integer getBuildId();
    public abstract Integer getPropertyId();

    @Override
    public byte[] getWriteValue() {
        if (getBuildId() == null) {
            return null;
        }

        PsKeyDatabaseHelper helper = new PsKeyDatabaseHelper(mPsKeyDatabase);
        try {
            helper.prepareForBuildId(getBuildId());
        }
        catch (IllegalStateException e) {
            return null;
        }

        PsKeysDetails psKeysDetails = helper.findPsKeysDetailsById(getPropertyId());
        if (psKeysDetails == null) {
            return null;
        }

        return createWriteByteArray(psKeysDetails.getOffset(), psKeysDetails.getLength());
    }

    private byte[] createWriteByteArray(int offset, int length) {
        byte[] offsetBytes = ByteBuffer.allocate(REQUEST_DATA_LENGTH).order(ByteOrder.LITTLE_ENDIAN).putInt(offset).array();
        byte[] lengthBytes = ByteBuffer.allocate(REQUEST_DATA_LENGTH).order(ByteOrder.LITTLE_ENDIAN).putInt(length * 2).array();
        byte[] bytes = new byte[REQUEST_DATA_LENGTH];
        System.arraycopy(offsetBytes, 0, bytes, 0, REQUEST_DATA_LENGTH / 2);
        System.arraycopy(lengthBytes, 0, bytes, REQUEST_DATA_LENGTH / 2, REQUEST_DATA_LENGTH / 2);

        return bytes;
    }
}
