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
    private static final String LAST_NOTIFICATION_ID = "LAST_NOTIFICATION_ID";

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
//        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
//
//        boolean hasBeenScheduled = false;
//
//        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
//            scheduler.getPendingJob(1).getClipData()
//            if (jobInfo.getId() == 0) {
//                hasBeenScheduled = true;
//                break;
//            }
//        }
//
//        return hasBeenScheduled;
    }

    @SneakyThrows
    public static void saveState(Context context, UUID uuid) {
        OutputStream outputStream = context.openFileOutput(LAST_NOTIFICATION_FILE, Context.MODE_PRIVATE);
        outputStream.write(uuid.toString().getBytes(StandardCharsets.UTF_8));
    }

//    private static final String LAST_NOTIFICATION_FILE = "lastNotification.txt";
//    private static final String LAST_NOTIFICATION_ID = "LAST_NOTIFICATION_ID";
//
//    /**
//     * Method to save notification id
//     *
//     * @param id notification id
//     */
//    @SuppressLint("ApplySharedPref")
//    static void saveState(int id) {
//        SharedPreferences sharedPreferences = ApplicationContext.getContext().getSharedPreferences(LAST_NOTIFICATION_FILE, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt(LAST_NOTIFICATION_ID, id);
//        editor.commit();
//    }
//
//    /**
//     * method to release state
//     */
//    @SuppressLint("ApplySharedPref")
//    public static void releaseState(Context context) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences(LAST_NOTIFICATION_FILE, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.remove(LAST_NOTIFICATION_ID);
//        editor.commit();
//    }
//
//    /**
//     * Method to check if notification exists
//     *
//     * @return true if notification exists
//     */
//    public static boolean hasActiveNotification(Context context) {
//        return context.getSharedPreferences(LAST_NOTIFICATION_FILE, Context.MODE_PRIVATE).contains(LAST_NOTIFICATION_ID);
//    }

}
