package com.yk.contentviewer.maincontent;

import android.annotation.SuppressLint;
import android.webkit.WebSettings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ContentViewerWebViewSettings {

    @SuppressLint("SetJavaScriptEnabled")
    static void initSettings(ContentViewerWebView contentViewerWebView) {
        contentViewerWebView.getSettings().setJavaScriptEnabled(true);
        contentViewerWebView.getSettings().setAllowFileAccess(true);
        contentViewerWebView.getSettings().setAllowContentAccess(true);
        contentViewerWebView.getSettings().setAllowFileAccessFromFileURLs(true);
        contentViewerWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        contentViewerWebView.getSettings().setLoadWithOverviewMode(true);
        contentViewerWebView.getSettings().setUseWideViewPort(true);
        contentViewerWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        contentViewerWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        contentViewerWebView.getSettings().setDomStorageEnabled(true);
        contentViewerWebView.getSettings().setDatabaseEnabled(true);
    }

}
