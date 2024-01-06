package com.yk.contentviewer.maincontent;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.util.Xml;
import android.webkit.WebResourceResponse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.yk.common.constants.ContentFont;
import com.yk.common.model.book.TableOfContent;
import com.yk.common.service.book.BookService;
import com.yk.common.service.book.BookServiceException;
import com.yk.common.utils.InputStreamWrapper;
import com.yk.common.utils.PreferenceHelper;
import com.yk.common.utils.Toaster;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@RequiresApi(api = Build.VERSION_CODES.S)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ContentViewerWebViewResourceGetter {

    private final Activity activity;

    @Nullable
    WebResourceResponse onBookRequest(@NonNull Uri bookUri) {
        String resourcePath = bookUri.getPath() != null ? bookUri.getPath() : "";
        if (resourcePath.equals("/"))
            resourcePath = bookUri.getAuthority() != null ? bookUri.getAuthority() : "";
        if (resourcePath.charAt(0) == '/')
            resourcePath = resourcePath.substring(1);
        try {
            var mimeTypeForResource = getMimeType(resourcePath);
            InputStream inputStream;
            if (resourcePath.endsWith("html") || resourcePath.endsWith("htm")) {
                if (PreferenceHelper.PreferenceHelperHolder.INSTANCE.helper.getContentFont() == ContentFont.DEFAULT)
                    inputStream = new InputStreamWrapper(BookService.getBookService().getResourceAsStream(resourcePath));
                else
                    inputStream = new InputStreamWrapper(BookService.getBookService().getResourceAsStream(resourcePath))
                            .setInjector(ContentViewerWebViewResourceEnum.HTML_TEXT_FONT_HEADER.toMap(
                                    PreferenceHelper.PreferenceHelperHolder.INSTANCE.helper.getContentFont().getFontTechnicalName()))
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
            Toaster.make(activity, "Error on load", e);
        }
        return null;
    }

    @Nullable
    WebResourceResponse onInternalFileRequest(@NonNull Uri internalFileUri) {
        String path = internalFileUri.getPath();
        if (path == null)
            return null;
        String mimeTypeForResource;
        try {
            mimeTypeForResource = Files.probeContentType(Paths.get(path));
            return new WebResourceResponse(mimeTypeForResource,
                    Xml.Encoding.UTF_8.name(),
                    activity.getAssets().open(path.substring(1)));
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
