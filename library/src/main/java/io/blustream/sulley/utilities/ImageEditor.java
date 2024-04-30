package io.blustream.sulley.utilities;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.blustream.sulley.otau.model.FirmwareProperties;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;


/**
 * Created by Ruzhitskii Sviatoslav on 11/1/19.
 */
public class ImageEditor {

    public static final String TAG = "ImageEditorTAG";
    public static final String NULL_STREAM_TEXT = "Input stream is null";
    public static final String NULL_MAC_TEXT = "Mac address is null";
    public static final String INVALID_MAC_TEXT = "Invalid Mac address format";
    public static final String NULL_CONTEXT_TEXT = "Context is null";
    public static final String NULL_PROPERTIES_TEXT = "Properties variable is null";
    private static final String MAC_PATTERN = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
    private Context context;
    private BufferedInputStream bufferedInputStream;
    private FirmwareProperties properties;
    private PublishSubject<byte[]> byteArrayReady = PublishSubject.create();
    private byte[] mergedData;
    private Runnable updateImageRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "doInBackground.bufferedInputStream = " + bufferedInputStream);
            boolean result;
            try {
                int size = bufferedInputStream.available();
                byte[] originData = new byte[size];
                bufferedInputStream.read(originData, 0, originData.length);
                mergedData = CsConfig.mergeKeys(originData, properties.getMacAddress());
                bufferedInputStream.close();
                byteArrayReady.onNext(mergedData);
            } catch (Exception e) {
                e.printStackTrace();
                byteArrayReady.onError(e);
            }
        }
    };

    public ImageEditor(@NonNull Context context, @NonNull BufferedInputStream bufferedInputStream, @NonNull FirmwareProperties properties) {
        this.context = context;
        this.bufferedInputStream = bufferedInputStream;
        this.properties = properties;
    }

    public byte[] getMergedData() {
        return mergedData;
    }

    private boolean checkArguments() {

        if (context == null) {
            byteArrayReady.onError(new IllegalArgumentException(NULL_CONTEXT_TEXT));
            return false;
        }
        if (bufferedInputStream == null) {
            byteArrayReady.onError(new IllegalArgumentException(NULL_STREAM_TEXT));
            return false;
        }
        if (properties == null) {
            byteArrayReady.onError(new IllegalArgumentException(NULL_PROPERTIES_TEXT));
            return false;
        }
        if (!checkMac(properties.getMacAddress())) {
            return false;
        }

        return true;
    }

    public boolean startDownloading(Observer<byte[]> observer) {
        if (observer == null) {
            return false;
        }
        byteArrayReady.subscribe(observer);
        if (!checkArguments()) {
            return false;
        }
        Scheduler scheduler = Schedulers.single();
        Scheduler.Worker worker = scheduler.createWorker();
        worker.schedule(updateImageRunnable);
        return true;
    }

    private boolean checkMac(String mac) {
        if (mac == null) {
            byteArrayReady.onError(new IllegalArgumentException(NULL_MAC_TEXT));
            return false;
        }
        Pattern pattern = Pattern.compile(MAC_PATTERN);
        Matcher matcher = pattern.matcher(mac);
        boolean isValid = matcher.matches();
        if (!isValid) {
            byteArrayReady.onError(new IllegalArgumentException(INVALID_MAC_TEXT));
        }
        return isValid;
    }
}
