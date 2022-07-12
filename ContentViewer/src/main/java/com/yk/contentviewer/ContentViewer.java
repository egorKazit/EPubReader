package com.yk.contentviewer;

import static com.yk.contentviewer.maincontent.ContentViewerLanguageOptionMenu.prepareLanguageOptionMenu;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.yk.common.launcher.ActivityResultLauncherWrapper;
import com.yk.common.model.book.BookService;
import com.yk.common.model.book.BookServiceException;
import com.yk.common.model.dictionary.DictionaryPool;
import com.yk.common.utils.Toaster;
import com.yk.common.utils.ZoomOutPageTransformer;
import com.yk.contentviewer.maincontent.ContentViewerItemSelector;
import com.yk.contentviewer.maincontent.ContentViewerOnPageChangeCallback;
import com.yk.contentviewer.maincontent.ContentViewerOnSpeechClickListener;
import com.yk.contentviewer.maincontent.ContentViewerPagerAdapter;
import com.yk.contentviewer.maincontent.ContentViewerStateSaver;
import com.yk.contentviewer.maincontent.ContentViewerWevView;

import java.util.Timer;
import java.util.TimerTask;

import lombok.SneakyThrows;

/**
 * Activity to open book and provide ability to translate some parts of text
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class ContentViewer extends AppCompatActivity {

    private ActivityResultLauncher<Intent> intentActivityResultLauncher;
    private ContentViewerItemSelector contentViewerItemSelector;
    private Timer timer;

    @SneakyThrows
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DictionaryPool.init();
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_content_viewer);

        ViewPager2 contentViewPager = findViewById(R.id.contentViewerChapterPager);
        // create and set adapter
        ContentViewerPagerAdapter contentViewerPagerAdapter = new ContentViewerPagerAdapter(getSupportFragmentManager(), getLifecycle());
        contentViewPager.setAdapter(contentViewerPagerAdapter);
        // set zoom change animation
        contentViewPager.setPageTransformer(new ZoomOutPageTransformer());
        // move to the end of chapter if do to previous page
        contentViewPager.registerOnPageChangeCallback(new ContentViewerOnPageChangeCallback(this::findViewById, R.id.contentViewerItemContentItem));
        try {
            // set current chapter
            contentViewPager.setCurrentItem(BookService.getBookService().getCurrentChapter());
        } catch (BookServiceException serviceException) {
            Toaster.make(getApplicationContext(), "Book can not be loaded", serviceException);
        }

        // handle table of content load on click
        findViewById(R.id.contentViewerTableOfContent).setOnClickListener(v -> {
            Intent intent = new Intent(this, TableOfContentViewer.class);
            intentActivityResultLauncher.launch(intent);
        });

        // handle on speech click
        ImageView speechImage = findViewById(R.id.contentViewerSoundPlay);
        speechImage.setOnClickListener(new ContentViewerOnSpeechClickListener(this));

        // register activity result
        intentActivityResultLauncher =
                ActivityResultLauncherWrapper
                        .getLauncher(this::registerForActivityResult,
                                new ActivityResultContracts.StartActivityForResult(),
                                intent -> {
                                    try {
                                        contentViewPager.setCurrentItem(BookService.getBookService().getCurrentChapter());
                                    } catch (BookServiceException bookServiceException) {
                                        Toaster.make(getApplicationContext(), "Error on loading", bookServiceException);
                                    }
                                },
                                intent -> {
                                });

        contentViewerItemSelector = new ContentViewerItemSelector(this);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ContentViewerStateSaver.getInstance()
                .startContentSaver(((ContentViewerWevView) findViewById(R.id.contentViewerItemContentItem)).getScrollPositionY(), true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_option_content_viewer, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean returnValue = super.onPrepareOptionsMenu(menu);
        prepareLanguageOptionMenu(menu, findViewById(R.id.contentViewerTranslatedWord));
        return returnValue;
    }

    @SneakyThrows
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.translateContext) {
            return contentViewerItemSelector.onTranslationContextCall(item);
        } else if (itemId == R.id.callSizer) {
            return contentViewerItemSelector.onSizerCall(this::showProgressBar, this::cancelTimerForProgressBar);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method to show progress bar for time that is defined in timer
     */
    private void showProgressBar() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    // hide progress bar
                    findViewById(R.id.contentViewerItemSize).setVisibility(View.GONE);
                    try {
                        BookService.getBookService().updatePersistenceBook();
                    } catch (BookServiceException bookServiceException) {
                        Toaster.make(getApplicationContext(), "Error on loading", bookServiceException);
                    }
                });
            }
        }, 3000);
    }

    /**
     * Method to cancel timer
     */
    private void cancelTimerForProgressBar() {
        timer.cancel();
        timer.purge();
    }


}
