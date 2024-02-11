package com.yk.common.service.learning;

import static com.yk.common.service.learning.LearningOperator.getLearningEntry;
import static com.yk.common.service.learning.LearningOperator.markCorrectLearning;

import android.app.NotificationManager;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.yk.common.R;
import com.yk.common.constants.GlobalConstants;

import java.util.Objects;

/**
 * Answer worker.
 * It gets answer and handle it
 */

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
            var learningEntry = getLearningEntry(context);
            if(learningEntry == null)
                return;
            // check answer
            if (Objects.requireNonNull(getLearningEntry(context)).getCorrectTranslation().equals(outcomeMessage)) {
                Toast.makeText(context, R.string.correct, Toast.LENGTH_LONG).show();
                markCorrectLearning(getApplicationContext());
            } else {
                Toast.makeText(context, R.string.incorrect, Toast.LENGTH_LONG).show();
            }
        });
        // set job status
        return Result.success();
    }
}
