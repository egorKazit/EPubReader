package com.yk.bookviewer.ui.home;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Extension of OnScrollListener.
 * It hide or show "book explorer" button on scroll
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class BookFragmentOnScrollListener extends RecyclerView.OnScrollListener {

    private final FloatingActionButton addBook;

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int asd, int dy) {
        if (dy < 0 && !addBook.isShown())
            // if scroll down -> show the button
            addBook.show();
        else if (dy > 0 && addBook.isShown())
            // if scroll up -> hide the button
            addBook.hide();
    }

}
