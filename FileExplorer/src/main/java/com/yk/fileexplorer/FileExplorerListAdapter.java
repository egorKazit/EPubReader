package com.yk.fileexplorer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.S)
public class FileExplorerListAdapter extends ArrayAdapter<FileExplorerItem> {

    private final Context context;
    private List<FileExplorerItem> list;

    public FileExplorerListAdapter(@NonNull Context context, @SuppressLint("SupportAnnotationUsage") @LayoutRes List<FileExplorerItem> list) {
        super(context, 0, list);
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View fileExplorerItemView = convertView;
        if (fileExplorerItemView == null)
            fileExplorerItemView = LayoutInflater.from(context).inflate(R.layout.file_explorer_item, parent, false);

        FileExplorerItem fileExplorerItem = list.get(position);
        TextView fileName = fileExplorerItemView.findViewById(R.id.fileName);
        ImageView fileImage = fileExplorerItemView.findViewById(R.id.fileImage);
        if (!fileExplorerItem.isFile) {
            fileName.setText(fileExplorerItem.filename);
            Glide.with(fileImage.getContext())
                    .load(R.drawable.ic_folder_foreground)
                    .centerCrop()
                    .fitCenter()
                    .into(fileImage);
        } else {
            fileName.setText(fileExplorerItem.filename);
            new FileExplorerItemImageInflater(
                    fileExplorerItem.filePath, fileExplorerItemView.findViewById(R.id.fileName),
                    fileExplorerItemView.findViewById(R.id.fileImage)).startInflating();

        }
        return fileExplorerItemView;
    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    void updateList(List<FileExplorerItem> list) {
        this.list = list;
    }

}
