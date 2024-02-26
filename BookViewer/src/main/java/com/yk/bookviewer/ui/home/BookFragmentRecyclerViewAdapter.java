package com.yk.bookviewer.ui.home;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yk.bookviewer.R;
import com.yk.common.model.book.Book;
import com.yk.common.service.book.BookPool;
import com.yk.common.service.book.BookService;
import com.yk.common.service.book.BookServiceException;
import com.yk.common.service.book.BookServiceHelper;
import com.yk.common.utils.Toaster;
import com.yk.contentviewer.ContentViewer;

import kotlin.io.ByteStreamsKt;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Recycler view adapter to get list of loaded books.
 * Events to open the book are set in the view holder
 */

@AllArgsConstructor(access = AccessLevel.PACKAGE)
class BookFragmentRecyclerViewAdapter extends RecyclerView.Adapter<BookFragmentRecyclerViewAdapter.BookViewHolder> {

    private final Runnable onLessBookRunner;

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_book_item, parent, false);
        return new BookViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = BookPool.getBook(position);
        holder.bookName.setText(book.getTitle());
        holder.bookName.setSelected(true);

        if (book.getCover() != null) {
            try {
                Glide.with(holder.itemView.getContext())
                        .load(ByteStreamsKt.readBytes(BookService
                                .getResourceAsStreamForSingleFile(book.getFilePath(), book.getRootPath(), book.getCover())))
                        .placeholder(R.mipmap.ic_default_book_cover_foreground)
                        .error(R.mipmap.ic_default_book_cover_foreground)
                        .fitCenter()
                        .override(1000, 1000)
                        .into(holder.bookImage);
            } catch (BookServiceException bookServiceException) {
                Toaster.make(holder.itemView.getContext(), String.format(holder.itemView.getContext().getString(R.string.can_not_load_cover),
                        book.getTitle()), bookServiceException);
            }
        } else
            holder.bookImage.setImageResource(R.mipmap.ic_default_book_cover_foreground);

    }

    @Override
    public int getItemCount() {
        return BookPool.getSize();
    }

    /**
     * Book View holder.
     * The class is needed to set data and events of a view for a recycler item
     */
    protected class BookViewHolder extends RecyclerView.ViewHolder {

        private final ImageView bookImage;
        private final TextView bookName;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookImage = itemView.findViewById(R.id.bookImage);
            bookImage.setOnClickListener(v -> openBook());
            bookImage.setOnLongClickListener(new BookFragmentOnLongClickListener(bookImage.getContext(), this::openBook, this::deleteBook));
            bookName = itemView.findViewById(R.id.bookName);
            bookName.setOnClickListener(v -> openBook());
        }

        /**
         * Set handler on item click
         */
        private void openBook() {
            Book book = BookPool.getBook(getLayoutPosition());
            BookServiceHelper.updateLatestBookPath(itemView.getContext(), book.getFilePath());
            try {
                // create new intent and start activity
                Intent intent = new Intent(itemView.getContext(), ContentViewer.class);
                BookService.initFromPath(book.getFilePath());
                itemView.getContext().startActivity(intent);
            } catch (BookServiceException bookServiceException) {
                Toaster.make(bookName.getContext(), String.format(itemView.getContext().getString(R.string.can_not_load_cover), book.getTitle()),
                        bookServiceException);
            }
        }

        /**
         * Method to delete book
         */
        private void deleteBook() {
            // remove from pool and notify about changes
            BookPool.removeBook(getLayoutPosition());
            notifyItemRemoved(getLayoutPosition());
            if (BookPool.getSize() <= 4) {
                onLessBookRunner.run();
            }
        }
    }
}
