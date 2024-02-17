package com.yk.contentviewer.maincontent;

import com.yk.common.context.ApplicationContext;
import com.yk.common.service.book.BookService;
import com.yk.common.service.book.BookServiceException;
import com.yk.common.service.book.BookServiceHelper;
import com.yk.common.utils.Toaster;
import com.yk.contentviewer.R;

import java.util.Timer;
import java.util.TimerTask;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Content view state saver
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContentViewerStateSaver {

    private static final int SCHEDULE_DURATION = 5000;
    private static final int IMMEDIATELY_SCHEDULE_DURATION = 5;
    private static ContentViewerStateSaver contentViewerStateSaver;
    private Timer saveTimer;

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

    /**
     * Method to initiate data saving
     *
     * @param chapterNumber chapter number
     * @param immediateFlag immediate flag
     */
    public void startContentSaver(int chapterNumber, boolean immediateFlag) {
        if (saveTimer != null) {
            saveTimer.cancel();
        }
        saveTimer = new Timer();
        saveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    BookService.getBookService()
                            .setCurrentChapterPosition(chapterNumber);
                    BookServiceHelper.updatePersistenceBook(BookService.getBookService());
                } catch (BookServiceException bookServiceException) {
                    Toaster.make(ApplicationContext.getContext(), R.string.error_on_state_loading, bookServiceException);
                }
            }
        }, immediateFlag ? IMMEDIATELY_SCHEDULE_DURATION : SCHEDULE_DURATION);
    }

    public void startContentSaver(int position) {
        startContentSaver(position, false);
    }
}
