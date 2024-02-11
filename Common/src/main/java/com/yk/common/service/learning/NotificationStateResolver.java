package com.yk.common.service.learning;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Class to store notification id
 */

public class NotificationStateResolver {

    private static final String LAST_NOTIFICATION_FILE = "lastNotification.txt";
    private static final String SERVICE_TAG = "NotificationStateResolver";

    /**
     * State checker.
     *
     * @param context context
     * @return true if no running work or notification is not shown
     */
    public static boolean isSchedulerRunning(Context context) {
        try {
            State state = State.readState(context);
            var workInfo = WorkManager.getInstance(context).getWorkInfoById(state.workUUID);
            if (workInfo.get() == null) {
                return false;
            } else {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
                var isNotificationRunning = Arrays.stream(notificationManager.getActiveNotifications())
                        .anyMatch(statusBarNotification -> statusBarNotification.getId() == NotificationWorker.NOTIFICATION_ID);
                return workInfo.get().getState() == WorkInfo.State.BLOCKED || workInfo.get().getState() == WorkInfo.State.ENQUEUED ||
                        workInfo.get().getState() == WorkInfo.State.RUNNING || isNotificationRunning;
            }
        } catch (ExecutionException | InterruptedException exception) {
            Log.e(SERVICE_TAG, Objects.requireNonNull(exception.getMessage()));
            return false;
        }
    }

    /**
     * Method to save work uuid to disk.
     * The method is needed to track status of work.
     * It should be called at the start of work.
     * Notification state is set as false in the method by default
     *
     * @param context context
     * @param uuid    work uuid
     */
    public static void saveWorkUUID(Context context, UUID uuid) {
        State.builder().workUUID(uuid).build().updateState(context);
    }

    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Setter
    @Getter
    public static class State {

        private UUID workUUID;

        static State readState(@NonNull Context context) {
            State.StateBuilder stateBuilder = State.builder();
            try {
                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(context.openFileInput(LAST_NOTIFICATION_FILE)));
                String workUUID = bufferedReader.readLine();
                stateBuilder.workUUID(UUID.fromString(workUUID));
            } catch (IOException ioException) {
                Log.e(SERVICE_TAG, Objects.requireNonNull(ioException.getMessage()));
            }
            return stateBuilder.build();
        }

        private void updateState(Context context) {
            if (workUUID == null) {
                return;
            }
            try (OutputStream outputStream = context.openFileOutput(LAST_NOTIFICATION_FILE, Context.MODE_PRIVATE)) {
                outputStream.write(workUUID.toString().getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            } catch (IOException ioException) {
                Log.e(SERVICE_TAG, Objects.requireNonNull(ioException.getMessage()));
            }
        }

    }

}
