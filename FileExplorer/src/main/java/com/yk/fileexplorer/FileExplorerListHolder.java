package com.yk.fileexplorer;

import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import lombok.Getter;


@Getter
public final class FileExplorerListHolder {

    private final FileExplorer fileExplorer;
    private File currentFolder;
    private final List<FileExplorerItem> files = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.S)
    FileExplorerListHolder(File currentFolder, FileExplorer fileExplorer) {
        this.currentFolder = currentFolder;
        this.fileExplorer = fileExplorer;
        new Thread(() -> {
            this.load();
            fileExplorer.runOnUiThread(() -> {
                Objects.requireNonNull(((RecyclerView)
                        fileExplorer.findViewById(R.id.files)).getAdapter()).notifyItemRangeChanged(0, files.size());
                fileExplorer.getFileExplorerProgressHelper().hide();
            });
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    void up() {
        currentFolder = currentFolder.getParentFile();
        load();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    void openFolder(String targetFolderName) {
        currentFolder = new File(currentFolder, targetFolderName);
        load();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void load() {

        files.clear();

        Arrays.stream(Objects.requireNonNull(currentFolder.listFiles()))
                .filter(file -> (file.isFile() && file.getName().toUpperCase().endsWith(".EPUB")) || !file.isFile())
                .sorted((file, second) -> {
                    if (!file.isFile() && second.isFile())
                        return -1;
                    if (file.isFile() && !second.isFile())
                        return 1;
                    return Long.compare(file.lastModified(), second.lastModified());
                }).forEach(file -> files.add(new FileExplorerItem(file.getName(), file.getAbsolutePath(), file.isFile())));

        if (!currentFolder.getAbsolutePath().equals("/") && currentFolder.getParentFile() != null && currentFolder.getParentFile().listFiles() != null) {
            FileExplorerItem fileExplorerItem = new FileExplorerItem("..", null, false);
            files.add(0, fileExplorerItem);
        }

    }

}
