package com.yk.contentviewer.maincontent;

import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

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
@RequiresApi(api = Build.VERSION_CODES.S)
public class ContentViewerLanguageOptionMenu {

    /**
     * Method to prepare option menu
     *
     * @param menu                option menu
     * @param translateWordLayout translated word view
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static void prepareLanguageOptionMenu(@NonNull Menu menu, ViewGroup translateWordLayout,
                                                 List<View> viewToReactOnLanguageChange,
                                                 List<View> leftViews) {
        // find menu and create sub menu
        MenuItem menuItem = menu.findItem(R.id.targetLanguageValue);
        SubMenu subMenu = menuItem.getSubMenu();
        AtomicInteger index = new AtomicInteger();
        List<Language> languages;
        String sourceLanguage;
        // get all supported languages and book language
        try {
            languages = LanguageService.getInstance().getLanguages();
            sourceLanguage = BookService.getBookService().getLanguage();
        } catch (BookServiceException exception) {
            Toaster.make(translateWordLayout.getContext(), "Error on table of content loading", exception);
            return;
        }
        if (subMenu == null)
            return;
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
