package com.yk.fileexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import lombok.Getter;


@Getter
public final class FileExplorerListHolder {

    private final List<FileExplorerItem> files = new ArrayList<>();
    private final Stack<File> folderStack;


    FileExplorerListHolder(File currentFolder) {
        this(new Stack<>());
        folderStack.add(currentFolder);
    }

    FileExplorerListHolder(Stack<File> folderStack) {
        this.folderStack = folderStack;
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

        var filesInFolder = currentFolder.listFiles();

        if (filesInFolder != null)
            files.addAll(Arrays.stream(filesInFolder)
                    .filter(file -> (file.isFile() && file.getName().toUpperCase().endsWith(".EPUB")) || !file.isFile())
                    .sorted((file, second) -> {
                        if (!file.isFile() && second.isFile())
                            return -1;
                        if (file.isFile() && !second.isFile())
                            return 1;
                        return Long.compare(second.lastModified(), file.lastModified());
                    })
                    .map(file -> new FileExplorerItem(file.getName(), file.getAbsolutePath(), file.isFile())).collect(Collectors.toList()));

        if (!currentFolder.getAbsolutePath().equals("/") && currentFolder.getParentFile() != null && currentFolder.getParentFile().listFiles() != null) {
            FileExplorerItem fileExplorerItem = new FileExplorerItem("..", null, false);
            files.add(0, fileExplorerItem);
        }

    }

}
