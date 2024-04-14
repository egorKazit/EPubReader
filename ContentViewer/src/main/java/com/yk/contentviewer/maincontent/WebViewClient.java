package com.yk.contentviewer.maincontent;

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import androidx.viewpager2.widget.ViewPager2;

import com.yk.common.service.book.BookService;
import com.yk.common.service.book.BookServiceException;
import com.yk.common.utils.ParentMethodCaller;
import com.yk.common.utils.Toaster;
import com.yk.contentviewer.R;

import java.util.Objects;
import java.util.function.Function;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Content viewer web client
 */

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class WebViewClient extends android.webkit.WebViewClient {

    private final WebView webView;
    private final Function<Uri, WebResourceResponse> onRequestFunction;

    @Override
    public boolean shouldOverrideUrlLoading(android.webkit.WebView view, WebResourceRequest request) {
        if (Objects.equals(request.getUrl().getScheme(), WebView.INTERNAL_BOOK_PROTOCOL)) {
            webView.stopLoading();
            try {
                ParentMethodCaller.callConsumerOnParent(view, ViewPager2.class,
                        (viewPager2, o) -> viewPager2.setCurrentItem((Integer) o), BookService.getBookService().getChapterByHRef(request.getUrl().getPath()));
            } catch (BookServiceException bookServiceException) {
                Toaster.make(webView.getContext(), R.string.error_on_book_loading, bookServiceException);
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
    public WebResourceResponse shouldInterceptRequest(android.webkit.WebView view, WebResourceRequest request) {
        return onRequestFunction.apply(request.getUrl());
    }

    @Override
    public void onPageFinished(android.webkit.WebView view, String url) {
        super.onPageFinished(view, url);
        webView.setScripts();
    }

//    @SneakyThrows
//    @Override
//    public void onPageCommitVisible(android.webkit.WebView view, String url) {
//        super.onPageCommitVisible(view, url);
////        if (webView != null) {
////            int height = 0;
////            if (webView.getChapterNumber() < BookService.getBookService().getCurrentChapterNumber())
////                height = 1000000000;
////            webView.scrollTo(0, height);
////            BookService.getBookService().setCurrentChapterPosition(height);
////            webView.setTextSize(BookService.getBookService().getTextSize());
////        }
//    }
}
