package com.yk.contentviewer.maincontent;

import androidx.viewpager2.widget.ViewPager2;

import com.yk.common.service.book.BookService;
import com.yk.common.service.book.BookServiceException;
import com.yk.common.service.book.BookServiceHelper;
import com.yk.common.utils.ParentMethodCaller;

import java.util.function.Function;

/**
 * Class to react on page loading
 */

public final class OnPageChangeCallback extends ViewPager2.OnPageChangeCallback {

    private static final float thresholdOffset = 0.5f;
    private static final int thresholdOffsetPixels = 1;
    private boolean checkDirection;
    private boolean isScrollingUp = false;

    private final Function<Integer, WebView> retriever;
    private final int viewId;
    private final BookService bookService;

    /**
     * Main constructor
     *
     * @param retriever retriever function
     * @param viewId    view
     */
    public OnPageChangeCallback(Function<Integer, WebView> retriever, int viewId) throws BookServiceException {
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


    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);
        WebView webView = retriever.apply(this.viewId);
        if (isScrollingUp && position > bookService.getCurrentChapterNumber()) {
            ParentMethodCaller.callConsumerOnParent(webView, ViewPager2.class,
                    (viewPager2, o) -> {
                        if (!viewPager2.isFakeDragging()) {
                            viewPager2.setCurrentItem((Integer) o);
                        }
                    }, bookService.getCurrentChapterNumber());
            return;
        }
        if (!isScrollingUp && position < bookService.getCurrentChapterNumber()) {
            ParentMethodCaller.callConsumerOnParent(webView, ViewPager2.class,
                    (viewPager2, o) -> {
                        if (!viewPager2.isFakeDragging()) {
                            viewPager2.setCurrentItem((Integer) o);
                        }
                    }, bookService.getCurrentChapterNumber());
            return;
        }
        if (webView != null) {
            int height = 0;
            if (position < bookService.getCurrentChapterNumber())
                height = 1000000000;
            webView.scrollTo(0, height);
            bookService.setCurrentChapterPosition(height);
            webView.setTextSize(bookService.getTextSize());
        }
        bookService.setCurrentChapterNumber(position);
        BookServiceHelper.updatePersistenceBook(bookService);
    }
}
