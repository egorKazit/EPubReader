package com.yk.fileexplorer;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import lombok.Getter;

@Getter
@RequiresApi(api = Build.VERSION_CODES.S)
public class FileExplorer extends AppCompatActivity {

    private FileExplorerProgressHelper fileExplorerProgressHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_expolorer);

        fileExplorerProgressHelper = new FileExplorerProgressHelper(this);

        RecyclerView recyclerFiles = findViewById(R.id.files);
        recyclerFiles.setLayoutManager(new LinearLayoutManager(this));
        recyclerFiles.setAdapter(new FileExplorerRecyclerViewAdapter(
                new FileExplorerListHolder(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), this)));

    }

}
