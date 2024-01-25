package com.yk.common.context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FloatingActionButtonOnScrollListener extends RecyclerView.OnScrollListener {
    private final FloatingActionButton floatingActionButton;
    private final int floatingActionButtonHeight;
    private ScrollState scrollState = null;

    public FloatingActionButtonOnScrollListener(@NonNull BottomNavigationView bottomNavigationView,
                                                @NonNull FloatingActionButton floatingActionButton) {
        this.floatingActionButton = floatingActionButton;
        this.floatingActionButtonHeight = bottomNavigationView.getHeight() + floatingActionButton.getHeight();
    }


    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        if (dy < 0) {
            floatingActionButton.show();

//                floatingActionButton.animate().translationY(0).setDuration(200).setStartDelay(0).start();
        } else if (dy > 0) {
            floatingActionButton.hide();
// floatingActionButton.animate().translationY(floatingActionButtonHeight).setDuration(200).setStartDelay(0).start();
        }
    }

    private enum ScrollState {
        UP, DOWN
    }

}
