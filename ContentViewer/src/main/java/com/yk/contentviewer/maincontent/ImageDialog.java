package com.yk.contentviewer.maincontent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import androidx.annotation.NonNull;

import com.github.chrisbanes.photoview.PhotoView;
import com.yk.common.service.book.BookService;
import com.yk.common.service.book.BookServiceException;
import com.yk.contentviewer.R;
import com.yk.contentviewer.databinding.ActivityContentViewerImageDialogBinding;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Image dialog.
 * It's going to be called on picture click in a book
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImageDialog {

    private static double INITIAL_HEIGHT;
    private static double INITIAL_WIDTH;

    @SuppressLint("ClickableViewAccessibility")
    static void openImageDialog(Activity context, String imageUrl) throws URISyntaxException, BookServiceException, IOException {
        URI url = new URI(imageUrl);
        Dialog dialog = new Dialog(context);
        ActivityContentViewerImageDialogBinding binding = ActivityContentViewerImageDialogBinding.inflate(context.getLayoutInflater());
        dialog.setContentView(binding.getRoot());
        Bitmap imageBitmap = BitmapFactory.decodeStream(BookService.getBookService().getResourceAsStream(url.getPath().substring(1)));
        float aspectRation = (float) imageBitmap.getWidth() / imageBitmap.getHeight();
        binding.contentViewerZoomedImage.setImageBitmap(imageBitmap);
        var displayMetrics = Resources.getSystem().getDisplayMetrics();
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        if (aspectRation > 1) {
            INITIAL_HEIGHT = width / aspectRation * .9;
            INITIAL_WIDTH = width * .9;
        } else {
            INITIAL_HEIGHT = height * .9;
            INITIAL_WIDTH = height * aspectRation * .9;
        }
        setSize(binding.contentViewerZoomedImage);
        dialog.show();
    }

    private static void setSize(@NonNull View view) {
        view.getLayoutParams().height += (int) INITIAL_HEIGHT;
        view.getLayoutParams().width += (int) INITIAL_WIDTH;
    }

}
