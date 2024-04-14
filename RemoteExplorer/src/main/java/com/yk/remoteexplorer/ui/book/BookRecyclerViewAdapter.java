package com.yk.remoteexplorer.ui.book;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yk.remoteexplorer.R;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@AllArgsConstructor
public class BookRecyclerViewAdapter extends RecyclerView.Adapter<BookRecyclerViewAdapter.BooksRecyclerViewHolder> {

    private final static String BOOK_ID = "BOOK_ID";

    private final BookFragment bookFragment;

    @NonNull
    @Override
    public BooksRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        var view = LayoutInflater.from(parent.getContext()).inflate(com.yk.common.R.layout.fragment_book_item, parent, false);
        return new BooksRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BooksRecyclerViewHolder holder, int position) {
        holder.bookName.setText(String.format("%s - %s", BookHolder.books.get(position).getAuthor(), BookHolder.books.get(position).getName()));
        holder.bookName.setSelected(true);
        Glide.with(holder.itemView.getContext())
                .load(com.yk.common.R.mipmap.ic_default_book_cover_foreground)
//                    .load(CacheService.Instance.INSTANCE.cacheService.loadBitmapBytes(book.getRootPath() + book.getFilePath(),
//                            BookService.getResourceAsStreamForSingleFile(book.getFilePath(), book.getRootPath(), book.getCover())))
//                    .placeholder(R.mipmap.ic_default_book_cover_foreground)
//                    .error(R.mipmap.ic_default_book_cover_foreground)
                .fitCenter()
                .override(1000, 1000)
                .into(holder.bookImage);

    }

    @Override
    public int getItemCount() {
        return BookHolder.books.size();
    }

    public class BooksRecyclerViewHolder extends RecyclerView.ViewHolder {

        private final ImageView bookImage;
        private final TextView bookName;

        public BooksRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            bookImage = itemView.findViewById(com.yk.common.R.id.bookImage);
            bookImage.setOnClickListener(this::openDetails);
            bookName = itemView.findViewById(com.yk.common.R.id.bookName);
            bookName.setOnClickListener(this::openDetails);
        }

        @SneakyThrows
        private void openDetails(View view) {
            var book = BookHolder.books.get(getLayoutPosition());
            var bundle = new Bundle();
            bundle.putString(BOOK_ID, book.getId());
            NavHostFragment.findNavController(bookFragment).navigate(R.id.action_navigation_books_to_navigation_details, bundle);
        }

    }

}
