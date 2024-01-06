package com.yk.contentviewer.maincontent;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@RequiresApi(api = Build.VERSION_CODES.R)
public enum ContentViewerWebViewResourceEnum {

    CSS_TEXT_ALIGN("text-align: justify;", "text-align: justify-all"),
    HTML_TEXT_FONT_HEADER("<head>", "<style>@font-face {font-family: 'arden'; src: url('qk-e-book-file://localhost/fonts/%s');}body {font-family: 'arden' !important;}</style>"),
    HTML_TEXT_FONT_BODY("<body", "<body style=\"font-family: arden  !important\"");
    private final String resourceKey;
    public final String resourceValue;


    public Map<String, String> toMap(Object... attrs) {
        return Map.of(resourceKey, String.format(resourceValue, attrs));
    }
}
