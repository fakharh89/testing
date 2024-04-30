package com.blustream.demo;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import io.blustream.sulley.otau.model.FirmwareProperties;
import io.blustream.sulley.utilities.ImageEditor;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class ImageEditorInstrumentedTest {

    private static final String VALID_MAC = "0C:1A:10:00:02:09";
    private static final String INVALID_MAC = "0C:1A:10:00:02:01";
    private static final String WRONG_PATTERN_MAC = "0C:1A:10:00:02:0";
    private static final String EMPTY_MAC = "";
    private final Object pauseLock = new Object();

    @Rule
    public ActivityTestRule<TestDataIntegrityActivity> mActivityRule =
            new ActivityTestRule(TestDataIntegrityActivity.class);
    private File newFile;
    private Throwable outputError;
    private boolean isMyAfterEqualsiOSAfter;

    @Before
    public void init() {
        Log.d(ImageEditor.TAG, "init");
        newFile = null;
        outputError = null;
        isMyAfterEqualsiOSAfter = false;
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        new TestDataIntegrityActivity();
    }

    private Context getAppContext() {
        return ApplicationProvider.getApplicationContext();
    }

    @Test
    public void mergeValidMac() {
        mergeMac(VALID_MAC);
        assert outputError == null;
        assertThat(isMyAfterEqualsiOSAfter, is(true));
    }

    @Test
    public void mergeInValidMac() {
        mergeMac(INVALID_MAC);
        assert outputError == null;
        assertThat(isMyAfterEqualsiOSAfter, is(false));
    }

    @Test
    public void mergeWrongPatternMac() {
        mergeMac(WRONG_PATTERN_MAC);
        assert outputError != null;
        assertThat(outputError.getMessage(), is(ImageEditor.INVALID_MAC_TEXT));
        assertThat(isMyAfterEqualsiOSAfter, is(false));
    }

    @Test
    public void mergeNullMac() {
        mergeMac(null);
        assert outputError != null;
        assertThat(outputError.getMessage(), is(ImageEditor.NULL_MAC_TEXT));
    }

    @Test
    public void mergeEmptyMac() {
        mergeMac(EMPTY_MAC);
        assert outputError != null;
        assertThat(outputError.getMessage(), is(ImageEditor.INVALID_MAC_TEXT));
    }

    @After
    public void removeFile() {
        if (newFile != null && newFile.exists()) {
            Log.d(ImageEditor.TAG, "removeFile() " + newFile.delete());
        }
    }

    public void mergeMac(String mac) {
        InputStream humi409_OriginImgIs = getAppContext().getResources().openRawResource(getOriginalImgId());
        ImageEditor imageEditorOrigin = new ImageEditor(getAppContext(), new BufferedInputStream(humi409_OriginImgIs), new FirmwareProperties(mac));
        if (imageEditorOrigin.startDownloading(getObserver())) {
            waitForFinish();
        }
    }

    @Test
    public void mergeNullStream() {
        ImageEditor imageEditorOrigin = new ImageEditor(getAppContext(), null, new FirmwareProperties(VALID_MAC));
        if (imageEditorOrigin.startDownloading(getObserver())) {
            waitForFinish();
        }
        assert outputError != null;
        assertThat(outputError.getMessage(), is(ImageEditor.NULL_STREAM_TEXT));
    }

    @Test
    public void mergeNullContext() {
        InputStream humi409_OriginImgIs = getAppContext().getResources().openRawResource(getOriginalImgId());
        ImageEditor imageEditorOrigin = new ImageEditor(null, new BufferedInputStream(humi409_OriginImgIs), new FirmwareProperties(VALID_MAC));
        if (imageEditorOrigin.startDownloading(getObserver())) {
            waitForFinish();
        }
        assert outputError != null;
        assertThat(outputError.getMessage(), is(ImageEditor.NULL_CONTEXT_TEXT));
    }

    @Test
    public void mergeNullProperties() {
        InputStream humi409_OriginImgIs = getAppContext().getResources().openRawResource(getOriginalImgId());
        ImageEditor imageEditorOrigin = new ImageEditor(getAppContext(), new BufferedInputStream(humi409_OriginImgIs), null);
        if (imageEditorOrigin.startDownloading(getObserver())) {
            waitForFinish();
        }
        assert outputError != null;
        assertThat(outputError.getMessage(), is(ImageEditor.NULL_PROPERTIES_TEXT));
    }

    @Test
    public void mergeWithNullObserver() {
        InputStream humi409_OriginImgIs = getAppContext().getResources().openRawResource(getOriginalImgId());
        ImageEditor imageEditorOrigin = new ImageEditor(getAppContext(), new BufferedInputStream(humi409_OriginImgIs), new FirmwareProperties(VALID_MAC));
        assertThat(imageEditorOrigin.startDownloading(null), is(false));
    }

    private void compareData(byte[] inputData, byte[] outputData) {
        boolean isEqual = Arrays.equals(inputData, outputData);
        Log.d(ImageEditor.TAG, "compareData. isEqual " + isEqual);
        Log.d(ImageEditor.TAG, "inputData = " + Arrays.toString(inputData));
        Log.d(ImageEditor.TAG, "outputData = " + Arrays.toString(outputData));
        Log.d(ImageEditor.TAG, "inputData.length = " + inputData.length + " outputData.length = " + outputData.length);
        isMyAfterEqualsiOSAfter = isEqual;
    }

    private Observer<byte[]> getObserver() {
        return new Observer<byte[]>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(ImageEditor.TAG, "onSubscribe");
            }

            @Override
            public void onNext(byte[] bytes) {
                Log.d(ImageEditor.TAG, "onNext");
                saveToFile(getFileName(), bytes);
                compareData(toBytes(getIOSAfterImgId()), bytes);
                onTransactionFinished();
            }

            @Override
            public void onError(Throwable e) {
                outputError = e;
                Log.d(ImageEditor.TAG, "onError = " + e.getMessage());
                onTransactionFinished();
            }

            @Override
            public void onComplete() {
                Log.d(ImageEditor.TAG, "onComplete");
            }
        };
    }

    private int getOriginalImgId() {
        int id = getAppContext().getResources().getIdentifier("humiditrak_4_0_9",
                "raw", getAppContext().getPackageName());
        Log.d(ImageEditor.TAG, "getOriginalImgId = " + id);
        return id;
    }

    private int getIOSAfterImgId() {
        int id = getAppContext().getResources().getIdentifier("after",
                "raw", getAppContext().getPackageName());
        Log.d(ImageEditor.TAG, "getOriginalImgId = " + id);
        return id;
    }

    private byte[] toBytes(int id) {
        InputStream sourceIS = getAppContext().getResources().openRawResource(id);
        byte[] targetArray = new byte[0];
        try {
            targetArray = new byte[sourceIS.available()];
            sourceIS.read(targetArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return targetArray;
    }

    private void saveToFile(String filename, byte[] bytes) {
        File outputFile = new File(getAppContext().getExternalFilesDir(null), filename + ".dat");
        if (!outputFile.exists()) {
            try {
                boolean isSuccess = outputFile.createNewFile();
                OutputStream os = new FileOutputStream(outputFile);
                os.write(bytes);
                os.close();
                Log.d(ImageEditor.TAG, " file " + filename + " has been saved " + isSuccess);
                newFile = outputFile;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileName() {
        return "myAfter" + getCurrentTime();
    }

    private String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        return new SimpleDateFormat("HH_mm_ss").format(calendar.getTime());
    }

    private void onTransactionFinished() {
        Log.d(ImageEditor.TAG, " onTransactionFinished()  = " + Thread.currentThread());
        synchronized (pauseLock) {
            pauseLock.notifyAll();
            Log.d(ImageEditor.TAG, " notifyAll()" + " Current thread = " + Thread.currentThread());
        }
    }

    private void waitForFinish() {
        Log.d(ImageEditor.TAG, " waitForFinish.presynch");
        synchronized (pauseLock) {
            try {
                Log.d(ImageEditor.TAG, " waitForFinish()");
                pauseLock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
