package io.blustream.sulley.otau.database;

import android.content.Context;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import io.blustream.sulley.otau.model.PsKeyDatabase;
import io.blustream.sulley.otau.model.PsKeyDatabaseObject;

public class PsKeyDatabaseLoader {

    private static final String PS_KEY_DATABASE_FOLDER = "raw";
    private static final String PS_KEY_DATABASE_NAME = "pskey_db";

    private Context context;
    private Gson gson;

    public PsKeyDatabaseLoader(Context context) {
        this.context = context;
        gson = new Gson();
    }

    public PsKeyDatabase loadPsKeyDatabase() throws IOException {
        String json = loadJsonFromAsset();
        return gson.fromJson(json, PsKeyDatabaseObject.class).getPsKeyDatabase();
    }

    private String loadJsonFromAsset() throws IOException {
        int identifier = context.getResources().getIdentifier(PS_KEY_DATABASE_NAME, PS_KEY_DATABASE_FOLDER, context.getPackageName());
        InputStream inputStream = context.getResources().openRawResource(identifier);
        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);
        inputStream.close();

        return new String(buffer, StandardCharsets.UTF_8);
    }
}
