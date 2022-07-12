package com.yk.fileexplorer;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.yk.common.constants.GlobalConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiresApi(api = Build.VERSION_CODES.S)
public class FileExplorer extends ListActivity {

    List<FileExplorerItem> files;
    private File currentDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

    public FileExplorer() {
        super();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(currentDir.getAbsolutePath());
        setContentView(R.layout.activity_file_expolorer);

        setListOfFiles();
        setListAdapter(new FileExplorerListAdapter(this, files));

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        FileExplorerItem fileExplorerItem = files.get(position);
        if (fileExplorerItem.isFile) {
            Intent intent = new Intent();
            intent.putExtra(GlobalConstants.BOOK_PATH, new File(currentDir, fileExplorerItem.filename).getAbsolutePath());
            setResult(RESULT_OK, intent);
            finish();
            return;
        }
        if (fileExplorerItem.filename.equals("..")) {
            currentDir = currentDir.getParentFile();
        } else {
            currentDir = new File(currentDir, fileExplorerItem.filename);
        }
        setListOfFiles();
        ((FileExplorerListAdapter) getListAdapter()).updateList(files);
        ((FileExplorerListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    private void setListOfFiles() {
        files = new ArrayList<>();
        if (!currentDir.getAbsolutePath().equals("/") && currentDir.getParentFile() != null && currentDir.getParentFile().listFiles() != null) {
            FileExplorerItem fileExplorerItem = new FileExplorerItem();
            fileExplorerItem.filename = "..";
            files.add(fileExplorerItem);
        }

        List<String> subDirsInString = new ArrayList<>();
        List<String> filesInString = new ArrayList<>();
        Arrays.stream(Objects.requireNonNull(currentDir.listFiles()))
                .forEach(file -> {
                    if (file.isFile()) {
                        if (file.getName().toUpperCase().endsWith(".EPUB"))
                            filesInString.add(file.getName());
                    } else
                        subDirsInString.add(file.getName());
                });

        files.addAll(subDirsInString.stream().sorted().map(fileName -> {
            FileExplorerItem fileExplorerItem = new FileExplorerItem();
            fileExplorerItem.filename = fileName;
            fileExplorerItem.isFile = false;
            return fileExplorerItem;
        }).collect(Collectors.toList()));

        files.addAll(filesInString.stream().sorted().map(fileName -> {
            FileExplorerItem fileExplorerItem = new FileExplorerItem();
            fileExplorerItem.filename = fileName;
            fileExplorerItem.filePath = new File(currentDir, fileExplorerItem.filename).getAbsolutePath();
            fileExplorerItem.isFile = true;
            return fileExplorerItem;
        }).collect(Collectors.toList()));
    }

}
