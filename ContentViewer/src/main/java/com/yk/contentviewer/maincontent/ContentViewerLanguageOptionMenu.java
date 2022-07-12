package com.yk.contentviewer.maincontent;

import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.yk.common.model.book.BookService;
import com.yk.common.model.book.BookServiceException;
import com.yk.common.utils.Toaster;
import com.yk.common.utils.learning.WordOperatorException;
import com.yk.common.utils.learning.WordTranslator;
import com.yk.contentviewer.R;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Language option menu
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class ContentViewerLanguageOptionMenu {

    /**
     * Method to prepare option menu
     *
     * @param menu          option menu
     * @param translateWord translated word view
     */
    public static void prepareLanguageOptionMenu(Menu menu, TextView translateWord) {
        // find menu and create sub menu
        MenuItem menuItem = menu.findItem(R.id.targetLanguageValue);
        SubMenu subMenu = menuItem.getSubMenu();
        AtomicInteger index = new AtomicInteger();
        List<WordTranslator.Language> languages;
        String sourceLanguage;
        // get all supported languages and book language
        try {
            languages = WordTranslator.getLanguages();
            sourceLanguage = BookService.getBookService().getLanguage();
        } catch (WordOperatorException | BookServiceException exception) {
            Toaster.make(translateWord.getContext(), "Error on table of content loading", exception);
            return;
        }
        if (languages == null)
            return;
        // inflate all languages
        languages.forEach(language -> subMenu.add(0, index.getAndIncrement(), Menu.NONE, language.getName())
                .setOnMenuItemClickListener(item -> {
                    WordTranslator.setLanguage(languages.get(item.getItemId()).getLanguage());

                    menuItem.setTitle(languages.get(item.getItemId()).getName());
                    if (WordTranslator.getLanguage().equals(sourceLanguage)) {
                        translateWord.setVisibility(View.GONE);
                    }
                    return false;
                }));

        menuItem.setTitle(languages.stream().filter(language -> language.getLanguage().equals(WordTranslator.getLanguage()))
                .findFirst().orElseGet(WordTranslator.Language::new).getName());
    }
}
