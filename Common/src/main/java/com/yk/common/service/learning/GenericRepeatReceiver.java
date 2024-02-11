package com.yk.common.service.learning;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;

import com.yk.common.R;
import com.yk.common.constants.GlobalConstants;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Broadcast receiver.
 * It reschedule the provided job(if flag is set) and accept some consumer (if provided)
 */

public class GenericRepeatReceiver extends BroadcastReceiver {

    public final static String NOTIFICATION_NAME = "LearningNotification";
    private final static String SERVICE_TAG = "GenericRepeatReceiver";
    private final static String WORK_NAME = "LearningAnswer";

    /**
     * Process scheduler configuration
     *
     * @param context context
     * @param intent  intent
     */
    @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        if (intent.getExtras() != null) {
            handleOutcomes(context, intent);
            repeatIfNeeded(context, intent);
        }
    }

    /**
     * Method to handle outcomes from job
     *
     * @param context context
     * @param intent  intent with outcomes
     */
    @SuppressWarnings("unchecked")
    private void handleOutcomes(Context context, @NonNull Intent intent) {
        // check if outcomes should be handled
        boolean hasOutcomes = intent.getBooleanExtra(GlobalConstants.OUTCOME_FLAG, false);
        if (hasOutcomes) {
            // get worker class and data for it
            String outcomeProcessorClassName = intent.getStringExtra(GlobalConstants.OUTCOME_WORKER_CLASS_NAME);
            Bundle outcomeBundles = intent.getBundleExtra(GlobalConstants.OUTCOME_BUNDLE);
            if (outcomeBundles == null)
                return;
            Map<String, Object> outcomesInMap = outcomeBundles.keySet().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(bundle -> bundle, bundle -> getBundle(outcomeBundles, bundle)));
            try {
                // get class by name and schedule new job to handle
                Class<? extends Worker> processorClass = (Class<? extends Worker>) Class.forName(outcomeProcessorClassName);
                new GenericUniqueJobScheduler(context.getApplicationContext(), processorClass, 0, outcomesInMap)
                        .schedule(WORK_NAME);
            } catch (ClassNotFoundException e) {
                Log.e(SERVICE_TAG, String.format(context.getString(R.string.error_on_work_handling), e.getMessage()), e);
            }
        }
    }

    /**
     * Method to reschedule the job if needed
     *
     * @param context context
     * @param intent  intent with job data
     */
    @SuppressWarnings("unchecked")
    private void repeatIfNeeded(Context context, @NonNull Intent intent) {
        boolean shouldSchedulerBeRepeated = intent.getBooleanExtra(GlobalConstants.SCHEDULER_REPEAT_FLAG, false);
        // check if scheduler should be repeated
        if (shouldSchedulerBeRepeated) {
            // get worker class
            String workerClassName = intent.getStringExtra(GlobalConstants.SCHEDULER_WORKER_CLASS_NAME);
            int workerRepeatInterval = intent.getIntExtra(GlobalConstants.SCHEDULER_WORKER_REPEAT_INTERVAL, 20);
            try {
                // get class by name and schedule new job
                Class<?> workerClass = Class.forName(workerClassName);
                new GenericUniqueJobScheduler(context.getApplicationContext(), (Class<? extends Worker>) workerClass,
                        workerRepeatInterval).schedule(NOTIFICATION_NAME);
            } catch (ClassNotFoundException e) {
                Log.e(SERVICE_TAG, String.format(context.getString(R.string.error_on_work_handling), e.getMessage()), e);
            }
        }
    }

    @lombok.NonNull
    private static Object getBundle(@NonNull Bundle outcomeBundles, String bundle) {
        var targetValue = outcomeBundles.get(bundle);
        return targetValue != null ? targetValue : new Object();
    }

}
