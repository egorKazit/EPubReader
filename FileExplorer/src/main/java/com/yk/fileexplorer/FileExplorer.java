package com.yk.fileexplorer;

import android.os.Bundle;
import android.os.Environment;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import lombok.Getter;

@Getter

public final class FileExplorer extends AppCompatActivity {

    private FileExplorerProgressHelper fileExplorerProgressHelper;
    private FileExplorerRecyclerViewAdapter fileExplorerRecyclerViewAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_expolorer);

        fileExplorerProgressHelper = new FileExplorerProgressHelper(this);

        RecyclerView recyclerFiles = findViewById(R.id.files);
        recyclerFiles.setLayoutManager(new LinearLayoutManager(this));
        fileExplorerRecyclerViewAdapter = new FileExplorerRecyclerViewAdapter(
                new FileExplorerListHolder(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), this), this);
        recyclerFiles.setAdapter(fileExplorerRecyclerViewAdapter);


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                fileExplorerRecyclerViewAdapter.back();
            }
        });

    }

}
