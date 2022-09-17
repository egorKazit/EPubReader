package com.yk.contentviewer.maincontent;

import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.viewpager2.widget.ViewPager2;

import com.yk.common.model.book.BookService;
import com.yk.common.model.book.BookServiceException;
import com.yk.common.model.book.BookServiceHelper;
import com.yk.common.utils.ParentMethodCaller;

import java.util.function.Function;

import lombok.SneakyThrows;

/**
 * Class to react on page loading
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class ContentViewerOnPageChangeCallback extends ViewPager2.OnPageChangeCallback {

    private static final float thresholdOffset = 0.5f;
    private static final int thresholdOffsetPixels = 1;
    private boolean checkDirection;
    private boolean isScrollingUp = false;

    private final Function<Integer, ContentViewerWebView> retriever;
    private final int viewId;
    private final BookService bookService;

    /**
     * Main constructor
     *
     * @param retriever retriever function
     * @param viewId    view
     */
    public ContentViewerOnPageChangeCallback(Function<Integer, ContentViewerWebView> retriever, int viewId) throws BookServiceException {
        this.retriever = retriever;
        this.viewId = viewId;
        this.bookService = BookService.getBookService();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        super.onPageScrolled(position, positionOffset, positionOffsetPixels);
        if (checkDirection) {
            isScrollingUp = !(thresholdOffset > positionOffset) || positionOffsetPixels <= thresholdOffsetPixels;
            checkDirection = false;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        super.onPageScrollStateChanged(state);
        if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
            checkDirection = true;
        }

    }

    @SneakyThrows
    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);
        ContentViewerWebView contentViewerWebView = retriever.apply(this.viewId);
        if (isScrollingUp && position > bookService.getCurrentChapterNumber()) {
            ParentMethodCaller.callConsumerOnParent(contentViewerWebView, ViewPager2.class,
                    (viewPager2, o) -> viewPager2.setCurrentItem((Integer) o), bookService.getCurrentChapterNumber());
            return;
        }
        if (!isScrollingUp && position < bookService.getCurrentChapterNumber()) {
            ParentMethodCaller.callConsumerOnParent(contentViewerWebView, ViewPager2.class,
                    (viewPager2, o) -> viewPager2.setCurrentItem((Integer) o), bookService.getCurrentChapterNumber());
            return;
        }
        if (contentViewerWebView != null) {
            int height = 0;
            if (position < bookService.getCurrentChapterNumber())
                height = 1000000000;
            contentViewerWebView.scrollTo(0, height);
            bookService.setCurrentChapterPosition(height);
            contentViewerWebView.setTextSize(bookService.getTextSize());
        }
        bookService.setCurrentChapterNumber(position);
        BookServiceHelper.updatePersistenceBook(bookService);
    }
}
