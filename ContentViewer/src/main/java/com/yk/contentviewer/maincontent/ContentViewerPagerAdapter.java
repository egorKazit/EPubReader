package com.yk.contentviewer.maincontent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.yk.common.service.book.BookService;
import com.yk.common.service.book.BookServiceException;
import com.yk.common.utils.Toaster;
import com.yk.contentviewer.R;

import java.util.Objects;

/**
 * Class with implementation of page adapter
 */

public final class ContentViewerPagerAdapter extends FragmentStateAdapter {

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
                    R.string.error_on_book_loading, bookServiceException);
        }
        return 0;
    }

    public void refresh() {
        fragmentManager.getFragments().forEach(fragment -> {
            if (fragment.getView() == null)
                return;
            ContentViewerWebView contentViewerWebView = fragment.getView().findViewById(R.id.contentViewerItemContentItem);
            if (contentViewerWebView == null)
                return;
            contentViewerWebView.reload();
        });
    }


    /**
     * Page adapter fragment
     */
    public static class ContentFragment extends Fragment {

        private final int chapterNumber;

        /**
         * Constructor without parameters.
         * Chapter number is taken from book service
         *
         * @throws BookServiceException exception on book read
         */
        public ContentFragment() throws BookServiceException {
            BookService bookService = BookService.getBookService();
            this.chapterNumber = bookService.getCurrentChapterNumber();
        }

        /**
         * Constructor with chapter number
         *
         * @param chapterNumber chapter number
         */
        public ContentFragment(int chapterNumber) {
            this.chapterNumber = chapterNumber;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.item_content_view, container, false);
            ContentViewerWebView contentViewerWebView = rootView.findViewById(R.id.contentViewerItemContentItem);
            try {
                int chapterPosition =
                        chapterNumber == BookService.getBookService().getCurrentChapterNumber() ?
                                BookService.getBookService().getCurrentChapterPosition() : 0;
                contentViewerWebView.uploadChapter(chapterNumber, chapterPosition);
            } catch (BookServiceException e) {
                Toaster.make(requireContext(), R.string.error_on_book_loading, e);
                requireActivity().finish();
            }
            return rootView;
        }

    }

}
