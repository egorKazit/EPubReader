package com.yk.contentviewer.maincontent;

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.viewpager2.widget.ViewPager2;

import com.yk.common.service.book.BookService;
import com.yk.common.service.book.BookServiceException;
import com.yk.common.utils.ParentMethodCaller;
import com.yk.common.utils.Toaster;

import java.util.Objects;
import java.util.function.Function;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Content viewer web client
 */

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ContentViewerWebViewClient extends WebViewClient {

    private final ContentViewerWebView contentViewerWebView;
    private final Function<Uri, WebResourceResponse> onRequestFunction;

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (Objects.equals(request.getUrl().getScheme(), ContentViewerWebView.INTERNAL_BOOK_PROTOCOL)) {
            contentViewerWebView.stopLoading();
            try {
                ParentMethodCaller.callConsumerOnParent(view, ViewPager2.class,
                        (viewPager2, o) -> viewPager2.setCurrentItem((Integer) o), BookService.getBookService().getChapterByHRef(request.getUrl().getPath()));
            } catch (BookServiceException bookServiceException) {
                Toaster.make(contentViewerWebView.getContext(), "Book can not be loaded", bookServiceException);
            }
            return super.shouldOverrideUrlLoading(view, request);
        } else {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(request.getUrl());
            contentViewerWebView.getContext().startActivity(i);
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
        contentViewerWebView.setScripts();
    }

    @Override
    public void onPageCommitVisible(WebView view, String url) {
        super.onPageCommitVisible(view, url);
    }
}
