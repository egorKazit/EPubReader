package com.yk.fileexplorer;

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import lombok.Getter;


@Getter
public final class FileExplorerListHolder {

    private final FileExplorer fileExplorer;
    private final List<FileExplorerItem> files = new ArrayList<>();
    private final Stack<File> folderStack = new Stack<>();


    FileExplorerListHolder(File currentFolder, FileExplorer fileExplorer) {
        folderStack.add(currentFolder);
        this.fileExplorer = fileExplorer;
        Executors.newSingleThreadExecutor().submit(() -> {
            this.load();
            fileExplorer.runOnUiThread(() -> {
                Objects.requireNonNull(((RecyclerView)
                        fileExplorer.findViewById(R.id.files)).getAdapter()).notifyItemRangeChanged(0, files.size());
                fileExplorer.getFileExplorerProgressHelper().hide();
            });
        });
    }


    void up() {
        folderStack.add(folderStack.peek().getParentFile());
        load();
    }


    void openFolder(String targetFolderName) {
        folderStack.add(new File(folderStack.peek(), targetFolderName));
        load();
    }

    boolean back() {
        folderStack.pop();
        return !folderStack.empty();
    }

    void load() {

        files.clear();

        var currentFolder = folderStack.peek();

        if (currentFolder.listFiles() != null)
            files.addAll(Arrays.stream(currentFolder.listFiles())
                    .parallel()
                    .filter(file -> (file.isFile() && file.getName().toUpperCase().endsWith(".EPUB")) || !file.isFile())
                    .sorted((file, second) -> {
                        if (!file.isFile() && second.isFile())
                            return -1;
                        if (file.isFile() && !second.isFile())
                            return 1;
                        return Long.compare(file.lastModified(), second.lastModified());
                    }).map(file -> new FileExplorerItem(file.getName(), file.getAbsolutePath(), file.isFile())).collect(Collectors.toList()));

        if (!currentFolder.getAbsolutePath().equals("/") && currentFolder.getParentFile() != null && currentFolder.getParentFile().listFiles() != null) {
            FileExplorerItem fileExplorerItem = new FileExplorerItem("..", null, false);
            files.add(0, fileExplorerItem);
        }

    }

}
