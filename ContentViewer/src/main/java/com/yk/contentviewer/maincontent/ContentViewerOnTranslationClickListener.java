package com.yk.contentviewer.maincontent;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.yk.common.service.dictionary.DictionaryService;
import com.yk.contentviewer.R;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class ContentViewerOnTranslationClickListener implements View.OnClickListener {

    private final Context context;


    @Override
    public void onClick(View v) {
        PopupMenu popup = new PopupMenu(context, v);
        if (DictionaryService.getInstance().getLastOriginWord() == null)
            return;

        if (DictionaryService.getInstance().isLastRequestSuccess()) {
            AtomicInteger index = new AtomicInteger();
            var dictionary = DictionaryService.getInstance().getLastTranslatedDictionary();
            dictionary.getTranslations().stream().filter(wordTranslation -> !wordTranslation.getPartOfSpeech().equals(DictionaryService.MAIN_TRANSLATION))
                    .forEach(wordTranslation -> {
                        popup.getMenu().add(0, index.get(), 0, String.format("%s - %s (%s)",
                                dictionary.getOriginWord().getOriginWord(), wordTranslation.getTranslation(), wordTranslation.getPartOfSpeech()));
                        popup.getMenu().getItem(index.get()).setOnMenuItemClickListener(item -> {
                            popup.dismiss();
                            return true;
                        });
                        popup.getMenu().getItem(index.getAndIncrement()).setEnabled(false);
                    });
            popup.show();
        } else {
            Executors.newSingleThreadExecutor().submit(() -> {
                var dictionary = DictionaryService.getInstance().getDictionary(DictionaryService.getInstance().getLastOriginWord());
                String translation = DictionaryService.getMainTranslation(dictionary);
                ((Activity) v.getContext()).runOnUiThread(() -> {
                    ((TextView) v).setText(String.format("%s - %s", DictionaryService.getInstance().getLastOriginWord(), translation));
                    v.setSelected(true);
                });
            });
        }
    }
}
