package io.blustream.sulley.otau.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.blustream.logger.Log;
import io.blustream.sulley.otau.exceptions.OTAUManagerException;
import io.blustream.sulley.otau.model.FirmwareProperties;
import io.blustream.sulley.utilities.ImageEditor;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;


public class OTAUFirmwareImageHelper {

    public static void initFirmwareImage(@NonNull Context context, @NonNull FirmwareProperties properties, @NonNull File imageFile, @NonNull FirmwareImageHelperListener listener) {
        FileInputStream inputStream;

        try {
            inputStream = new FileInputStream(imageFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            listener.onImageError(new OTAUManagerException("Image File not found!", e));
            return;
        }

        ImageEditor imageEditorOrigin = new ImageEditor(context, new BufferedInputStream(inputStream), properties);

        final boolean b = imageEditorOrigin.startDownloading(new Observer<byte[]>() {
            @Override
            public void onSubscribe(Disposable d) {
                android.util.Log.d(ImageEditor.TAG, "onSubscribe");
            }

            @Override
            public void onNext(byte[] bytes) {
                listener.onImageReady(bytes);
            }

            @Override
            public void onError(Throwable e) {
                listener.onImageError(new OTAUManagerException("Failed to parse image file!", e));
            }

            @Override
            public void onComplete() {
                Log.d("Parsing image file complete!");
            }
        });
    }

    public static void cacheFirmwareProperties(Context context, FirmwareProperties properties) {
        SharedPreferences.Editor editor = context.getSharedPreferences("sensor_upgrade_cache", Context.MODE_PRIVATE).edit();
        editor.putString(properties.getMacAddress(), properties.toString());
        editor.apply();
    }

    public static FirmwareProperties getCachedFirmwareProperties(Context context, String macAddress) {
        SharedPreferences prefs = context.getSharedPreferences("sensor_upgrade_cache", Context.MODE_PRIVATE);
        if (prefs.contains(macAddress)) {
            return FirmwareProperties.fromString(prefs.getString(macAddress, null));
        }
        return null;
    }

    public static void deleteFirmwarePropertiesCache(Context context, FirmwareProperties properties) {
        SharedPreferences.Editor editor = context.getSharedPreferences("sensor_upgrade_cache", Context.MODE_PRIVATE).edit();
        editor.remove(properties.getMacAddress());
        editor.apply();

    }

    public interface FirmwareImageHelperListener {
        void onImageReady(byte[] image);
        void onImageError(OTAUManagerException e);
    }

    private static int getOriginalImgId(Context context, File file) {
        int id = context.getResources().getIdentifier(file.getName(),
                "raw", context.getPackageName());
        android.util.Log.d(ImageEditor.TAG, "getOriginalImgId = " + id);
        return id;
    }

    @Nullable
    public static File cacheStockFirmwareImage(@NonNull Context context, @NonNull String fileName, @NonNull InputStream inputStream) {
        File file;
        try {
            file = new File(context.getCacheDir(), fileName);
            try (OutputStream output = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024]; // or other buffer size
                int read;

                while ((read = inputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }

                output.flush();
            } catch (IOException e) {
                file = null;
            }
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
