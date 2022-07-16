package com.yk.common.learning;

import static com.yk.common.model.dictionary.DictionaryPool.getLearningEntry;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.yk.common.constants.GlobalConstants;
import com.yk.common.model.dictionary.DictionaryPool;
import com.yk.common.model.dictionary.LearningEntry;
import com.yk.common.utils.PreferenceHelper;

import java.util.Random;

import lombok.SneakyThrows;

/**
 * Notification worker.
 * It creates and shows up new notification
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class NotificationWorker extends Worker {

    private final Context context;
    private final int notificationId = new Random().nextInt();

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @SneakyThrows
    @NonNull
    @Override
    public Result doWork() {
        // no action if learning is disabled or notification is already shown
        if (!new PreferenceHelper().isLearningEnabled())
            return Result.success();
        // no action if dictionary is empty
        if (DictionaryPool.getDictionaries().size() == 0)
            return Result.success();
        // get notification manager and notification channel
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel("1",
                "notificationWorker",
                NotificationManager.IMPORTANCE_DEFAULT);
        // set notification channel description and update notification manager with notification channel
        notificationChannel.setDescription("NotificationWorker");
        notificationManager.createNotificationChannel(notificationChannel);
        // prepare content intent
        Intent headerIntent = new Intent();
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                headerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        // get current learning entry
        LearningEntry learningEntry = getLearningEntry(context);
        // start building
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "1")
                .setSmallIcon(androidx.core.R.drawable.notification_tile_bg)
                .setContentTitle("Pick Translation")
                .setContentText(learningEntry.getOriginWord())
                .setContentIntent(contentIntent)
                .setAutoCancel(false)
                .setDeleteIntent(new GenericPendingIntent(context).appendSchedulerRepeating(NotificationWorker.class).getPendingIntent());
        // set up notification actions
        learningEntry.getPossibleTranslations()
                .forEach(action -> {
                            Bundle bundle = new Bundle();
                            bundle.putString(GlobalConstants.OUTCOME_SINGLE, action);
                            bundle.putInt(GlobalConstants.NOTIFICATION_ID, notificationId);
                            notificationBuilder.addAction(0, action,
                                    new GenericPendingIntent(context)
                                            .appendSchedulerRepeating(NotificationWorker.class)
                                            .appendActionHandling(AnswerWorker.class, bundle)
                                            .getPendingIntent());
                        }
                );
        // notify and save state
        notificationManager.notify(notificationId, notificationBuilder.build());
        // set job status
        return Result.success();
    }
}
