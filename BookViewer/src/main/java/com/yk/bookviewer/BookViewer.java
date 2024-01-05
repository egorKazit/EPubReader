package com.yk.bookviewer;

import android.os.Build;
import android.os.Bundle;
import android.view.Menu;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.yk.bookviewer.databinding.ActivityBookViewerBinding;
import com.yk.common.service.learning.LearningOperator;
import com.yk.common.utils.PreferenceHelper;

@RequiresApi(api = Build.VERSION_CODES.S)
public class BookViewer extends AppCompatActivity {

    @SuppressWarnings("all")
    private ActivityBookViewerBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBookViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dictionary, R.id.navigation_settings)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_book_viewer);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        new LearningOperator(this).startLearning();
        if (PreferenceHelper.PreferenceHelperHolder.INSTANCE.helper.isNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp();
    }



//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
////        if(navController.getCurrentDestination())
//        getMenuInflater().inflate(R.menu.search_menu, menu);
////        if (menuState.containsKey(com.yk.contentviewer.R.id.darkMode) && menuState.get(com.yk.contentviewer.R.id.darkMode) != null) {
////            var item = menu.findItem(com.yk.contentviewer.R.id.darkMode);
////            item.setChecked(PreferenceHelper.PreferenceHelperHolder.INSTANCE.helper.isNightMode());
////        }
////        if (menuState.containsKey(com.yk.contentviewer.R.id.translateContext) && menuState.get(com.yk.contentviewer.R.id.translateContext) != null) {
////            var localValue = menuState.get(com.yk.contentviewer.R.id.translateContext);
////            if (localValue == null)
////                localValue = false;
////            if (localValue)
////                contentViewerItemSelector.onTranslationContextCall(menu.findItem(com.yk.contentviewer.R.id.translateContext));
////        }
//        return true;
//    }

}