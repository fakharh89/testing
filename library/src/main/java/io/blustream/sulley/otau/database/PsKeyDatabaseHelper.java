package io.blustream.sulley.otau.database;

import androidx.annotation.NonNull;

import io.blustream.sulley.otau.model.PsKey;
import io.blustream.sulley.otau.model.PsKeyDatabase;
import io.blustream.sulley.otau.model.PsKeysDetails;

public class PsKeyDatabaseHelper {
    @NonNull
    private final PsKeyDatabase mPsKeyDatabase;
    private PsKey mPsKey;

    public PsKeyDatabaseHelper(@NonNull PsKeyDatabase psKeyDatabase) {
        mPsKeyDatabase = psKeyDatabase;
    }

    public void prepareForBuildId(int buildId) throws IllegalStateException {
        mPsKey = findPsKeyByBuildId(buildId);
        if (mPsKey == null) {
            throw new IllegalStateException("PSKEY for id" + buildId + " is not found");
        }
    }

    private PsKey findPsKeyByBuildId(int buildId) {
        for (PsKey psKey : mPsKeyDatabase.getPsKeys()) {
            if (psKey.getBuildId() == buildId) {
                return psKey;
            }
        }

        return null;
    }

    public PsKeysDetails findPsKeysDetailsById(int id) {
        for (PsKeysDetails psKeysDetails : mPsKey.getDetails()) {
            if (psKeysDetails.getId() == id) {
                return psKeysDetails;
            }
        }

        return null;
    }
}
