package com.yk.contentviewer.tableofcontent;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yk.common.model.book.TableOfContent;
import com.yk.common.service.book.BookService;
import com.yk.common.service.book.BookServiceException;
import com.yk.contentviewer.R;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TableOfContentNestedListAdapter extends RecyclerView.Adapter<TableOfContentNestedListAdapter.NestedViewHolder> {

    private static final int ITEM_WITH_CHILDREN = 1;
    private static final int ITEM_WITHOUT_CHILDREN = 0;

    private final int level;
    private LinkedList<TableOfContent.Chapter> chapterTree;
    private List<Boolean> isExpanded;

    public TableOfContentNestedListAdapter(LinkedList<TableOfContent.Chapter> chapterTree, int level) {
        this.level = level;
        setChapterTree(chapterTree);
    }

    @NonNull
    @Override
    public NestedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        var nestedView = LayoutInflater.from(parent.getContext()).inflate(
                viewType == ITEM_WITH_CHILDREN ? R.layout.item_chapter_of_content_with_children :
                        R.layout.item_chapter_of_content_without_children, parent, false);
        return new NestedViewHolder(nestedView).asClickable(viewType == ITEM_WITHOUT_CHILDREN);
    }

    @Override
    public void onBindViewHolder(@NonNull NestedViewHolder holder, int position) {
        holder.chapterName.setText(chapterTree.get(position).getChapterName());
        var subChaptersAdapter = new TableOfContentNestedListAdapter(new LinkedList<>(), level + 1);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(holder.subChapters.getContext());
        holder.subChapters.setLayoutManager(linearLayoutManager);
        holder.subChapters.setAdapter(subChaptersAdapter);
        init(subChaptersAdapter, position);
        holder.chapterName.setOnClickListener(v -> expandOrCollapse(subChaptersAdapter, position));

        var width = ((Activity) holder.itemView.getContext()).getWindowManager()
                .getCurrentWindowMetrics().getBounds().width();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width * (100 - 5 * level) / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        holder.itemView.setLayoutParams(params);
        if (holder.isClickable) {
            setOnClickListener(position, holder.itemView);
            setOnClickListener(position, holder.chapterName);
        }
    }

    @Override
    public int getItemCount() {
        return chapterTree.size();
    }

    @Override
    public int getItemViewType(int position) {
        return !chapterTree.get(position).getSubChapters().isEmpty() ? ITEM_WITH_CHILDREN : ITEM_WITHOUT_CHILDREN;
    }

    private void setChapterTree(LinkedList<TableOfContent.Chapter> chapterTree) {
        this.chapterTree = chapterTree;
        this.isExpanded = (this.isExpanded == null || this.isExpanded.isEmpty()) && !this.chapterTree.isEmpty() ?
                IntStream.range(0, chapterTree.size()).mapToObj(operand -> false).collect(Collectors.toList()) : this.isExpanded;
    }

    private void expandOrCollapse(TableOfContentNestedListAdapter subChaptersAdapter, int position) {
        if (isExpanded.size() <= position) return;
        if (isExpanded.get(position)) {
            isExpanded.set(position, false);
            subChaptersAdapter.setChapterTree(new LinkedList<>());
            subChaptersAdapter.notifyItemRangeRemoved(0, chapterTree.get(position).getSubChapters().size());
        } else {
            isExpanded.set(position, true);
            subChaptersAdapter.setChapterTree(chapterTree.get(position).getSubChapters());
            subChaptersAdapter.notifyItemRangeInserted(0, chapterTree.get(position).getSubChapters().size());
        }
    }

    private void init(TableOfContentNestedListAdapter subChaptersAdapter, int position) {
        if (isExpanded.size() <= position) return;
        if (isExpanded.get(position)) {
            subChaptersAdapter.setChapterTree(chapterTree.get(position).getSubChapters());
            subChaptersAdapter.notifyItemRangeInserted(0, chapterTree.get(position).getSubChapters().size());
        } else {
            subChaptersAdapter.setChapterTree(new LinkedList<>());
            subChaptersAdapter.notifyItemRangeRemoved(0, chapterTree.get(position).getSubChapters().size());
        }
    }

    private void setOnClickListener(int position, View contentView) {
        contentView.setOnClickListener(v -> {
            Intent intent = new Intent();
            try {
                BookService.getBookService().setCurrentChapterNumber(chapterTree.get(position).getSpineRefId());
            } catch (BookServiceException bookServiceException) {
                Log.e(this.getClass().getName(), "Error at table of content loading: " + bookServiceException.getMessage());
            }
            try {
                BookService.getBookService().setCurrentChapterPosition(0);
            } catch (BookServiceException bookServiceException) {
                Log.e(this.getClass().getName(), "Error at table of content loading: " + bookServiceException.getMessage());
            }
            ((Activity) contentView.getContext()).setResult(Activity.RESULT_OK, intent);
            ((Activity) contentView.getContext()).finish();
        });
    }

    public static class NestedViewHolder extends RecyclerView.ViewHolder {

        private final TextView chapterName;
        private final RecyclerView subChapters;
        private boolean isClickable;

        public NestedViewHolder(@NonNull View itemView) {
            super(itemView);
            chapterName = itemView.findViewById(R.id.chapterName);
            subChapters = itemView.findViewById(R.id.subChapters);
        }

        private NestedViewHolder asClickable(boolean isClickable) {
            this.isClickable = isClickable;
            return this;
        }

    }

}
