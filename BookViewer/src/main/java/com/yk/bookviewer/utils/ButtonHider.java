package com.yk.bookviewer.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yk.bookviewer.R;

import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class ButtonHider {

    public static void showOrHideButtons(Fragment fragment) {
        var localLibraryActionButton = (FloatingActionButton) fragment.requireView().getRootView().findViewById(R.id.libraryLocal);
        var remoteLibraryActionButton = (FloatingActionButton) fragment.requireView().getRootView().findViewById(R.id.libraryRemote);
        if (localLibraryActionButton.getVisibility() == View.GONE) {
            localLibraryActionButton.setVisibility(View.VISIBLE);
            localLibraryActionButton.animate().translationY(-150).setDuration(300);
            remoteLibraryActionButton.animate().translationY(-290).setDuration(300);
            remoteLibraryActionButton.setVisibility(View.VISIBLE);
        } else {
            Stream.of(localLibraryActionButton, remoteLibraryActionButton).forEach(button ->
                    button.animate().translationY(0).setDuration(300).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            if (button.getTranslationY() == 0)
                                button.setVisibility(View.GONE);
                        }
                    }));
        }
    }

    public static void hide(Fragment fragment) {
        var libraryRemoteActionButton = (FloatingActionButton) fragment.requireView().getRootView().findViewById(R.id.libraryRemote);
        if (libraryRemoteActionButton.getVisibility() == View.VISIBLE) {
            libraryRemoteActionButton.setVisibility(View.GONE);
            libraryRemoteActionButton.animate().translationY(0).setDuration(0);
        }
        var libraryLocalActionButton = (FloatingActionButton) fragment.requireView().getRootView().findViewById(R.id.libraryLocal);
        if (libraryLocalActionButton.getVisibility() == View.VISIBLE) {
            libraryLocalActionButton.setVisibility(View.GONE);
            libraryLocalActionButton.animate().translationY(0).setDuration(0);
        }
    }

}
