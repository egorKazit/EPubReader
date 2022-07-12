package com.yk.common.launcher;

import android.app.Activity;
import android.content.Intent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;

import java.util.function.BiFunction;
import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Launcher wrapper to launch activity with some consumers on success or cancel from activity
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class ActivityResultLauncherWrapper {

    /**
     * get launcher from provided register, contract and consumers
     *
     * @param register          activity register
     * @param contract          contract
     * @param consumerOnSuccess action on success
     * @param consumerOnCancel  action on cancel
     * @param <I>               input type
     * @param <O>               output type
     * @return activity launcher
     */
    public static <I, O> ActivityResultLauncher<I> getLauncher(BiFunction<ActivityResultContract<I, O>, ActivityResultCallback<O>, ActivityResultLauncher<I>> register,
                                                               ActivityResultContract<I, O> contract,
                                                               Consumer<Intent> consumerOnSuccess,
                                                               Consumer<Intent> consumerOnCancel) {

        return register.apply(
                contract,
                result -> {
                    ActivityResult activityResult = (ActivityResult) result;
                    Intent data = activityResult.getData();
                    // init intent data if empty
                    if (data == null) {
                        data = new Intent();
                    }
                    // handle activity result
                    if (activityResult.getResultCode() == Activity.RESULT_OK) {
                        // action on success
                        consumerOnSuccess.accept(data);
                    } else if (activityResult.getResultCode() == Activity.RESULT_CANCELED) {
                        // action on cancel
                        consumerOnCancel.accept(data);
                    }
                });
    }

}
