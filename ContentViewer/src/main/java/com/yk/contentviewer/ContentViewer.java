package com.yk.contentviewer;

import static com.yk.contentviewer.maincontent.ContentViewerLanguageOptionMenu.prepareLanguageOptionMenu;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.yk.common.context.ActivityResultLauncherWrapper;
import com.yk.common.context.ZoomOutPageTransformer;
import com.yk.common.service.book.BookService;
import com.yk.common.service.book.BookServiceException;
import com.yk.common.service.book.BookServiceHelper;
import com.yk.common.service.dictionary.DictionaryService;
import com.yk.common.service.dictionary.LanguageService;
import com.yk.common.utils.PreferenceHelper;
import com.yk.common.utils.Toaster;
import com.yk.contentviewer.maincontent.ContentViewerItemSelector;
import com.yk.contentviewer.maincontent.ContentViewerOnPageChangeCallback;
import com.yk.contentviewer.maincontent.ContentViewerOnSpeechClickListener;
import com.yk.contentviewer.maincontent.ContentViewerOnTranslationClickListener;
import com.yk.contentviewer.maincontent.ContentViewerPagerAdapter;
import com.yk.contentviewer.maincontent.ContentViewerStateSaver;
import com.yk.contentviewer.maincontent.ContentViewerWebView;
import com.yk.contentviewer.maincontent.ContentViewerWebViewFontRecyclerAdapter;

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

public class ContentViewer extends AppCompatActivity {

    private ActivityResultLauncher<Intent> intentActivityResultLauncher;
    private ContentViewerItemSelector contentViewerItemSelector;
    private final Map<Integer, Timer> timers = new HashMap<>();
    private final Map<Integer, Boolean> menuState = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DictionaryService.getInstance().init();
        LanguageService.getInstance().init(this);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_content_viewer);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ViewPager2 contentViewPager = findViewById(R.id.contentViewerChapterPager);
        // create and set adapter
        ContentViewerPagerAdapter contentViewerPagerAdapter = new ContentViewerPagerAdapter(getSupportFragmentManager(), getLifecycle());
        contentViewPager.setAdapter(contentViewerPagerAdapter);
        // set zoom change animation
        contentViewPager.setPageTransformer(new ZoomOutPageTransformer());
        // move to the end of chapter if it goes to previous page
        try {
            contentViewPager.registerOnPageChangeCallback(new ContentViewerOnPageChangeCallback(this::findViewById, R.id.contentViewerItemContentItem));
        } catch (BookServiceException e) {
            throw new RuntimeException(e);
        }
        try {
            // set current chapter
            contentViewPager.setCurrentItem(BookService.getBookService().getCurrentChapterNumber());
        } catch (BookServiceException serviceException) {
            Toaster.make(getApplicationContext(), "Book can not be loaded", serviceException);
        }

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            findViewById(R.id.contentViewerBackground).setVisibility(View.GONE);
        }

        RecyclerView recyclerViewFont = findViewById(R.id.contentViewerFont);
        recyclerViewFont.setLayoutManager(new GridLayoutManager(this, 3));
        ContentViewerWebViewFontRecyclerAdapter contentViewerWebViewFontRecyclerAdapter =
                new ContentViewerWebViewFontRecyclerAdapter()
                        .setRunnableBeforeAction(() -> cancelTimerForShownElement(R.id.contentViewerFontHolder))
                        .setRunnableAfterAction(() -> startTimerForShownElement(R.id.contentViewerFontHolder));
        recyclerViewFont.setAdapter(contentViewerWebViewFontRecyclerAdapter);
        findViewById(R.id.contentViewerFontLeft).setOnClickListener(v -> {
            cancelTimerForShownElement(R.id.contentViewerFontHolder);
            contentViewerWebViewFontRecyclerAdapter.left();
            startTimerForShownElement(R.id.contentViewerFontHolder);
        });
        findViewById(R.id.contentViewerFontRight).setOnClickListener(v -> {
            cancelTimerForShownElement(R.id.contentViewerFontHolder);
            contentViewerWebViewFontRecyclerAdapter.right();
            startTimerForShownElement(R.id.contentViewerFontHolder);
        });

        // handle table of content load on click
        findViewById(R.id.contentViewerTableOfContent).setOnClickListener(v -> {
            Intent intent = new Intent(this, TableOfContentViewer.class);
            intentActivityResultLauncher.launch(intent);
        });

        // handle on speech click
        ImageView speechImage = findViewById(R.id.contentViewerSoundPlay);
        speechImage.setOnClickListener(new ContentViewerOnSpeechClickListener(this));

        // handle on speech click
        TextView textView = findViewById(R.id.contentViewerTranslatedWord);
        textView.setOnClickListener(new ContentViewerOnTranslationClickListener(this));

        // register activity result
        intentActivityResultLauncher =
                ActivityResultLauncherWrapper
                        .getLauncher(this::registerForActivityResult,
                                new ActivityResultContracts.StartActivityForResult(),
                                intent -> {
                                    try {
                                        contentViewPager.setCurrentItem(BookService.getBookService().getCurrentChapterNumber());
                                    } catch (BookServiceException bookServiceException) {
                                        Toaster.make(getApplicationContext(), "Error on loading", bookServiceException);
                                    }
                                },
                                intent -> {
                                });

        contentViewerItemSelector = new ContentViewerItemSelector(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ContentViewerStateSaver.getInstance()
                .startContentSaver(((ContentViewerWebView) findViewById(R.id.contentViewerItemContentItem)).getVerticalPosition(), true);
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
                contentViewerItemSelector.onTranslationContextCall(menu.findItem(R.id.translateContext));
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
        prepareLanguageOptionMenu(menu, findViewById(R.id.contentViewerHeader),
                List.of(findViewById(R.id.contentViewerTranslatedWord), findViewById(R.id.contentViewerSoundPlay)),
                List.of(findViewById(R.id.contentViewerTableOfContent)));
        return returnValue;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.darkMode) {
            contentViewerItemSelector.onNightModeCall(item);
            menuState.put(R.id.darkMode, item.isChecked());
            return true;
        } else if (itemId == R.id.translateContext) {
            contentViewerItemSelector.onTranslationContextCall(item);
            menuState.put(R.id.translateContext, item.isChecked());
            return true;
        } else if (itemId == R.id.callSizer) {
            try {
                contentViewerItemSelector.onSizerCall(() -> startTimerForShownElement(R.id.contentViewerItemSize), () -> cancelTimerForShownElement(R.id.contentViewerItemSize));
            } catch (BookServiceException e) {
                Toaster.make(getApplicationContext(), "Can not load", e);
            }
            return true;
        } else if (itemId == R.id.textFont) {
            findViewById(R.id.contentViewerFontHolder).setVisibility(View.VISIBLE);
            startTimerForShownElement(R.id.contentViewerFontHolder);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method to show progress bar for time that is defined in timer
     */
    private void startTimerForShownElement(int contentId) {
        timers.put(contentId, new Timer());
        Timer timer = timers.get(contentId);
        if (timer == null)
            return;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    // hide progress bar
                    findViewById(contentId).setVisibility(View.GONE);
                    try {
                        BookServiceHelper.updatePersistenceBook(BookService.getBookService());
                    } catch (BookServiceException bookServiceException) {
                        Toaster.make(getApplicationContext(), "Error on update", bookServiceException);
                    }
                });
            }
        }, 3000);
    }

    /**
     * Method to cancel timer
     */
    private void cancelTimerForShownElement(int contentId) {
        Objects.requireNonNull(timers.get(contentId)).cancel();
        Objects.requireNonNull(timers.get(contentId)).purge();
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
