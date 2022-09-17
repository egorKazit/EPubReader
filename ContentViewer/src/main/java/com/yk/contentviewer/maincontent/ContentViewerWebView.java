package com.yk.contentviewer.maincontent;

import static com.yk.contentviewer.maincontent.ContentViewerWebViewSettings.initSettings;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import com.yk.common.model.book.BookService;
import com.yk.common.model.book.BookServiceException;
import com.yk.common.model.book.TableOfContent;
import com.yk.common.utils.ApplicationContext;
import com.yk.common.utils.JavaScriptInteractor;
import com.yk.common.utils.PreferenceHelper;
import com.yk.common.utils.Toaster;
import com.yk.common.utils.learning.WordTranslator;
import com.yk.contentviewer.R;

import lombok.Getter;
import lombok.SneakyThrows;

/**
 * Content web view.
 * It represents whole book.
 * Each chapter is separate page in page viewer
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class ContentViewerWebView extends WebView {

    final static String INTERNAL_BOOK_PROTOCOL = "qk-e-book-file";

    @Getter
    private int chapterNumber;
    @Getter
    private int verticalPosition = 0;
    private BookService bookService;
    @Getter
    private ContentViewerJSHandler contentViewerJSHandler;
    private int allPageHeight = 0;
    private int onePageHeight = 0;
    private ContentViewerWebViewResourceGetter contentViewerWebViewResourceGetter;

    /**
     * Constructor with context
     *
     * @param context context
     */
    public ContentViewerWebView(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor with context and attributes
     *
     * @param context context
     * @param attrs   attributes
     */
    public ContentViewerWebView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Constructor with context, attributes and style
     *
     * @param context  context
     * @param attrs    attributes
     * @param defStyle style
     */
    public ContentViewerWebView(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Method to init web view
     */
    private void init() {
        try {
            // get book service
            bookService = BookService.getBookService();
            // set text size
            if (bookService.getTextSize() != 0)
                setTextSize(bookService.getTextSize());
            // set speech feature
            if (WordTranslator.getLanguage().equals(bookService.getLanguage())) {
                ((Activity) getContext()).findViewById(R.id.contentViewerSoundPlay).setVisibility(GONE);
                ((Activity) getContext()).findViewById(R.id.contentViewerTranslatedWord).setVisibility(GONE);
            }
        } catch (BookServiceException bookServiceException) {
            Toaster.make(ApplicationContext.getContext(), "Error on script loading", bookServiceException);
        }

        contentViewerJSHandler = new ContentViewerJSHandler(this);
        contentViewerWebViewResourceGetter = new ContentViewerWebViewResourceGetter(this);
        setBackgroundColor(Color.TRANSPARENT);

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK) && PreferenceHelper.Instance.INSTANCE.helper.isNightMode()) {
            WebSettingsCompat.setForceDark(getSettings(), WebSettingsCompat.FORCE_DARK_ON);
        }

        requestFocus();
        initSettings(this);
        setWebViewClient(new ContentViewerWebViewClient(new JavaScriptInteractor(this), this, this::onRequest));

        setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            verticalPosition = scrollY;
            if (verticalPosition > 0) {
                setContentPosition();
            }
            ((Activity) getContext()).findViewById(R.id.contentViewerPosition).setVisibility(verticalPosition > 0 ? VISIBLE : INVISIBLE);
            ContentViewerStateSaver.getInstance().startContentSaver(verticalPosition);
        });

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        onePageHeight = getHeight();
        allPageHeight = computeVerticalScrollRange();
        setContentPosition();
    }

    public void setContentPosition() {
        if (chapterNumber != bookService.getCurrentChapterNumber())
            return;
        int pagesCount = (int) Math.ceil(allPageHeight * 1.0 / onePageHeight - 1);
        int pageNumber = (int) Math.ceil(verticalPosition * 1.0 / onePageHeight);
        pageNumber = Math.min(pageNumber, pagesCount);
        ((TextView) ((Activity) getContext()).findViewById(R.id.contentViewerPosition)).setText(
                String.format("Progress: %s page of %s pages", pageNumber, pagesCount)
        );
    }

    public void setTextSize(int textSize) throws BookServiceException {
        getSettings().setTextZoom(textSize);
        bookService.setTextSize(textSize);
    }

    void uploadChapter(int chapterPosition, int scrollPosition) throws BookServiceException {
        this.chapterNumber = chapterPosition;
        verticalPosition = scrollPosition;
        TableOfContent.Spine spine = bookService.getTableOfContent().getSpineById(chapterPosition);
        loadUrl(INTERNAL_BOOK_PROTOCOL + "://localhost/" + spine.getChapterRef());
    }

    @SneakyThrows
    private WebResourceResponse onRequest(@NonNull Uri uri) {
        if (BookService.getBookService().isEntryPresented(uri.getPath().substring(1)))
            return contentViewerWebViewResourceGetter.onBookRequest(uri);
        else
            return contentViewerWebViewResourceGetter.onInternalFileRequest(uri);
    }


}