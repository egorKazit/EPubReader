package com.yk.common.learning;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.work.Worker;

import com.yk.common.constants.GlobalConstants;
import com.yk.common.utils.PreferenceHelper;

import java.util.Random;

/**
 * Class to create pending intent
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class GenericPendingIntent {
    private final Intent intent;
    private final Context context;
    private int counter = new Random().nextInt();

    /**
     * Main constructor
     *
     * @param context context
     */
    GenericPendingIntent(Context context) {
        this.context = context;
        intent = new Intent(context, GenericRepeatReceiver.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    /**
     * Method to append handling action
     *
     * @param classWorker worker class
     * @param bundle      worker data
     * @return reference to itself
     */
    GenericPendingIntent appendActionHandling(Class<? extends Worker> classWorker, Bundle bundle) {
        intent.putExtra(GlobalConstants.OUTCOME_FLAG, true);
        intent.putExtra(GlobalConstants.OUTCOME_WORKER_CLASS_NAME, classWorker.getName());
        intent.putExtra(GlobalConstants.OUTCOME_BUNDLE, bundle);
        return this;
    }

    /**
     * Method to append rescheduling action
     *
     * @param classWorker worker class
     * @return reference to itself
     */
    GenericPendingIntent appendSchedulerRepeating(Class<? extends Worker> classWorker) {
        intent.putExtra(GlobalConstants.SCHEDULER_REPEAT_FLAG, true);
        intent.putExtra(GlobalConstants.SCHEDULER_WORKER_CLASS_NAME, classWorker.getName());
        intent.putExtra(GlobalConstants.SCHEDULER_WORKER_REPEAT_INTERVAL, PreferenceHelper.Instance.INSTANCE.helper.getLearningInterval());
        return this;
    }

    /**
     * create and return final intent
     *
     * @return pending intent
     */
    PendingIntent getPendingIntent() {
        counter++;
        return PendingIntent.getBroadcast(context, counter, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

}
