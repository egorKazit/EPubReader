package com.yk.contentviewer.maincontent;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.yk.common.model.book.BookService;
import com.yk.common.model.book.BookServiceException;
import com.yk.common.utils.Toaster;
import com.yk.contentviewer.R;

import java.util.Objects;

import lombok.SneakyThrows;

/**
 * Class with implementation of page adapter
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class ContentViewerPagerAdapter extends FragmentStateAdapter {

    private final FragmentManager fragmentManager;

    /**
     * Main constructor
     *
     * @param fragmentManager fragment manager
     * @param lifecycle       lifecycle
     */
    public ContentViewerPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new ContentFragment(position);
    }

    @Override
    public int getItemCount() {
        try {
            return BookService.getBookService().getTableOfContent().getSpines().size();
        } catch (BookServiceException bookServiceException) {
            Toaster.make(Objects.requireNonNull(fragmentManager.getPrimaryNavigationFragment()).requireContext(),
                    "Error on book loading", bookServiceException);
        }
        return 0;
    }

    public static class ContentFragment extends Fragment {

        private final int position;
        private BookService bookService;

        public ContentFragment() throws BookServiceException {
            this.bookService = BookService.getBookService();
            this.position = bookService.getCurrentChapter();
        }

        public ContentFragment(int position) {
            this.position = position;
        }

        @SneakyThrows
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.item_content_view, container, false);
            ContentViewerWevView contentViewerWevView = rootView.findViewById(R.id.contentViewerItemContentItem);
            int currentChapterPosition = BookService.getBookService().getCurrentChapterPosition();
            if (position != BookService.getBookService().getCurrentChapter()) {
                currentChapterPosition = 0;
            }
            contentViewerWevView.uploadChapter(position, currentChapterPosition);
            return rootView;
        }

    }

}
