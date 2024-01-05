package com.yk.common.service.learning;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.work.WorkManager;

import com.yk.common.model.dictionary.Dictionary;
import com.yk.common.model.dictionary.LearningEntry;
import com.yk.common.service.dictionary.DictionaryService;
import com.yk.common.utils.PreferenceHelper;
import com.yk.common.utils.ThreadOperator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RequiresApi(api = Build.VERSION_CODES.S)
public class LearningOperator {
    private final Context context;

    public void startLearning() {
        if (PreferenceHelper.PreferenceHelperHolder.INSTANCE.helper.isLearningEnabled() && !NotificationStateResolver.isSchedulerRunning(context))
            new GenericUniqueJobScheduler(context, NotificationWorker.class, 0).schedule("LearningNotification");
    }

    /**
     * Method to get learning entry
     *
     * @return learning entry
     */
    static LearningEntry getLearningEntry(Context context) {
        List<String> possibleTranslations = new ArrayList<>();
        var dictionaries = ThreadOperator.getInstance(false).executeSingle(() -> DictionaryService.getInstance().getDictionaries(),Exception::new);
        int currentPosition = new LearningStateOperator().getWordPosition(context);
        if (currentPosition > 0) {
            int previousPosition = new Random().ints(0, currentPosition).findFirst().orElse(0);
            possibleTranslations.add(dictionaries.get(previousPosition).getTranslations().get(0).getTranslation());
        }
        Dictionary currentDictionary = dictionaries.get(currentPosition);
        possibleTranslations.add(currentDictionary.getTranslations().get(0).getTranslation());
        if ((currentPosition + 1) < dictionaries.size()) {
            int nextPosition = new Random().ints(currentPosition + 1, dictionaries.size()).findFirst()
                    .orElse(dictionaries.size());
            possibleTranslations.add(dictionaries.get(nextPosition).getTranslations().get(0).getTranslation());
        }
        Collections.shuffle(possibleTranslations, new Random());
        return LearningEntry.builder()
                .originWord(currentDictionary.getOriginWord().getOriginWord())
                .correctTranslation(currentDictionary.getTranslations().get(0).getTranslation())
                .possibleTranslations(possibleTranslations)
                .build();
    }

    /**
     * Method to notify about correct answer
     */
    static void markCorrectLearning(Context context) {
        LearningStateOperator learningStateOperator = new LearningStateOperator();
        var dictionaries = ThreadOperator.getInstance(false).executeSingle(() -> DictionaryService.getInstance().getDictionaries(),Exception::new);
        int currentPosition = learningStateOperator.getWordPosition(context);
        currentPosition++;
        if (currentPosition == dictionaries.size())
            currentPosition = 0;
        learningStateOperator.setWordPosition(currentPosition, context);
    }

    public void stopLearning() {
        NotificationStateResolver.State state = NotificationStateResolver.State.readState(context);
        WorkManager.getInstance(context).cancelWorkById(state.getWorkUUID());
        ((NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE)).cancel(NotificationWorker.NOTIFICATION_ID);
    }

}
