package com.yk.common.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.yk.common.learning.LearningOperator;

@RequiresApi(api = Build.VERSION_CODES.S)
public class AllNeedsService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new LearningOperator(this).startLearning();
        return Service.START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
