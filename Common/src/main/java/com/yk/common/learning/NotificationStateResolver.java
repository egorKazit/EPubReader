package com.yk.common.learning;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.work.WorkManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import lombok.SneakyThrows;

/**
 * Class to store notification id
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class NotificationStateResolver {

    private static final String LAST_NOTIFICATION_FILE = "lastNotification.txt";

    public static boolean isSchedulerRunning(Context context) {
        try {
            BufferedReader outputStream =
                    new BufferedReader(new InputStreamReader(context.openFileInput(LAST_NOTIFICATION_FILE)));
            String workUUID = outputStream.readLine();
            var workInfoById = WorkManager.getInstance(context).getWorkInfoById(UUID.fromString(workUUID));
            if (workInfoById.get() == null)
                return false;
            return !workInfoById.get().getState().isFinished();
        } catch (IOException | ExecutionException | InterruptedException exception) {
            Log.e("Error", exception.getMessage());
            return false;
        }
    }

    @SneakyThrows
    public static void saveState(Context context, UUID uuid) {
        OutputStream outputStream = context.openFileOutput(LAST_NOTIFICATION_FILE, Context.MODE_PRIVATE);
        outputStream.write(uuid.toString().getBytes(StandardCharsets.UTF_8));
    }

}
