package com.yk.contentviewer.maincontent;

import android.app.Activity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;

import com.yk.common.service.book.BookService;
import com.yk.common.service.book.BookServiceException;
import com.yk.common.utils.PreferenceHelper;
import com.yk.contentviewer.R;

import java.util.Objects;

import lombok.AllArgsConstructor;

/**
 * Class show/hide translation of context and text sizer
 */

@AllArgsConstructor
public final class ItemSelector {

    private final Activity activity;

    /**
     * Method to hide or show translation context
     *
     * @param item menu item
     */
    public void onTranslationContextCall(MenuItem item) {
        if (item.isChecked()) {
            activity.findViewById(R.id.contentViewerTranslatedContext).setVisibility(View.GONE);
        } else {
            activity.findViewById(R.id.contentViewerTranslatedContext).setVisibility(View.VISIBLE);
        }
        item.setChecked(!item.isChecked());
    }

    /**
     * Method to hide or show size progress bar
     *
     * @param showProgressBar           progress bar function
     * @param cancelTimerForProgressBar cancel progress bar function
     */
    public void onSizerCall(Runnable showProgressBar, Runnable cancelTimerForProgressBar) throws BookServiceException {
        SeekBar seekBar = activity.findViewById(R.id.contentViewerItemSize);
        if (BookService.getBookService().getTextSize() != 0)
            seekBar.setProgress(BookService.getBookService().getTextSize());
        seekBar.setVisibility(View.VISIBLE);
        seekBar.setOnSeekBarChangeListener(OnSeekBarChangeListener.builder()
                .viewId(R.id.contentViewerItemContentItem)
                .consumerOnProgressChange((viewId, progress) -> {
                    try {
                        ((WebView) activity.findViewById(viewId)).setTextSize(progress);

                        String javascript = "var images = document.getElementsByTagName('img'); " +
                                "for (var i = 0; i < images.length; i++) {" +
                                "  var img = images[i];" +
                                String.format("  var targetWidth = Math.round(%s * img.width);", (float) BookService.getBookService().getTextSize() / 200) +
                                "  targetWidth = targetWidth < 80 ? 80 : targetWidth;" +
                                "  console.log(targetWidth);" +
                                "  img.width = targetWidth;" +
                                "}";
                        ((WebView) activity.findViewById(viewId)).loadUrl("javascript:" + javascript);
                    } catch (BookServiceException bookServiceException) {
                        Log.e("Sizer issue", Objects.requireNonNull(bookServiceException.getMessage()));
                    }
                })
                .onStartTrackingWrapper(cancelTimerForProgressBar)
                .onStopTrackingWrapper(showProgressBar)
                .build());
        showProgressBar.run();
    }

    /**
     * Method to hide or show translation context
     *
     * @param item menu item
     */
    public void onNightModeCall(MenuItem item) {
        PreferenceHelper.PreferenceHelperHolder.INSTANCE.helper.enableNightMode(item.isChecked());
        item.setChecked(!item.isChecked());
    }

}
