package com.yk.common.service.learning;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.yk.common.constants.GlobalConstants;
import com.yk.common.utils.PreferenceHelper;

import java.util.Random;

/**
 * Class to create pending intent
 */

public final class LearningPendingIntent {
    private final Intent intent;
    private final Context context;
    private int counter = new Random().nextInt();

    /**
     * Main constructor
     *
     * @param context context
     */
    LearningPendingIntent(Context context) {
        this.context = context;
        intent = new Intent(context, GenericRepeatReceiver.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    /**
     * Method to append handling action
     *
     * @param bundle worker data
     * @return reference to itself
     */
    LearningPendingIntent appendActionHandling(Bundle bundle) {
        intent.putExtra(GlobalConstants.OUTCOME_FLAG, true);
        intent.putExtra(GlobalConstants.OUTCOME_WORKER_CLASS_NAME, AnswerWorker.class.getName());
        intent.putExtra(GlobalConstants.OUTCOME_BUNDLE, bundle);
        return this;
    }

    /**
     * Method to append rescheduling action
     *
     * @return reference to itself
     */
    LearningPendingIntent appendSchedulerRepeating() {
        intent.putExtra(GlobalConstants.SCHEDULER_REPEAT_FLAG, true);
        intent.putExtra(GlobalConstants.SCHEDULER_WORKER_CLASS_NAME, NotificationWorker.class.getName());
        intent.putExtra(GlobalConstants.SCHEDULER_WORKER_REPEAT_INTERVAL, PreferenceHelper.PreferenceHelperHolder.INSTANCE.helper.getLearningInterval());
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
