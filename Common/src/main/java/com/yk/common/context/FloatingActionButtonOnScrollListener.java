package com.yk.common.context;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public final class FloatingActionButtonOnScrollListener extends RecyclerView.OnScrollListener {
    private final List<FloatingActionButton> floatingActionButtons2ShowAndHide;
    private final List<FloatingActionButton> floatingActionButtons2Disappear;

    public FloatingActionButtonOnScrollListener(@NonNull List<FloatingActionButton> floatingActionButtons2ShowAndHide, List<FloatingActionButton> floatingActionButtons2Disappear) {
        this.floatingActionButtons2ShowAndHide = floatingActionButtons2ShowAndHide;
        this.floatingActionButtons2Disappear = floatingActionButtons2Disappear;
    }


    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        if (dy < 0) {
            floatingActionButtons2ShowAndHide.forEach(FloatingActionButton::show);
        } else if (dy > 0) {
            floatingActionButtons2ShowAndHide.forEach(FloatingActionButton::hide);
            floatingActionButtons2Disappear.forEach(floatingActionButton2ShowAndHide -> {
                if (floatingActionButton2ShowAndHide.getVisibility() == View.VISIBLE) {
                    floatingActionButton2ShowAndHide.setVisibility(View.GONE);
                    floatingActionButton2ShowAndHide.animate().translationY(0).setDuration(0);
                }
            });
        }
    }

}
