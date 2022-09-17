package com.yk.contentviewer.maincontent;

import android.net.Uri;
import android.os.Build;
import android.util.Xml;
import android.webkit.WebResourceResponse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.yk.common.model.book.BookService;
import com.yk.common.model.book.BookServiceException;
import com.yk.common.model.book.TableOfContent;
import com.yk.common.utils.ContentFont;
import com.yk.common.utils.InputStreamWrapper;
import com.yk.common.utils.PreferenceHelper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@RequiresApi(api = Build.VERSION_CODES.S)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ContentViewerWebViewResourceGetter {

    private final ContentViewerWebView contentViewerWebView;

    @Nullable
    WebResourceResponse onBookRequest(@NonNull Uri bookUri) {
        String resourcePath = bookUri.getPath();
        if (resourcePath.equals("/"))
            resourcePath = bookUri.getAuthority();
        if (resourcePath.charAt(0) == '/')
            resourcePath = resourcePath.substring(1);
        try {
            var mimeTypeForResource = getMimeType(resourcePath);
            InputStream inputStream;
            if (resourcePath.endsWith(".css")) {
                inputStream = new InputStreamWrapper(BookService.getBookService().getResourceAsStream(resourcePath))
                        .setReplacer(ContentViewerWebViewResourceEnum.CSS_TEXT_ALIGN.toMap());
            } else if (resourcePath.endsWith(".html")) {
                if (PreferenceHelper.Instance.INSTANCE.helper.getContentFont() == ContentFont.DEFAULT)
                    inputStream = new InputStreamWrapper(BookService.getBookService().getResourceAsStream(resourcePath));
                else
                    inputStream = new InputStreamWrapper(BookService.getBookService().getResourceAsStream(resourcePath))
                            .setInjector(ContentViewerWebViewResourceEnum.HTML_TEXT_FONT_HEADER.toMap(
                                    PreferenceHelper.Instance.INSTANCE.helper.getContentFont().getFontTechnicalName()))
                            .setReplacer(ContentViewerWebViewResourceEnum.HTML_TEXT_FONT_BODY.toMap());
            } else {
                inputStream = BookService.getBookService().getResourceAsStream(resourcePath);
            }

            return new WebResourceResponse(
                    mimeTypeForResource,
                    Xml.Encoding.UTF_8.name(),
                    inputStream
            );
        } catch (IOException | BookServiceException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    WebResourceResponse onInternalFileRequest(@NonNull Uri internalFileUri) {
        String path = internalFileUri.getPath();
        String mimeTypeForResource;
        try {
            mimeTypeForResource = Files.probeContentType(Paths.get(path));
            return new WebResourceResponse(mimeTypeForResource,
                    Xml.Encoding.UTF_8.name(),
                    contentViewerWebView.getContext().getAssets().open(path.substring(1)));
        } catch (IOException ioException) {
            return null;
        }
    }

    private String getMimeType(String resourcePath) throws BookServiceException {
        return BookService.getBookService()
                .getTableOfContent().getSpines()
                .stream()
                .filter(mimeType -> mimeType.getChapterRef().equals(resourcePath))
                .map(TableOfContent.Spine::getMediaType)
                .findFirst().orElseGet(() -> {
                    try {
                        return Files.probeContentType(Paths.get(resourcePath));
                    } catch (IOException ioException) {
                        return "text/html";
                    }
                });
    }

}
