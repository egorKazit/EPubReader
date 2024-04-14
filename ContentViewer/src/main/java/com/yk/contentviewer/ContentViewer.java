package com.yk.contentviewer;

import static com.yk.contentviewer.maincontent.LanguageOptionMenu.prepareLanguageOptionMenu;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;

import com.yk.common.context.ActivityResultLauncherWrapper;
import com.yk.common.context.ZoomOutPageTransformer;
import com.yk.common.service.book.BookService;
import com.yk.common.service.book.BookServiceException;
import com.yk.common.service.book.BookServiceHelper;
import com.yk.common.service.dictionary.DictionaryService;
import com.yk.common.service.dictionary.LanguageService;
import com.yk.common.utils.PreferenceHelper;
import com.yk.common.utils.Toaster;
import com.yk.contentviewer.databinding.ActivityContentViewerBinding;
import com.yk.contentviewer.maincontent.ItemSelector;
import com.yk.contentviewer.maincontent.OnGestureListener;
import com.yk.contentviewer.maincontent.OnPageChangeCallback;
import com.yk.contentviewer.maincontent.OnSpeechClickListener;
import com.yk.contentviewer.maincontent.OnTranslationClickListener;
import com.yk.contentviewer.maincontent.PagerAdapter;
import com.yk.contentviewer.maincontent.StateSaver;
import com.yk.contentviewer.maincontent.WebView;
import com.yk.contentviewer.maincontent.WebViewFontRecyclerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Activity to open book and provide ability to translate some parts of text
 */

public final class ContentViewer extends AppCompatActivity {

