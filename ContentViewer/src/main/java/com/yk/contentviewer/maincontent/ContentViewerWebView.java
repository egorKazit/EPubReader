package com.yk.contentviewer.maincontent;

import static com.yk.contentviewer.maincontent.ContentViewerWebViewSettings.initSettings;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import com.yk.common.context.ApplicationContext;
import com.yk.common.model.book.TableOfContent;
import com.yk.common.service.book.BookService;
import com.yk.common.service.book.BookServiceException;
import com.yk.common.service.dictionary.LanguageService;
import com.yk.common.utils.Toaster;
import com.yk.contentviewer.R;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.Getter;

/**
 * Content web view.
 * It represents whole book.
 * Each chapter is separate page in page viewer
 */

public final class ContentViewerWebView extends WebView {

    final static String INTERNAL_BOOK_PROTOCOL = "qk-e-book-file";
    public static final String LOCALHOST = "://localhost/";

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
    private final ScaleGestureDetector scaleGestureDetector;
    private final ContentViewerJavaScriptInteractor contentViewerJavaScriptInteractor = new ContentViewerJavaScriptInteractor(this);

    /**
     * Constructor with context
     *
     * @param context context
     */
    public ContentViewerWebView(Context context) {
        super(context);
        init();
        scaleGestureDetector = new ScaleGestureDetector(context, new ContentViewerWebViewScaleListener());
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
        scaleGestureDetector = new ScaleGestureDetector(context, new ContentViewerWebViewScaleListener());
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
        scaleGestureDetector = new ScaleGestureDetector(context, new ContentViewerWebViewScaleListener());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        var parentResult = super.dispatchTouchEvent(event);
        if (event.getPointerCount() >= 2)
            return scaleGestureDetector.onTouchEvent(event);
        else
            return parentResult;
    }

    /**
     * Method to init web view
     */
    private void init() {
        try {
            // get book service
            bookService = BookService.getBookService();
            // set text size
            if (bookService.getTextSize() != 0) setTextSize(bookService.getTextSize());
            // set speech feature
            if (LanguageService.getInstance().getLanguage().equals(bookService.getLanguage())) {
                ((Activity) getContext()).findViewById(R.id.contentViewerSoundPlay).setVisibility(GONE);
                ((Activity) getContext()).findViewById(R.id.contentViewerTranslatedWord).setVisibility(GONE);
            }
        } catch (BookServiceException bookServiceException) {
            Toaster.make(ApplicationContext.getContext(), R.string.error_on_script_loading, bookServiceException);
        }

        contentViewerJSHandler = new ContentViewerJSHandler((Activity) this.getContext());
        contentViewerWebViewResourceGetter = new ContentViewerWebViewResourceGetter((Activity) this.getContext());
        setBackgroundColor(Color.TRANSPARENT);

        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            try {
                WebSettingsCompat.setAlgorithmicDarkeningAllowed(getSettings(), true);
            } catch (Exception ignored) {
            }
        }

        requestFocus();
        initSettings(this);
        setWebViewClient(new ContentViewerWebViewClient(this, this::onRequest));

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
        if (chapterNumber != bookService.getCurrentChapterNumber()) return;
        int pagesCount = (int) Math.ceil(allPageHeight * 1.0 / onePageHeight - 1);
        int pageNumber = (int) Math.ceil(verticalPosition * 1.0 / onePageHeight);
        pageNumber = Math.min(pageNumber, pagesCount);
        ((TextView) ((Activity) getContext()).findViewById(R.id.contentViewerPosition))
                .setText(String.format(ApplicationContext.getContext().getString(R.string.progress), pageNumber, pagesCount));
    }

    public void setTextSize(int textSize) {
        getSettings().setTextZoom(textSize);
        bookService.setTextSize(textSize);
    }

    void uploadChapter(int chapterPosition, int scrollPosition) throws BookServiceException {
        this.chapterNumber = chapterPosition;
        verticalPosition = scrollPosition;
        TableOfContent.Spine spine = bookService.getTableOfContent().getSpineById(chapterPosition);
        loadUrl(INTERNAL_BOOK_PROTOCOL + LOCALHOST + spine.getChapterRef());
        this.addIntersections();
    }

    private WebResourceResponse onRequest(@NonNull Uri uri) {
        try {
            if (BookService.getBookService().isEntryPresented(Objects.requireNonNull(uri.getPath()).substring(1)))
                return contentViewerWebViewResourceGetter.onBookRequest(uri);
            else return contentViewerWebViewResourceGetter.onInternalFileRequest(uri);
        } catch (BookServiceException e) {
            Toaster.make(this.getContext(), R.string.error_on_book_loading, e);
            throw new RuntimeException(e);
        }
    }

    void addIntersections() {

        contentViewerJavaScriptInteractor.
                addInteraction(ContentViewerJavaScriptInteractor.JavascriptInterfaceTag.JAVASCRIPT_CLICK_WORD_INTERFACE,
                        contentViewerJSHandler::handleSelectedWord);
        contentViewerJavaScriptInteractor.
                addInteraction(ContentViewerJavaScriptInteractor.JavascriptInterfaceTag.JAVASCRIPT_SELECT_PHRASE_INTERFACE,
                        contentViewerJSHandler::handleSelectedPhrase);
        contentViewerJavaScriptInteractor.
                addInteraction(ContentViewerJavaScriptInteractor.JavascriptInterfaceTag.JAVASCRIPT_CLICK_PHRASE_INTERFACE,
                        contentViewerJSHandler::handleContextOfSelectedWord);
        contentViewerJavaScriptInteractor.
                addInteraction(ContentViewerJavaScriptInteractor.JavascriptInterfaceTag.JAVASCRIPT_CLICK_IMAGE_INTERFACE,
                        contentViewerJSHandler::handleSelectedImage);
    }

    void setScripts() {
        String selectionScript = new BufferedReader(new InputStreamReader(this.getResources().openRawResource(R.raw.selection)))
                .lines().collect(Collectors.joining());
        contentViewerJavaScriptInteractor.setupScript(selectionScript);
        String javascript;
        try {
            javascript = "var images = document.getElementsByTagName('img'); " +
                    "for (var i = 0; i < images.length; i++) {" +
                    "  var img = images[i];" +
                    String.format("  var targetWidth = Math.round(%s * img.width);", (float) BookService.getBookService().getTextSize() / 200) +
                    "  targetWidth = targetWidth < 80 ? 80 : targetWidth;" +
                    "  console.log('targetWidth = ' + targetWidth);" +
                    "  img.width = targetWidth;" +
                    "}";
            loadUrl("javascript:" + javascript);
        } catch (BookServiceException e) {
            throw new RuntimeException(e);
        }
        scrollTo(0, verticalPosition);
    }

    public final class ContentViewerWebViewScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private float initialDistance;

        @Override
        public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
            initialDistance = detector.getCurrentSpan();
            return super.onScaleBegin(detector);
        }

        @Override
        public boolean onScale(@NotNull ScaleGestureDetector detector) {
            var targetTextSize = (int) (ContentViewerWebView.this.getSettings().getTextZoom()
                    * (1 + (detector.getCurrentSpan() - initialDistance) / initialDistance * .05));
            targetTextSize = Math.min(Math.max(targetTextSize, 100), 500);
            ContentViewerWebView.this.setTextSize(targetTextSize);
            return super.onScale(detector);
        }
    }

}