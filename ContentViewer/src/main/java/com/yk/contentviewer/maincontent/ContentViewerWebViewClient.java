package com.yk.contentviewer.maincontent;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.RequiresApi;
import androidx.viewpager2.widget.ViewPager2;

import com.yk.common.model.book.BookService;
import com.yk.common.model.book.BookServiceException;
import com.yk.common.utils.JavaScriptInteractor;
import com.yk.common.utils.ParentMethodCaller;
import com.yk.common.utils.Toaster;
import com.yk.contentviewer.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Content viewer web client
 */
@RequiresApi(api = Build.VERSION_CODES.S)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ContentViewerWebViewClient extends WebViewClient {

    private final JavaScriptInteractor javaScriptInteractor;
    private final ContentViewerWevView webView;
    private final Function<Uri, WebResourceResponse> onRequestFunction;
    private final Runnable onLoadFunction;

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (request.getUrl().getScheme().equals(ContentViewerWevView.INTERNAL_PROTOCOL)) {
            webView.stopLoading();
            try {
                ParentMethodCaller.callConsumerOnParent(view, ViewPager2.class,
                        (viewPager2, o) -> viewPager2.setCurrentItem((Integer) o), BookService.getBookService().getChapterByHRef(request.getUrl().getPath()));
            } catch (BookServiceException bookServiceException) {
                Toaster.make(webView.getContext(), "Book can not be loaded", bookServiceException);
            }
            return super.shouldOverrideUrlLoading(view, request);
        } else {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(request.getUrl());
            webView.getContext().startActivity(i);
            return true;
        }
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return onRequestFunction.apply(request.getUrl());
    }


    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        String selectionScript = new BufferedReader(new InputStreamReader(webView.getResources().openRawResource(R.raw.selection)))
                .lines().collect(Collectors.joining());
        javaScriptInteractor.
                addInteraction(JavaScriptInteractor.JavascriptInterfaceTag.JAVASCRIPT_CLICK_WORD_INTERFACE,
                        webView::handleSelectWord);
        javaScriptInteractor.
                addInteraction(JavaScriptInteractor.JavascriptInterfaceTag.JAVASCRIPT_SELECT_PHRASE_INTERFACE,
                        webView::handleSelectedPhrase);
        javaScriptInteractor.
                addInteraction(JavaScriptInteractor.JavascriptInterfaceTag.JAVASCRIPT_CLICK_PHRASE_INTERFACE,
                        webView::handleSelectPhrase);
        javaScriptInteractor.setupScript(selectionScript);
        webView.scrollTo(0, webView.getScrollPositionY());
        onLoadFunction.run();
    }
}
