package com.yk.bookviewer.ui.home;

import android.content.Context;
import android.view.View;
import android.widget.PopupMenu;

import com.yk.bookviewer.R;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * On long clink listener for book.
 * It shows content menu from with book can be opened or deleted.
 * Open/delete operation is supplied as runnable function
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class BookFragmentOnLongClickListener implements View.OnLongClickListener {

    private final Context context;
    private final Runnable openOperation;
    private final Runnable deleteOperation;

    @Override
    public boolean onLongClick(View v) {
        PopupMenu popup = new PopupMenu(context, v);
        // set handler
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.open) {
                // execute "open" function
                openOperation.run();
            } else if (itemId == R.id.close) {
                // execute "delete" function
                deleteOperation.run();
            }
            return true;
        });
        // inflate menu
        popup.inflate(R.menu.book_context_menu);
        popup.show();
        return true;
    }
}
