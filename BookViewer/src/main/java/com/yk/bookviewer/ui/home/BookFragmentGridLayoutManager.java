package com.yk.bookviewer.ui.home;

import android.content.Context;
import android.view.Surface;
import android.view.WindowManager;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Extension of GridLayoutManager.
 * It sets layout span based on rotation state
 */
class BookFragmentGridLayoutManager extends GridLayoutManager {
    private final Context context;

    BookFragmentGridLayoutManager(Context context) {
        super(context, 2, LinearLayoutManager.VERTICAL, false);
        this.context = context;
    }

    @Override
    public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
        // get rotation
        final int rotation = ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        // set 2x2 for rotation 0/180 or 4x1 for rotation 90/270
        switch (rotation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                setSpanCount(2);
                lp.width = getWidth() / 2;
                lp.height = getHeight() / 2;
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
            default:
                setSpanCount(4);
                lp.width = getWidth() / 4;
                lp.height = getHeight();
        }
        return true;

    }
}
