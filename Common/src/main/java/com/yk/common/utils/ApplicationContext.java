package com.yk.common.utils;

import android.app.Application;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.yk.common.learning.GenericUniqueJobScheduler;
import com.yk.common.learning.NotificationStateResolver;
import com.yk.common.learning.NotificationWorker;
import com.yk.common.persistance.AppDatabaseAbstract;
import com.yk.common.persistance.AppDatabaseFactory;

import lombok.Getter;

/**
 * Application context
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class ApplicationContext extends Application {

    @Getter
    private static ApplicationContext context;
    @Getter
    private AppDatabaseAbstract appDatabaseAbstract;

    /**
     * Method to set application context
     */
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        appDatabaseAbstract = AppDatabaseFactory.getFromContext(this);
        //TODO: move if to separate method
        if (!NotificationStateResolver.hasActiveNotification(getApplicationContext()) && new PreferenceHelper().isLearningEnabled())
            new GenericUniqueJobScheduler(context, NotificationWorker.class, 0).schedule("LearningNotification");
        NotificationStateResolver.releaseState(getApplicationContext());
    }

}
