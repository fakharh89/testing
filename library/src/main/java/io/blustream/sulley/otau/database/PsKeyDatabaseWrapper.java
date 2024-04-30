package io.blustream.sulley.otau.database;

import android.content.Context;

import io.blustream.sulley.otau.model.PsKey;
import io.blustream.sulley.otau.model.PsKeyDatabase;
import io.blustream.sulley.otau.model.PsKeysDetails;

public class PsKeyDatabaseWrapper {

    private PsKeyDatabaseLoader databaseLoader;
    private PsKeyDatabase psKeyDatabase;
    private PsKey psKey;

    public PsKeyDatabaseWrapper(Context context) {
        this.databaseLoader = new PsKeyDatabaseLoader(context);
    }

    public void prepareForBuildId(int buildId) throws Exception {
        psKeyDatabase = databaseLoader.loadPsKeyDatabase();
        psKey = findPsKeyByBuildId(buildId);
        if (psKey == null) {
            throw new IllegalStateException("PSKEY for id" + buildId + " is not found");
        }
    }

    private PsKey findPsKeyByBuildId(int buildId) {
        for (PsKey psKey : psKeyDatabase.getPsKeys()) {
            if (psKey.getBuildId() == buildId) {
                return psKey;
            }
        }

        return null;
    }

    public PsKeysDetails findPsKeysDetailsById(int id) {
        for (PsKeysDetails psKeysDetails : psKey.getDetails()) {
            if (psKeysDetails.getId() == id) {
                return psKeysDetails;
            }
        }

        return null;
    }
}
