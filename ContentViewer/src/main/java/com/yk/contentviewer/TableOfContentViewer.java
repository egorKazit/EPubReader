package com.yk.contentviewer;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yk.common.service.book.BookService;
import com.yk.contentviewer.tableofcontent.NestedListAdapter;

import lombok.SneakyThrows;

/**
 * Table of content viewer
 */

public final class TableOfContentViewer extends AppCompatActivity {

    @SneakyThrows
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_of_content_list);
        RecyclerView recyclerView = findViewById(R.id.subChapters);
        // fill recycler view with data
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(new NestedListAdapter(BookService.getBookService().getTableOfContent().getChapterTree(), 0));
        linearLayoutManager.scrollToPosition(BookService.getBookService().getCurrentChapterNumber());
    }
}
