package com.yk.contentviewer.maincontent;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.yk.common.model.book.BookService;
import com.yk.common.model.book.BookServiceException;
import com.yk.common.model.book.BookServiceHelper;
import com.yk.common.utils.ApplicationContext;
import com.yk.common.utils.Toaster;

import java.util.Timer;
import java.util.TimerTask;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Content view state saver
 */
@RequiresApi(api = Build.VERSION_CODES.S)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ContentViewerStateSaver {

    private static final int SCHEDULE_DURATION = 5000;
    private static final int IMMEDIATELY_SCHEDULE_DURATION = 5;
    private static ContentViewerStateSaver contentViewerStateSaver;
    private Timer timer;

    /**
     * Method to implement singleton
     *
     * @return instance of class
     */
    public static ContentViewerStateSaver getInstance() {
        if (contentViewerStateSaver == null) {
            contentViewerStateSaver = new ContentViewerStateSaver();
        }
        return contentViewerStateSaver;
    }

    public void startContentSaver(int position, boolean immediately) {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    BookService.getBookService()
                            .setCurrentChapterPosition(position);
                    BookServiceHelper.updatePersistenceBook(BookService.getBookService());
                } catch (BookServiceException bookServiceException) {
                    Toaster.make(ApplicationContext.getContext(), "Error on state loading", bookServiceException);
                }
            }
        }, immediately ? IMMEDIATELY_SCHEDULE_DURATION : SCHEDULE_DURATION);
    }

    public void startContentSaver(int position) {
        startContentSaver(position, false);
    }
}
