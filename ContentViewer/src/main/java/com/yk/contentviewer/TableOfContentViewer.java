package com.yk.contentviewer;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yk.common.service.book.BookService;
import com.yk.contentviewer.databinding.ActivityTableOfContentListBinding;
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

        ActivityTableOfContentListBinding binding = ActivityTableOfContentListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // fill recycler view with data
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext());
        binding.subChapters.setLayoutManager(linearLayoutManager);
        binding.subChapters.setAdapter(new NestedListAdapter(BookService.getBookService().getTableOfContent().getChapterTree(), 0));
        linearLayoutManager.scrollToPosition(BookService.getBookService().getRootChapterPosition());
    }

}
