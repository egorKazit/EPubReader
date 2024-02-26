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

    private final FloatingActionButton addBookButton;

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int asd, int dy) {
        if (dy < 0 && !addBookButton.isShown())
            // if scroll down -> show the button
            addBookButton.show();
        else if (dy > 0 && addBookButton.isShown())
            // if scroll up -> hide the button
            addBookButton.hide();
    }

}
