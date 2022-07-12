package com.yk.common.learning;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;

/**
 * Generic Unique Job Scheduler.
 * It create an instance what can enqueue one-time scheduled work.
 * Work, initial delay and data for work should be provided as constructor parameter
 */
@AllArgsConstructor
@RequiresApi(api = Build.VERSION_CODES.S)
@SuppressLint("RestrictedApi")
public class GenericUniqueJobScheduler {

    private final Context context;
    private final Class<? extends Worker> workerClass;
    private final int initialDelay;
    private final Map<String, Object> data;

    /**
     * Constructor with empty data for work
     *
     * @param context      context
     * @param workerClass  extension of work
     * @param initialDelay initial delay
     */
    public GenericUniqueJobScheduler(Context context, Class<? extends Worker> workerClass, int initialDelay) {
        this(context, workerClass, initialDelay, Map.of());
    }

    /**
     * Method which starts scheduling
     */
    public void schedule(String uniqueWorkName) {
        Data.Builder builder = new Data.Builder();
        data.forEach(builder::put);
        // create and start one time worker request
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(workerClass)
                .setInitialDelay(initialDelay, TimeUnit.MINUTES)
                .setInputData(builder.build())
                .build();
        // enqueue unique job
        WorkManager.getInstance(context).enqueueUniqueWork(uniqueWorkName,
                ExistingWorkPolicy.REPLACE,
                oneTimeWorkRequest);

    }

}
