package com.yk.common.learning;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.work.WorkManager;

import com.yk.common.utils.PreferenceHelper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RequiresApi(api = Build.VERSION_CODES.S)
public class LearningOperator {
    private final Context context;

    public void startLearning() {
        if (PreferenceHelper.Instance.INSTANCE.helper.isLearningEnabled() && !NotificationStateResolver.isSchedulerRunning(context))
            new GenericUniqueJobScheduler(context, NotificationWorker.class, 0).schedule("LearningNotification");
    }

    public void stopLearning() {
        NotificationStateResolver.State state = NotificationStateResolver.State.readState(context);
        WorkManager.getInstance(context).cancelWorkById(state.getWorkUUID());
        ((NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE)).cancel(NotificationWorker.NOTIFICATION_ID);
    }

}
