package com.yk.fileexplorer;

import android.app.Activity;
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

public class FileExplorerRecyclerViewAdapter extends RecyclerView.Adapter<FileExplorerRecyclerViewAdapter.FileExplorerRecyclerViewHolder> {

    private final FileExplorerListHolder fileExplorerListHolder;
    private FileExplorer fileExplorer;

    FileExplorerRecyclerViewAdapter(FileExplorerListHolder fileExplorerListHolder) {
        this.fileExplorerListHolder = fileExplorerListHolder;
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
        RequestBuilder<Drawable> drawableRequestBuilder;
        if (!fileExplorerItem.isFile()) {
            drawableRequestBuilder = glide.load(R.drawable.ic_folder_foreground);
        } else {
            if (fileExplorerItem.getTitle() != null)
                holder.fileName.setText(fileExplorerItem.getTitle());
            if (fileExplorerItem.getBitmap() != null)
                drawableRequestBuilder = glide.load(fileExplorerItem.getBitmap());
            else drawableRequestBuilder = glide.load(R.drawable.ic_no_cover_foreground);
        }
        drawableRequestBuilder.centerCrop()
                .fitCenter()
                .into(holder.fileImage);

        holder.itemView.setOnClickListener(view -> handleClick(view, fileExplorerItem));
        holder.itemView.setOnClickListener(view -> handleClick(view, fileExplorerItem));
        holder.itemView.setOnClickListener(view -> handleClick(view, fileExplorerItem));
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

    
    public void handleClick(View view, FileExplorerItem fileExplorerItem) {
        if (fileExplorerItem.isFile()) {
            Intent intent = new Intent();
            intent.putExtra(GlobalConstants.BOOK_PATH, fileExplorerItem.getFilePath());
            Activity activity = (Activity) view.getContext();
            activity.setResult(Activity.RESULT_OK, intent);
            activity.finish();
            return;
        }
        notifyItemRangeRemoved(0, fileExplorerListHolder.getFiles().size());
        fileExplorer.getFileExplorerProgressHelper().show();
        new Thread(() -> {
            if (fileExplorerItem.getFileName().equals("..")) {
                fileExplorerListHolder.up();
            } else {
                fileExplorerListHolder.openFolder(fileExplorerItem.getFileName());
            }
            fileExplorer.runOnUiThread(() -> {
                fileExplorer.getFileExplorerProgressHelper().hide();
                notifyItemRangeChanged(0, fileExplorerListHolder.getFiles().size());
            });
        }).start();

    }

}
