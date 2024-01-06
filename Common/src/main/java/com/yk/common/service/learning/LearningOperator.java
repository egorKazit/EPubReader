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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

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
        var futureDictionaries = Executors.newSingleThreadExecutor().submit(() -> DictionaryService.getInstance().getDictionaries());
        List<Dictionary> dictionaries;
        try {
            dictionaries = futureDictionaries.get();
        } catch (ExecutionException | InterruptedException e) {
            return null;
        }
        int currentPosition = new LearningStateOperator().getWordPosition(context);
        if (currentPosition > 0) {
            int previousPosition = new Random().ints(0, currentPosition).findFirst().orElse(0);
            possibleTranslations.add(DictionaryService.getMainTranslation(dictionaries.get(previousPosition)));
        }
        Dictionary currentDictionary = dictionaries.get(currentPosition);
        possibleTranslations.add(DictionaryService.getMainTranslation(currentDictionary));
        if ((currentPosition + 1) < dictionaries.size()) {
            int nextPosition = new Random().ints(currentPosition + 1, dictionaries.size()).findFirst()
                    .orElse(dictionaries.size());
            possibleTranslations.add(DictionaryService.getMainTranslation(dictionaries.get(nextPosition)));
        }
        Collections.shuffle(possibleTranslations, new Random());
        return LearningEntry.builder()
                .originWord(currentDictionary.getOriginWord().getOriginWord())
                .correctTranslation(DictionaryService.getMainTranslation(currentDictionary))
                .possibleTranslations(possibleTranslations)
                .build();
    }

    /**
     * Method to notify about correct answer
     */
    static void markCorrectLearning(Context context) {
        LearningStateOperator learningStateOperator = new LearningStateOperator();
        var futureDictionaries = Executors.newSingleThreadExecutor().submit(() -> DictionaryService.getInstance().getDictionaries());
        List<Dictionary> dictionaries;
        try {
            dictionaries = futureDictionaries.get();
        } catch (ExecutionException | InterruptedException e) {
            return;
        }
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
