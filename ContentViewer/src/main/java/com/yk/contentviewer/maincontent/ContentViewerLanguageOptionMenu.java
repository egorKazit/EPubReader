package com.yk.contentviewer.maincontent;

import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

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
     * @param menu                option menu
     * @param translateWordLayout translated word view
     */
    public static void prepareLanguageOptionMenu(Menu menu, ViewGroup translateWordLayout,
                                                 List<View> viewToReactOnLanguageChange,
                                                 List<View> leftViews) {
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
            Toaster.make(translateWordLayout.getContext(), "Error on table of content loading", exception);
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
                        viewToReactOnLanguageChange.forEach(view -> view.setVisibility(View.GONE));
                        translateWordLayout.getLayoutParams().height = leftViews.stream().mapToInt(View::getHeight).sum();
                        translateWordLayout.getLayoutParams().width = leftViews.stream().mapToInt(View::getWidth).sum();
                    } else {
                        viewToReactOnLanguageChange.forEach(view -> view.setVisibility(View.VISIBLE));
                        translateWordLayout.getLayoutParams().width = ((ViewGroup)translateWordLayout.getParent()).getLayoutParams().width;
                    }
                    return false;
                }));

        menuItem.setTitle(languages.stream().filter(language -> language.getLanguage().equals(WordTranslator.getLanguage()))
                .findFirst().orElseGet(WordTranslator.Language::new).getName());
    }
}
