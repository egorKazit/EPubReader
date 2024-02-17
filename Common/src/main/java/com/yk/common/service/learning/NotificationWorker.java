package com.yk.common.service.learning;

import static com.yk.common.service.learning.LearningOperator.getLearningEntry;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.yk.common.R;
import com.yk.common.constants.GlobalConstants;
import com.yk.common.model.dictionary.LearningEntry;
import com.yk.common.service.dictionary.DictionaryService;
import com.yk.common.utils.PreferenceHelper;

/**
 * Notification worker.
 * It creates and shows up new notification
 */

public final class NotificationWorker extends Worker {

    public static final int NOTIFICATION_ID = 666;
    private static final String WORKER_NAME = "notificationWorker";
    private static final String WORKER_DESCRIPTION = "NotificationWorker";

    private final Context context;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        // no action if learning is disabled or notification is already shown
        if (!PreferenceHelper.PreferenceHelperHolder.INSTANCE.helper.isLearningEnabled())
            return Result.success();
        // no action if dictionary is empty
        if (DictionaryService.getInstance().getDictionaries(false).isEmpty())
            return Result.success();
        // get notification manager and notification channel
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel("1",
                WORKER_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        // set notification channel description and update notification manager with notification channel
        notificationChannel.setDescription(WORKER_DESCRIPTION);
        notificationManager.createNotificationChannel(notificationChannel);
        // prepare content intent
        Intent headerIntent = new Intent();
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                headerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        // get current learning entry
        LearningEntry learningEntry = getLearningEntry(context);
        if (learningEntry == null)
            return Result.failure();
        // start building
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "1")
                .setSmallIcon(androidx.core.R.drawable.notification_tile_bg)
                .setContentTitle(context.getString(R.string.pick_translation))
                .setContentText(learningEntry.getOriginWord())
                .setContentIntent(contentIntent)
                .setAutoCancel(false)
                .setDeleteIntent(PreferenceHelper.PreferenceHelperHolder.INSTANCE.helper.isLearningEnabled() ?
                        new LearningPendingIntent(context).appendSchedulerRepeating().getPendingIntent() :
                        new LearningPendingIntent(context).getPendingIntent());
        // set up notification actions
        learningEntry.getPossibleTranslations()
                .forEach(action -> {
                            Bundle bundle = new Bundle();
                            bundle.putString(GlobalConstants.OUTCOME_SINGLE, action);
                            bundle.putInt(GlobalConstants.NOTIFICATION_ID, NOTIFICATION_ID);
                            notificationBuilder.addAction(0, action,
                                    PreferenceHelper.PreferenceHelperHolder.INSTANCE.helper.isLearningEnabled() ?
                                            new LearningPendingIntent(context)
                                                    .appendSchedulerRepeating()
                                                    .appendActionHandling(bundle)
                                                    .getPendingIntent() :
                                            new LearningPendingIntent(context)
                                                    .appendActionHandling(bundle)
                                                    .getPendingIntent());
                        }
                );
        // notify and save state
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        // set job status
        return Result.success();
    }
}
