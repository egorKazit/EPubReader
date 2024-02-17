package com.yk.fileexplorer;


import android.view.View;

public final class FileExplorerProgressHelper {

    private final FileExplorer fileExplorer;

    FileExplorerProgressHelper(FileExplorer fileExplorer) {
        this.fileExplorer = fileExplorer;
    }

    void show() {
        fileExplorer.findViewById(R.id.loadingDialog).setVisibility(View.VISIBLE);
    }

    void hide() {
        fileExplorer.findViewById(R.id.loadingDialog).setVisibility(View.GONE);
    }

}
