package com.yk.common.context;

import android.app.Application;

import com.yk.common.persistance.AppDatabaseAbstract;
import com.yk.common.persistance.AppDatabaseFactory;

import lombok.Getter;

/**
 * Application context
 */

public final class ApplicationContext extends Application {

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
    }

}
