package com.yk.contentviewer.maincontent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.yk.common.constants.GlobalConstants;
import com.yk.common.model.book.BookService;
import com.yk.common.model.book.BookServiceException;
import com.yk.common.model.book.TableOfContent;
import com.yk.common.model.dictionary.DictionaryPool;
import com.yk.common.utils.ApplicationContext;
import com.yk.common.utils.JavaScriptInteractor;
import com.yk.common.utils.Toaster;
import com.yk.common.utils.learning.WordOperatorException;
import com.yk.common.utils.learning.WordTranslator;
import com.yk.contentviewer.R;

import java.io.IOException;

import lombok.Getter;

/**
 * Content web view.
 * It represents whole book.
 * Each chapter is separate page in page viewer
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class ContentViewerWevView extends WebView {

    final static String INTERNAL_PROTOCOL = "qk-e-book-file";

    private final ContentViewerWevView thisWevView = this;
    private final JavaScriptInteractor javaScriptInteractor = new JavaScriptInteractor(this);
    @Getter
    private int chapterPosition;
    @Getter
    private int scrollPositionY = 0;
    private BookService bookService;

    /**
     * Constructor with context
     *
     * @param context context
     */
    public ContentViewerWevView(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor with context and attributes
     *
     * @param context context
     * @param attrs   attributes
     */
    public ContentViewerWevView(@NonNull Context context, AttributeSet attrs) {
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
    public ContentViewerWevView(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Method to init web view
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        try {
            bookService = BookService.getBookService();
        } catch (BookServiceException bookServiceException) {
            Toaster.make(ApplicationContext.getContext(), "Error on script loading", bookServiceException);
        }

        requestFocus();
        getSettings().setJavaScriptEnabled(true);
        getSettings().setAllowFileAccess(true);
        getSettings().setAllowContentAccess(true);
        getSettings().setLoadWithOverviewMode(true);
        getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        setWebViewClient(new ContentViewerWebViewClient(javaScriptInteractor, thisWevView, thisWevView::onRequest));

        try {
            if (bookService.getTextSize() != 0)
                setTextSize(bookService.getTextSize());
        } catch (BookServiceException bookServiceException) {
            Toaster.make(getContext(), "Error on loading", bookServiceException);
        }

        setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            scrollPositionY = scrollY;
            if (scrollPositionY > 0) {
                int wholePagesCount = (int) Math.ceil(getVerticalScrollRange() * 1.0 / getHeight() - 1);
                int currentPagePosition = (int) Math.ceil(scrollPositionY * 1.0 / getHeight());
                currentPagePosition = Math.min(currentPagePosition, wholePagesCount);
                ((TextView) ((Activity) getContext()).findViewById(R.id.contentViewerPosition)).setText(
                        String.format("Progress: %s page of %s pages", currentPagePosition, wholePagesCount)
                );
                ((Activity) getContext()).findViewById(R.id.contentViewerPosition).setVisibility(VISIBLE);
            } else {
                ((Activity) getContext()).findViewById(R.id.contentViewerPosition).setVisibility(INVISIBLE);
            }
            ContentViewerStateSaver.getInstance().startContentSaver(scrollPositionY);
        });

        try {
            if (WordTranslator.getLanguage().equals(bookService.getLanguage())) {
                ((Activity) getContext()).findViewById(R.id.contentViewerTranslatedWord).setVisibility(GONE);
            }
        } catch (BookServiceException bookServiceException) {
            Toaster.make(getContext(), "Error on loading", bookServiceException);
        }

    }

    public int getVerticalScrollRange() {
        return computeVerticalScrollRange();
    }

    public void handleSelectWord(@NonNull String originText) {
        if (((Activity) getContext()).findViewById(R.id.contentViewerTranslatedWord).getVisibility() != VISIBLE) {
            return;
        }
        // set translation text
        new Thread(() -> {
            String translation = DictionaryPool.getWordTranslation(originText);
            ((Activity) getContext()).runOnUiThread(() ->
                    ((TextView) ((Activity) getContext()).findViewById(R.id.contentViewerTranslatedWord))
                            .setText(translation));
        }).start();
    }

    @SuppressLint("SetTextI18n")
    public void handleSelectPhrase(@NonNull String originText) {
        // set translation text
        if (((Activity) getContext()).findViewById(R.id.contentViewerTranslatedContext).getVisibility() != VISIBLE) {
            return;
        }
        new Thread(() -> {
            try {
                ((TextView) ((Activity) getContext()).findViewById(R.id.contentViewerTranslatedContext))
                        .setText(new WordTranslator().translateText(originText).get(0));
            } catch (WordOperatorException e) {
                ((TextView) ((Activity) getContext()).findViewById(R.id.contentViewerTranslatedContext))
                        .setText(GlobalConstants.ERROR_ON_TRANSLATE + e.getMessage());
            }
        }).start();
    }

    public void handleSelectedPhrase(@NonNull String originText) {
        String originTextTrim = originText.trim();
        if (originTextTrim.length() > 0 && !originTextTrim.contains(" ")) {
            // set translation text
            new Thread(() -> ((TextView) ((Activity) getContext()).findViewById(R.id.contentViewerTranslatedWord))
                    .setText(DictionaryPool.getWordTranslation(originText))).start();
        } else {
            final AlertDialog alertDialog =
                    new AlertDialog.Builder(getContext())
                            .setMessage(originTextTrim)
                            .setPositiveButton("Translate", null)
                            .setNegativeButton("Close", null).show();
            Button translateButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            translateButton.setOnClickListener(dialog -> {
                try {
                    alertDialog.setMessage(String.join("\n", originTextTrim, new WordTranslator().translateText(originText).get(0)));
                } catch (WordOperatorException e) {
                    alertDialog.setMessage(String.join("\n", originTextTrim, GlobalConstants.ERROR_ON_TRANSLATE + e.getMessage()));
                }
                translateButton.setVisibility(View.GONE);
            });
        }
    }

    public void setTextSize(int textSize) throws BookServiceException {
        getSettings().setTextZoom(textSize);
        BookService.getBookService().setTextSize(textSize);
    }

    void uploadChapter(int chapterPosition, int scrollPosition) throws BookServiceException {
        this.chapterPosition = chapterPosition;
        scrollPositionY = scrollPosition;
        TableOfContent.Spine spine = BookService.getBookService().getTableOfContent().getSpineById(chapterPosition);
        loadUrl(INTERNAL_PROTOCOL + "://localhost/" + spine.getChapterRef());
    }

    private WebResourceResponse onRequest(@NonNull Uri url) {
        String resourcePath = url.getPath();
        if (resourcePath.equals("/"))
            resourcePath = url.getAuthority();
        if (resourcePath.charAt(0) == '/')
            resourcePath = resourcePath.substring(1);
        try {
            String finalResourcePath = resourcePath;
            return new WebResourceResponse(
                    BookService.getBookService()
                            .getTableOfContent().getSpines()
                            .stream()
                            .filter(mimeType -> mimeType.getChapterRef().equals(finalResourcePath))
                            .map(TableOfContent.Spine::getMediaType)
                            .findFirst().orElseGet(() -> {
                        if (finalResourcePath.endsWith(".css"))
                            return "text/css";
                        else if (finalResourcePath.endsWith(".js"))
                            return "text/javascript";
                        else
                            return "text/html";
                    }),
                    Xml.Encoding.UTF_8.name(),
                    BookService.getBookService().getResourceAsStream(resourcePath)
            );
        } catch (IOException | BookServiceException e) {
            e.printStackTrace();
        }
        return null;
    }

}