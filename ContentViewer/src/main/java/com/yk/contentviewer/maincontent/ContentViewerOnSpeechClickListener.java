package com.yk.contentviewer.maincontent;

import android.content.Context;
import android.view.View;
import android.widget.PopupMenu;

import com.yk.common.http.WordSpeaker;
import com.yk.common.service.dictionary.DictionaryService;
import com.yk.contentviewer.R;

/**
 * Speech inflater
 */

public class ContentViewerOnSpeechClickListener implements View.OnClickListener {

    private final Context context;

    /**
     * Main constructor
     *
     * @param context context
     */
    public ContentViewerOnSpeechClickListener(Context context) {
        this.context = context;
        WordSpeaker.init();
    }

    @Override
    public void onClick(View v) {
        PopupMenu popup = new PopupMenu(context, v);
        if (DictionaryService.getInstance().getLastOriginWord() == null)
            return;
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.translateSource) {
                WordSpeaker.speakSourcePhrase();
            } else if (itemId == R.id.translateTarget) {
                WordSpeaker.speakTargetPhrase();
            }
            return true;
        });
        popup.inflate(R.menu.speech_menu);
        popup.show();
    }
}
