package com.yk.bookviewer.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;

import com.yk.fileexplorer.FileExplorer;

import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * On click listener for book explorer
 */
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class BookFragmentOnClickListener implements View.OnClickListener {

    public static final String PACKAGE = "package";
    private final Consumer<Intent> launcher;

    @Override
    public void onClick(View v) {
        if (Environment.isExternalStorageManager()) {
            Intent launchIntent = new Intent(v.getContext(), FileExplorer.class);
            launcher.accept(launchIntent);
        } else {
            //request for the permission in old way
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts(PACKAGE, v.getContext().getPackageName(), null);
            intent.setData(uri);
            v.getContext().startActivity(intent);
        }
    }
}
