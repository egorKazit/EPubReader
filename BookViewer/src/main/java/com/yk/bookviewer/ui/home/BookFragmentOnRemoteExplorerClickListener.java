package com.yk.bookviewer.ui.home;

import android.content.Intent;
import android.view.View;

import com.yk.remoteexplorer.RemoteExplorer;

import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * On click listener for book explorer
 */
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public final class BookFragmentOnRemoteExplorerClickListener implements View.OnClickListener {

    private final Consumer<Intent> launcher;

    @Override
    public void onClick(View v) {
        Intent launchIntent = new Intent(v.getContext(), RemoteExplorer.class);
        launcher.accept(launchIntent);
    }
}
