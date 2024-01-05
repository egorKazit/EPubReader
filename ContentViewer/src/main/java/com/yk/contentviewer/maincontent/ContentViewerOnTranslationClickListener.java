package com.yk.contentviewer.maincontent;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.RequiresApi;

import com.yk.common.http.WordSpeaker;
import com.yk.common.model.dictionary.Dictionary;
import com.yk.common.service.dictionary.DictionaryService;
import com.yk.common.utils.ThreadOperator;
import com.yk.contentviewer.R;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ContentViewerOnTranslationClickListener implements View.OnClickListener {

    private final Context context;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onClick(View v) {
        PopupMenu popup = new PopupMenu(context, v);
        if (DictionaryService.getInstance().getLastOriginWord() == null)
            return;

        AtomicInteger index = new AtomicInteger();
        var dictionary = DictionaryService.getInstance().getLastTranslatedDictionary();
        dictionary.getTranslations().stream().filter(wordTranslation -> !wordTranslation.getPartOfSpeech().equals(Dictionary.MAIN_TRANSLATION))
                .forEach(wordTranslation -> {
                    popup.getMenu().add(0, index.get(), 0, String.format("%s - %s (%s)",
                            dictionary.getOriginWord().getOriginWord(), wordTranslation.getTranslation(), wordTranslation.getPartOfSpeech()));
                    popup.getMenu().getItem(index.getAndIncrement()).setEnabled(false);
                });
        popup.show();
    }
}
