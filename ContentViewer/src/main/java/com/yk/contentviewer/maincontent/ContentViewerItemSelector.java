package com.yk.contentviewer.maincontent;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;

import androidx.annotation.RequiresApi;

import com.yk.common.model.book.BookService;
import com.yk.common.model.book.BookServiceException;
import com.yk.contentviewer.R;

import lombok.AllArgsConstructor;

/**
 * Class show/hide translation of context and text sizer
 */
@RequiresApi(api = Build.VERSION_CODES.S)
@AllArgsConstructor
public class ContentViewerItemSelector {

    private final Activity activity;

    /**
     * Method to hide or show translation context
     *
     * @param item menu item
     * @return true if state is changed
     */
    public boolean onTranslationContextCall(MenuItem item) {
        if (item.isChecked()) {
            activity.findViewById(R.id.contentViewerTranslatedContext).setVisibility(View.GONE);
        } else {
            activity.findViewById(R.id.contentViewerTranslatedContext).setVisibility(View.VISIBLE);
        }
        item.setChecked(!item.isChecked());
        return true;
    }

    /**
     * Method to hide or show size progress bar
     *
     * @param showProgressBar           progress bar function
     * @param cancelTimerForProgressBar cancel progress bar function
     * @return true by default
     */
    public boolean onSizerCall(Runnable showProgressBar, Runnable cancelTimerForProgressBar) throws BookServiceException {
        SeekBar seekBar = activity.findViewById(R.id.contentViewerItemSize);
        if (BookService.getBookService().getTextSize() != 0)
            seekBar.setProgress(BookService.getBookService().getTextSize());
        seekBar.setVisibility(View.VISIBLE);
        seekBar.setOnSeekBarChangeListener(ContentViewerOnSeekBarChangeListener.builder()
                .viewId(R.id.contentViewerItemContentItem)
                .consumerOnProgressChange((viewId, progress) -> {
                    try {
                        ((ContentViewerWevView) activity.findViewById(viewId)).setTextSize(progress);
                    } catch (BookServiceException bookServiceException) {
                        Log.e("Sizer issue", bookServiceException.getMessage());
                    }
                })
                .onStartTrackingWrapper(cancelTimerForProgressBar)
                .onStopTrackingWrapper(showProgressBar)
                .build());
        showProgressBar.run();
        return true;
    }

}
