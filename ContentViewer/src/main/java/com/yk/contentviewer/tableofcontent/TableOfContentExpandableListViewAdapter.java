package com.yk.contentviewer.tableofcontent;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.yk.common.model.book.BookService;
import com.yk.common.model.book.BookServiceException;
import com.yk.common.model.book.TableOfContent;
import com.yk.contentviewer.R;

@RequiresApi(api = Build.VERSION_CODES.S)
public class TableOfContentExpandableListViewAdapter extends BaseExpandableListAdapter {

    private final TableOfContent tableOfContent;
    private TableOfContent.Chapter chapter;

    public TableOfContentExpandableListViewAdapter(TableOfContent tableOfContent) {
        this.tableOfContent = tableOfContent;
    }

    @Override
    public int getGroupCount() {
        return tableOfContent != null ? tableOfContent.getChapterTree().size() : chapter.getSubChapters().size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return tableOfContent != null ? tableOfContent.getChapterTree().get(groupPosition).getSubChapters().size()
                : chapter.getSubChapters().get(0).getSubChapters().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        Log.i("position for group", "" + groupPosition);
        return tableOfContent != null ? tableOfContent.getChapterTree().get(groupPosition) :
                chapter.getSubChapters().get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return tableOfContent != null ? tableOfContent.getChapterTree().get(groupPosition).getSubChapters().get(childPosition) :
                chapter.getSubChapters().get(groupPosition).getSubChapters().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        TableOfContent.Chapter chapter = (TableOfContent.Chapter) getGroup(groupPosition);
        View patentContentView = LayoutInflater.from(parent.getContext())
                .inflate(getChildrenCount(groupPosition) == 0 ?
                        R.layout.item_parent_chapter_of_content_without_child :
                        R.layout.item_parent_chapter_of_content_with_child, parent, false);
        ((TextView) patentContentView.findViewById(R.id.parentChapterOfContentName)).setText(chapter.getChapterName());
        if (getChildrenCount(groupPosition) == 0) {
            setOnClickListener(groupPosition, 0, patentContentView, parent, false);
        }
        this.notifyDataSetChanged();
        return patentContentView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        TableOfContent.Chapter chapter = (TableOfContent.Chapter) getChild(groupPosition, childPosition);
        View childContentView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chapter_of_content, parent, false);
        ((TextView) childContentView.findViewById(R.id.parentChapterOfContentName)).setText(chapter.getChapterName());
        setOnClickListener(groupPosition, childPosition, childContentView, parent, true);
        this.notifyDataSetChanged();
        return childContentView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return tableOfContent != null ? tableOfContent.getChapterTree().get(groupPosition) != null &&
                tableOfContent.getChapterTree().get(groupPosition).getSubChapters().get(childPosition) != null :
                chapter.getSubChapters().get(groupPosition).getSubChapters() != null &&
                        chapter.getSubChapters().get(groupPosition).getSubChapters().get(childPosition) != null;
    }

    private void setOnClickListener(int groupPosition, int childPosition, View contentView, ViewGroup parent, boolean isForChild) {
        contentView.setOnClickListener(v -> {
            TableOfContent.Chapter chapterToSelect;
            if (isForChild)
                chapterToSelect = (TableOfContent.Chapter) getChild(groupPosition, childPosition);
            else
                chapterToSelect = (TableOfContent.Chapter) getGroup(groupPosition);
            Intent intent = new Intent();
            try {
                BookService.getBookService().setCurrentChapter(chapterToSelect.getSpineRefId());
            } catch (BookServiceException bookServiceException) {
                bookServiceException.printStackTrace();
            }
            try {
                BookService.getBookService().setCurrentChapterPosition(0);
            } catch (BookServiceException bookServiceException) {
                bookServiceException.printStackTrace();
            }
            ((Activity) parent.getContext()).setResult(Activity.RESULT_OK, intent);
            ((Activity) parent.getContext()).finish();
        });
    }

}
