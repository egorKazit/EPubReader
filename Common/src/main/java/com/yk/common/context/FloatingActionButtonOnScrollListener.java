package com.yk.common.context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FloatingActionButtonOnScrollListener extends RecyclerView.OnScrollListener {
    private final FloatingActionButton floatingActionButton;

    public FloatingActionButtonOnScrollListener(@NonNull FloatingActionButton floatingActionButton) {
        this.floatingActionButton = floatingActionButton;
    }


    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        if (dy < 0) {
            floatingActionButton.show();
        } else if (dy > 0) {
            floatingActionButton.hide();
        }
    }

}
