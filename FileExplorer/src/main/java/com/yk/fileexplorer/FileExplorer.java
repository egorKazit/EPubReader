package com.yk.fileexplorer;

import android.os.Bundle;
import android.os.Environment;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yk.fileexplorer.databinding.ActivityFileExpolorerBinding;

import java.io.File;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import lombok.Getter;

@Getter

public final class FileExplorer extends AppCompatActivity {

    private static final String PATHS_CONSTANT = "paths";

    private FileExplorerProgressHelper fileExplorerProgressHelper;
    private FileExplorerRecyclerViewAdapter fileExplorerRecyclerViewAdapter;
    private FileExplorerListHolder fileExplorerListHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityFileExpolorerBinding binding = ActivityFileExpolorerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fileExplorerProgressHelper = new FileExplorerProgressHelper(this);

        binding.files.setLayoutManager(new LinearLayoutManager(this));
        var paths = savedInstanceState != null ? savedInstanceState.getStringArray(PATHS_CONSTANT) : null;
        if (paths != null && paths.length > 0) {
            var fileStack = new Stack<File>();
            fileExplorerListHolder = new FileExplorerListHolder(Stream.of(paths).reduce(fileStack, (stack, val) -> {
                stack.push(new File(val));
                return stack;
            }, (stack, val) -> stack));
        } else {
            fileExplorerListHolder = new FileExplorerListHolder(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        }
        fileExplorerRecyclerViewAdapter = new FileExplorerRecyclerViewAdapter(
                fileExplorerListHolder, this);
        binding.files.setAdapter(fileExplorerRecyclerViewAdapter);

        Executors.newSingleThreadExecutor().submit(() -> {
            fileExplorerListHolder.load();
            runOnUiThread(() -> {
                Objects.requireNonNull(((RecyclerView)
                        binding.files.findViewById(R.id.files)).getAdapter()).notifyItemRangeChanged(0, fileExplorerListHolder.getFiles().size());
                fileExplorerProgressHelper.hide();
            });
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                fileExplorerRecyclerViewAdapter.back();
            }
        });

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray(PATHS_CONSTANT, fileExplorerListHolder.getFolderStack().stream().map(File::getAbsolutePath).toArray(String[]::new));
    }
}
