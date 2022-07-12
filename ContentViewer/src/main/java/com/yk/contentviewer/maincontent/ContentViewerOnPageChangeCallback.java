package com.yk.contentviewer.maincontent;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.viewpager2.widget.ViewPager2;

import com.yk.common.model.book.BookService;
import com.yk.common.model.book.BookServiceException;
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

    private final Function<Integer, ContentViewerWevView> retriever;
    private final int viewId;
    private BookService bookService;

    /**
     * Main constructor
     *
     * @param retriever retriever function
     * @param viewId    view
     */
    public ContentViewerOnPageChangeCallback(Function<Integer, ContentViewerWevView> retriever, int viewId) throws BookServiceException {
        this.retriever = retriever;
        this.viewId = viewId;
        this.bookService = BookService.getBookService();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        super.onPageScrolled(position, positionOffset, positionOffsetPixels);
        if (checkDirection) {
            if (thresholdOffset > positionOffset && positionOffsetPixels > thresholdOffsetPixels) {
                Log.i("C.TAG", "going down");
                isScrollingUp = false;
            } else {
                Log.i("C.TAG", "going up");
                isScrollingUp = true;
            }
            checkDirection = false;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        super.onPageScrollStateChanged(state);
        if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
            Log.e("state", String.valueOf(state));
            checkDirection = true;
        }

    }

    @SneakyThrows
    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);
        ContentViewerWevView contentViewerWevView = retriever.apply(this.viewId);
        if (isScrollingUp && position > bookService.getCurrentChapter()) {
            ParentMethodCaller.callConsumerOnParent(contentViewerWevView, ViewPager2.class,
                    (viewPager2, o) -> viewPager2.setCurrentItem((Integer) o), bookService.getCurrentChapter());
            return;
        }
        if (!isScrollingUp && position < bookService.getCurrentChapter()) {
            ParentMethodCaller.callConsumerOnParent(contentViewerWevView, ViewPager2.class,
                    (viewPager2, o) -> viewPager2.setCurrentItem((Integer) o), bookService.getCurrentChapter());
            return;
        }
        if (contentViewerWevView != null) {
            int height = 0;
            if (position < bookService.getCurrentChapter())
                height = 1000000000;
            contentViewerWevView.scrollTo(0, height);
            bookService.setCurrentChapterPosition(height);
            contentViewerWevView.setTextSize(bookService.getTextSize());
        }
        bookService.setCurrentChapter(position);
        bookService.updatePersistenceBook();
    }
}
