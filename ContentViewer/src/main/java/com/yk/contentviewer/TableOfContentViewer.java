package com.yk.contentviewer;

import android.os.Build;
import android.os.Bundle;
import android.widget.ExpandableListView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.yk.common.model.book.BookService;
import com.yk.common.model.book.BookServiceException;
import com.yk.common.utils.Toaster;
import com.yk.contentviewer.tableofcontent.TableOfContentExpandableListViewAdapter;

/**
 * Table of content viewer
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class TableOfContentViewer extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_of_content);
        // create and set adapter
        ExpandableListView expandableListView = findViewById(R.id.tableOfContent);
        try {
            expandableListView.setAdapter(new TableOfContentExpandableListViewAdapter(BookService.getBookService().getTableOfContent()));
        } catch (BookServiceException bookServiceException) {
            Toaster.make(getApplicationContext(), "Error on table of content loading", bookServiceException);
        }
        // hide expand indicator
        expandableListView.setIndicatorBounds(0, 0);
    }
}
