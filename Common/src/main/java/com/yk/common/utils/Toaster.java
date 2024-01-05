package com.yk.common.utils;

import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Class to prepare toast with exception and show it
 */
public class Toaster {
    /**
     * Method to make a toast
     *
     * @param context   context
     * @param message   initial message
     * @param exception exception
     */
    public static void make(Context context, String message, Exception exception) {
        StringBuilder messageBuilder = new StringBuilder(message);
        Throwable exceptionToProcess = exception;
        while (exceptionToProcess != null) {
            if (exceptionToProcess.getMessage() == null)
                break;
            messageBuilder.append(" cause: ").append(exceptionToProcess.getMessage());
            exceptionToProcess = exceptionToProcess.getCause();
        }
        TextView textView = new TextView(context);
        textView.setText(messageBuilder.toString());
        textView.setTextColor(Color.BLUE);
        Toast toast = new Toast(context.getApplicationContext());
        toast.setView(textView);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }
}
