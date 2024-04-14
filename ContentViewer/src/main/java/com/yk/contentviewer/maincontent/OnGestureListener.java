package com.yk.contentviewer.maincontent;

import android.gesture.GestureOverlayView;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.yk.contentviewer.R;

import lombok.Getter;

public class OnGestureListener implements GestureOverlayView.OnGestureListener {

    private final ViewPager2 contentViewPager;

    private float verticalPosition = 0;
    private float initialVerticalPosition = 0;
    private SwipeDirection direction = SwipeDirection.NONE;
    private boolean started = false;
    @Getter
    private static SwipeDirection lastDirection = SwipeDirection.NONE;

    public OnGestureListener(ViewPager2 contentViewPager) {
        this.contentViewPager = contentViewPager;
    }


    @Override
    public void onGestureStarted(GestureOverlayView overlay, @NonNull MotionEvent event) {
        verticalPosition = event.getY();
        started = false;

    }

    @Override
    public void onGesture(GestureOverlayView overlay, MotionEvent event) {

        // set direction if none
        // idea is only one direction is allowed
        if (direction == SwipeDirection.NONE) {
            if ((event.getY() - verticalPosition) > 0) {
                lastDirection = direction = SwipeDirection.DOWN;
            } else {
                lastDirection = direction = SwipeDirection.UP;
            }
        }

        if (!isChangeChapterNeeded() && !started) {
            lastDirection = direction = SwipeDirection.NOT_NEEDED;
        }

        if (!started) {
            started = true;
        }

        var stopDrag = (direction == SwipeDirection.UP && (event.getY() - initialVerticalPosition) > 0)
                || (direction == SwipeDirection.DOWN && (event.getY() - initialVerticalPosition) < 0)
                || Math.abs(event.getY() - initialVerticalPosition) >= contentViewPager.getHeight();

        if (stopDrag) {
            contentViewPager.endFakeDrag();
        } else {
            contentViewPager.fakeDragBy(event.getY() - verticalPosition);
            verticalPosition = event.getY();
        }

        if (!contentViewPager.isFakeDragging()) {
            if (isChangeChapterNeeded()) {
                initialVerticalPosition = verticalPosition = event.getY();
                contentViewPager.beginFakeDrag();
            }
        }
    }

    @Override
    public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
        contentViewPager.endFakeDrag();
        initialVerticalPosition = verticalPosition = 0;
        direction = SwipeDirection.NONE;
    }

    @Override
    public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {
        contentViewPager.endFakeDrag();
        initialVerticalPosition = verticalPosition = 0;
        direction = SwipeDirection.NONE;
    }

    private boolean isChangeChapterNeeded() {
        WebView webView = contentViewPager.requireViewById(R.id.contentViewerItemContentItem);
        return (direction == SwipeDirection.DOWN && !webView.canScrollVertically(-1)) ||
                (direction == SwipeDirection.UP && !webView.canScrollVertically(1));
    }

    enum SwipeDirection {
        DOWN, UP, NONE, NOT_NEEDED
    }

}
