package com.yk.contentviewer.maincontent;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.RequiresApi;

import com.yk.common.model.dictionary.DictionaryPool;
import com.yk.common.utils.learning.WordSpeaker;
import com.yk.contentviewer.R;

/**
 * Speech inflater
 */
@RequiresApi(api = Build.VERSION_CODES.S)
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
        if (DictionaryPool.getLastOriginWord() == null)
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
        popup.inflate(R.menu.menu_translation_content_viewer);
        popup.show();
    }
}
