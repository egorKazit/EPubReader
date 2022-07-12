package com.yk.common.learning;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.yk.common.constants.GlobalConstants;
import com.yk.common.model.dictionary.DictionaryPool;

/**
 * Answer worker.
 * It gets answer and handle it
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class AnswerWorker extends Worker {

    private final Context context;
    private final String outcomeMessage;
    private final int notificationId;

    /**
     * Main constructor
     *
     * @param context      context
     * @param workerParams worker params
     */
    public AnswerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        outcomeMessage = workerParams.getInputData().getString(GlobalConstants.OUTCOME_SINGLE);
        notificationId = workerParams.getInputData().getInt(GlobalConstants.NOTIFICATION_ID, 0);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        getApplicationContext().getMainExecutor().execute(() -> {
            // cancel notification
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(notificationId);
            // check answer
            if (DictionaryPool.getLearningEntry(context).getCorrectTranslation().equals(outcomeMessage)) {
                Toast.makeText(context, "Correct", Toast.LENGTH_LONG).show();
                DictionaryPool.markCorrectLearning(getApplicationContext());
            } else {
                Toast.makeText(context, "Incorrect", Toast.LENGTH_LONG).show();
            }
        });
        // set job status
        return Result.success();
    }
}
