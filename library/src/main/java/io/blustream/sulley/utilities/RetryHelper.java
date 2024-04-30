package io.blustream.sulley.utilities;

import android.os.Handler;

public class RetryHelper {

    private int maxRetryAttempt;
    private int retryAttemptPeriodMillis;
    private RetryListener retryListener;

    private int retryAttemptCount;
    private Handler handler = new Handler();
    private Runnable retryRunnable = new Runnable() {
        @Override
        public void run() {
            ++retryAttemptCount;
            if (retryAttemptCount > maxRetryAttempt) {
                stop();
                if (retryListener != null) {
                    retryListener.onTimeOut();
                }
            } else {
                if (retryListener != null) {
                    retryListener.onRetry();
                }
                handler.postDelayed(retryRunnable, retryAttemptPeriodMillis);
            }
        }
    };

    public RetryHelper(int maxRetryAttempt, int retryAttemptPeriodMillis) {
        this.maxRetryAttempt = maxRetryAttempt;
        this.retryAttemptPeriodMillis = retryAttemptPeriodMillis;
    }

    public void start(RetryListener retryListener) {
        this.retryListener = retryListener;
        handler.postDelayed(retryRunnable, retryAttemptPeriodMillis);
    }

    public void stop() {
        handler.removeCallbacks(retryRunnable);
        retryListener = null;
    }

    public interface RetryListener {

        void onRetry();

        void onTimeOut();
    }
}
