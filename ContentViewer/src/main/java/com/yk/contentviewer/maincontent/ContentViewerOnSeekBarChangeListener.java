package com.yk.contentviewer.maincontent;

import android.widget.SeekBar;

import java.util.function.BiConsumer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;

/**
 * Class to react on size progress bar change
 */
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)

public class ContentViewerOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

    @NonNull
    private final Integer viewId;
    private final BiConsumer<Integer, Integer> consumerOnProgressChange;
    private final Runnable onStartTrackingWrapper;
    private final Runnable onStopTrackingWrapper;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        consumerOnProgressChange.accept(viewId, progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        onStartTrackingWrapper.run();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        onStopTrackingWrapper.run();
    }

}
