package com.yk.bookviewer.ui.home;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.yk.fileexplorer.FileExplorer;

import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * On click listener for book explorer
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class BookFragmentOnClickListener implements View.OnClickListener {

    private static final int STORAGE_PERMISSION_CODE = 101;

    private final Consumer<Intent> launcher;

    @Override
    public void onClick(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (Environment.isExternalStorageManager()) {
                Intent launchIntent = new Intent(v.getContext(), FileExplorer.class);
                launcher.accept(launchIntent);
            } else {
                //request for the permission in old way
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", v.getContext().getPackageName(), null);
                intent.setData(uri);
                v.getContext().startActivity(intent);
            }
        } else {
            // request permissions if not provided
            ActivityCompat.requestPermissions((Activity) v.getContext(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }
}
