package com.yk.fileexplorer;

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


    void up() {
        currentFolder = currentFolder.getParentFile();
        load();
    }


    void openFolder(String targetFolderName) {
        currentFolder = new File(currentFolder, targetFolderName);
        load();
    }


    private void load() {

        files.clear();

        if (currentFolder.listFiles() != null)
            Arrays.stream(currentFolder.listFiles())
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
