package com.yk.contentviewer.maincontent;

import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.yk.common.model.dictionary.Language;
import com.yk.common.service.book.BookService;
import com.yk.common.service.book.BookServiceException;
import com.yk.common.service.dictionary.LanguageService;
import com.yk.common.utils.Toaster;
import com.yk.contentviewer.R;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Language option menu
 */

public final class ContentViewerLanguageOptionMenu {

    /**
     * Method to prepare option menu
     *
     * @param menu                option menu
     * @param translateWordLayout translated word view
     */
    public static void prepareLanguageOptionMenu(@NonNull Menu menu, ViewGroup translateWordLayout,
                                                 List<View> viewToReactOnLanguageChange,
                                                 List<View> leftViews) {
        // find menu and create sub menu
        MenuItem menuItem = menu.findItem(R.id.targetLanguageValue);
        SubMenu subMenu = menuItem.getSubMenu();
        if (subMenu == null)
            return;
        subMenu.clear();
        AtomicInteger index = new AtomicInteger();
        List<Language> languages;
        String sourceLanguage;
        // get all supported languages and book language
        try {
            languages = LanguageService.getInstance().getLanguages();
            sourceLanguage = BookService.getBookService().getLanguage();
        } catch (BookServiceException exception) {
            Toaster.make(translateWordLayout.getContext(), R.string.error_on_content_loading, exception);
            return;
        }
        // inflate all languages
        languages.forEach(language -> subMenu.add(0, index.getAndIncrement(), Menu.NONE, language.getName())
                .setOnMenuItemClickListener(item -> {
                    LanguageService.getInstance().setLanguage(languages.get(item.getItemId()).getLanguage());
                    menuItem.setTitle(languages.get(item.getItemId()).getName());
                    if (LanguageService.getInstance().getLanguage().equals(sourceLanguage)) {
                        viewToReactOnLanguageChange.forEach(view -> view.setVisibility(View.GONE));
                        translateWordLayout.getLayoutParams().height = leftViews.stream().mapToInt(View::getHeight).sum();
                        translateWordLayout.getLayoutParams().width = leftViews.stream().mapToInt(View::getWidth).sum();
                    } else {
                        viewToReactOnLanguageChange.forEach(view -> view.setVisibility(View.VISIBLE));
                        translateWordLayout.getLayoutParams().width = ((ViewGroup) translateWordLayout.getParent()).getLayoutParams().width;
                    }
                    return false;
                }));

        menuItem.setTitle(languages.stream().filter(language -> language.getLanguage().equals(LanguageService.getInstance().getLanguage()))
                .findFirst().orElseGet(() -> new Language("ru", "Russian")).getName());
    }
}
