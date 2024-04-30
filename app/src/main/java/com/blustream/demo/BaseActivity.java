package com.blustream.demo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import io.blustream.logger.Log;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Dispatch onLowMemory() to all fragments.
     */
    @Override
    public void onLowMemory() {
        Log.i("WARNING! onLowMemory called.. OS is asking us to clean up, we may be killed!!!");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        String levelString;
        switch (level) {
            case TRIM_MEMORY_BACKGROUND:
                levelString = "TRIM_MEMORY_BACKGROUND: App has gone onto the LRU list...";
                break;
            case TRIM_MEMORY_COMPLETE:
                levelString = "TRIM_MEMORY_COMPLETE: We are background, and App will be killed shortly if resources are not freed for the system";
                break;
            case TRIM_MEMORY_MODERATE:
                levelString = "TRIM_MEMORY_MODERATE: We are background, and OS is requesting we free some resources if possible.";
                break;
            case TRIM_MEMORY_RUNNING_CRITICAL:
                levelString = "TRIM_MEMORY_RUNNING_CRITICAL: We are foreground, but system is about to be unable to run background processes.. Free some resources if possible...";
                break;
            case TRIM_MEMORY_RUNNING_LOW:
                levelString = "TRIM_MEMORY_RUNNING_LOW: We are foreground, but system is asking us to free up some resources if we can...";
                break;
            case TRIM_MEMORY_RUNNING_MODERATE:
                levelString = "TRIM_MEMORY_RUNNING_MODERATE: We are foreground, and OS is requesting we free some resources if possible.";
                break;
            case TRIM_MEMORY_UI_HIDDEN:
                levelString = "TRIM_MEMORY_UI_HIDDEN: App is no longer showing a UI";
                break;
            default:
                levelString = "Unknown level... WARNING! We could be killed at any time...";
                break;
        }

        String classString = getActivityIdentifier();

        Log.i(classString + " onTrimMemory called: " + levelString);
        super.onTrimMemory(level);
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        String classString = getActivityIdentifier();
        Log.i(classString + " onPause() called");

        super.onPause();
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        String classString = getActivityIdentifier();
        Log.i(classString + " onResume() called");

        super.onResume();
    }

    protected abstract String getActivityIdentifier();
}
