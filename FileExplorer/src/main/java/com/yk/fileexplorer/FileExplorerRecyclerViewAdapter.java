package com.yk.fileexplorer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.yk.common.constants.GlobalConstants;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FileExplorerRecyclerViewAdapter extends RecyclerView.Adapter<FileExplorerRecyclerViewAdapter.FileExplorerRecyclerViewHolder> {

    private final FileExplorerListHolder fileExplorerListHolder;
    private FileExplorer fileExplorer;
    private final Context context;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    FileExplorerRecyclerViewAdapter(FileExplorerListHolder fileExplorerListHolder, Context context) {
        this.fileExplorerListHolder = fileExplorerListHolder;
        this.context = context;
    }

    @NonNull
    @Override
    public FileExplorerRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        fileExplorer = (FileExplorer) parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.file_explorer_item, parent, false);
        return new FileExplorerRecyclerViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull FileExplorerRecyclerViewHolder holder, int position) {

        var fileExplorerItem = fileExplorerListHolder.getFiles().get(position);
        holder.fileName.setText(fileExplorerItem.getFileName());

        var glide = Glide.with(holder.itemView.getContext());
        if (!fileExplorerItem.isFile()) {
            executorService.submit(() -> ((Activity) holder.itemView.getContext()).runOnUiThread(() -> {
                glide.load(R.drawable.ic_folder_foreground).centerCrop()
                        .fitCenter()
                        .into(holder.fileImage);
            }));
        } else {
            executorService.submit(() -> {
                RequestBuilder<Drawable> drawableRequestBuilder;
                if (fileExplorerItem.getCover() != null)
                    drawableRequestBuilder = glide.load(fileExplorerItem.getCover());
                else drawableRequestBuilder = glide.load(R.drawable.ic_no_cover_foreground);
                var title = fileExplorerItem.fetchTitle();
                ((Activity) holder.itemView.getContext()).runOnUiThread(() -> {
                    if (title != null && !Objects.equals(title, ""))
                        holder.fileName.setText(title);
                    drawableRequestBuilder.centerCrop()
                            .fitCenter()
                            .into(holder.fileImage);
                });
            });
        }

        holder.itemView.setOnClickListener(view -> handleClick(fileExplorerItem));
        holder.itemView.setOnClickListener(view -> handleClick(fileExplorerItem));
        holder.itemView.setOnClickListener(view -> handleClick(fileExplorerItem));
    }

    @Override
    public int getItemCount() {
        return fileExplorerListHolder.getFiles().size();
    }

    public static class FileExplorerRecyclerViewHolder extends RecyclerView.ViewHolder {

        private final ImageView fileImage;
        private final TextView fileName;

        public FileExplorerRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            fileImage = itemView.findViewById(R.id.fileImage);
            fileName = itemView.findViewById(R.id.fileName);
        }
    }


    public void handleClick(FileExplorerItem fileExplorerItem) {
        if (fileExplorerItem.isFile()) {
            Intent intent = new Intent();
            intent.putExtra(GlobalConstants.BOOK_PATH, fileExplorerItem.getFilePath());
            Activity activity = (Activity) context;
            activity.setResult(Activity.RESULT_OK, intent);
            activity.finish();
            return;
        }
        notifyItemRangeRemoved(0, fileExplorerListHolder.getFiles().size());
        fileExplorer.getFileExplorerProgressHelper().show();
        Executors.newSingleThreadExecutor().submit(() -> {
            if (fileExplorerItem.getFileName().equals("..")) {
                fileExplorerListHolder.up();
            } else {
                fileExplorerListHolder.openFolder(fileExplorerItem.getFileName());
            }
            fileExplorer.runOnUiThread(() -> {
                fileExplorer.getFileExplorerProgressHelper().hide();
                notifyItemRangeChanged(0, fileExplorerListHolder.getFiles().size());
            });
        });
    }

    public void back() {
        notifyItemRangeRemoved(0, fileExplorerListHolder.getFiles().size());
        if (!fileExplorerListHolder.back()) {
            Activity activity = (Activity) context;
            activity.finish();
            return;
        }
        fileExplorer.getFileExplorerProgressHelper().show();
        Executors.newSingleThreadExecutor().submit(() -> {
            fileExplorerListHolder.load();
            fileExplorer.runOnUiThread(() -> {
                fileExplorer.getFileExplorerProgressHelper().hide();
                notifyItemRangeChanged(0, fileExplorerListHolder.getFiles().size());
            });
        });
    }

}