    private ActivityContentViewerBinding binding;
    private ActivityResultLauncher<Intent> intentActivityResultLauncher;
    private ItemSelector itemSelector;
    private final Map<Integer, Timer> timers = new HashMap<>();
    private final Map<Integer, Boolean> menuState = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DictionaryService.getInstance().init();
        LanguageService.getInstance().init(this);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR);

        binding = ActivityContentViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        binding.contentViewerChapterPager.setUserInputEnabled(false);
        // create and set adapter
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), getLifecycle());
        binding.contentViewerChapterPager.setAdapter(pagerAdapter);
        // set zoom change animation
        binding.contentViewerChapterPager.setPageTransformer(new ZoomOutPageTransformer());
        // move to the end of chapter if it goes to previous page
        try {
            binding.contentViewerChapterPager.registerOnPageChangeCallback(new OnPageChangeCallback(this.findViewById(R.id.contentViewerItemContentItem)));
        } catch (BookServiceException e) {
            throw new RuntimeException(e);
        }
        try {
            // set current chapter
            binding.contentViewerChapterPager.setCurrentItem(BookService.getBookService().getCurrentChapterNumber());
        } catch (BookServiceException serviceException) {
            Toaster.make(getApplicationContext(), R.string.error_on_book_loading, serviceException);
        }

        binding.touchOverlay.addOnGestureListener(new OnGestureListener(binding.contentViewerChapterPager));

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            binding.contentViewerBackground.setVisibility(View.GONE);
        }

        binding.contentViewerFont.setLayoutManager(new GridLayoutManager(this, 3));
        WebViewFontRecyclerAdapter webViewFontRecyclerAdapter =
                new WebViewFontRecyclerAdapter()
                        .setRunnableBeforeAction(() -> cancelTimerForShownElement(binding.contentViewerFontHolder))
                        .setRunnableAfterAction(() -> startTimerForShownElement(binding.contentViewerFontHolder));
        binding.contentViewerFont.setAdapter(webViewFontRecyclerAdapter);
        binding.contentViewerFontLeft.setOnClickListener(v -> {
            cancelTimerForShownElement(binding.contentViewerFontHolder);
            webViewFontRecyclerAdapter.left();
            startTimerForShownElement(binding.contentViewerFontHolder);
        });
        binding.contentViewerFontRight.setOnClickListener(v -> {
            cancelTimerForShownElement(binding.contentViewerFontHolder);
            webViewFontRecyclerAdapter.right();
            startTimerForShownElement(binding.contentViewerFontHolder);
        });

        // handle table of content load on click
        binding.contentViewerTableOfContent.setOnClickListener(v -> {
            Intent intent = new Intent(this, TableOfContentViewer.class);
            intentActivityResultLauncher.launch(intent);
        });

        // handle on speech click
        binding.contentViewerSoundPlay.setOnClickListener(new OnSpeechClickListener(this));

        // handle on speech click
        binding.contentViewerTranslatedWord.setOnClickListener(new OnTranslationClickListener(this));

        // register activity result
        intentActivityResultLauncher =
                ActivityResultLauncherWrapper
                        .getLauncher(this::registerForActivityResult,
                                new ActivityResultContracts.StartActivityForResult(),
                                intent -> {
                                    try {
                                        binding.contentViewerChapterPager.setCurrentItem(BookService.getBookService().getCurrentChapterNumber());
                                    } catch (BookServiceException bookServiceException) {
                                        Toaster.make(getApplicationContext(), R.string.error_on_loading, bookServiceException);
                                    }
                                },
                                intent -> {
                                });

        itemSelector = new ItemSelector(this, binding);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StateSaver.getInstance()
                .startContentSaver(((WebView) findViewById(R.id.contentViewerItemContentItem)).getVerticalPosition(), true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.content_viewer_menu, menu);
        if (menuState.containsKey(R.id.darkMode) && menuState.get(R.id.darkMode) != null) {
            var item = menu.findItem(R.id.darkMode);
            item.setChecked(PreferenceHelper.PreferenceHelperHolder.INSTANCE.helper.isNightMode());
        }
        if (menuState.containsKey(R.id.translateContext) && menuState.get(R.id.translateContext) != null) {
            var localValue = menuState.get(R.id.translateContext);
            if (localValue == null)
                localValue = false;
            if (localValue)
                itemSelector.onTranslationContextCall(menu.findItem(R.id.translateContext));
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onMenuOpened(int featureId, @NotNull Menu menu) {
        var darkModeItem = menu.findItem(R.id.darkMode);
        if (darkModeItem != null) {
            darkModeItem.setChecked(PreferenceHelper.PreferenceHelperHolder.INSTANCE.helper.isNightMode());
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean returnValue = super.onPrepareOptionsMenu(menu);
        prepareLanguageOptionMenu(menu, binding.contentViewerHeader,
                List.of(binding.contentViewerTranslatedWord, binding.contentViewerSoundPlay),
                List.of(binding.contentViewerTableOfContent));
        return returnValue;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.darkMode) {
            itemSelector.onNightModeCall(item);
            menuState.put(R.id.darkMode, item.isChecked());
            return true;
        } else if (itemId == R.id.translateContext) {
            itemSelector.onTranslationContextCall(item);
            menuState.put(R.id.translateContext, item.isChecked());
            return true;
        } else if (itemId == R.id.callSizer) {
            try {
                itemSelector.onSizerCall(() -> startTimerForShownElement(binding.contentViewerItemSize),
                        () -> cancelTimerForShownElement(binding.contentViewerItemSize));
            } catch (BookServiceException e) {
                Toaster.make(getApplicationContext(), R.string.error_on_loading, e);
            }
            return true;
        } else if (itemId == R.id.textFont) {
            binding.contentViewerFontHolder.setVisibility(View.VISIBLE);
            startTimerForShownElement(binding.contentViewerFontHolder);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method to show progress bar for time that is defined in timer
     */
    private void startTimerForShownElement(View view) {
        timers.put(view.getId(), new Timer());
        Timer timer = timers.get(view.getId());
        if (timer == null)
            return;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    // hide progress bar
                    view.setVisibility(View.GONE);
                    try {
                        BookServiceHelper.updatePersistenceBook(BookService.getBookService());
                    } catch (BookServiceException bookServiceException) {
                        Toaster.make(getApplicationContext(), R.string.error_on_loading, bookServiceException);
                    }
                });
            }
        }, 3000);
    }

    /**
     * Method to cancel timer
     */
    private void cancelTimerForShownElement(View view) {
        Objects.requireNonNull(timers.get(view.getId())).cancel();
        Objects.requireNonNull(timers.get(view.getId())).purge();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        menuState.forEach((key, value) -> outState.putBoolean(String.valueOf(key), value));
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        var value = savedInstanceState.getBoolean(String.valueOf(R.id.darkMode));
        menuState.put(R.id.darkMode, value);
        value = savedInstanceState.getBoolean(String.valueOf(R.id.translateContext));
        menuState.put(R.id.translateContext, value);
    }

}
