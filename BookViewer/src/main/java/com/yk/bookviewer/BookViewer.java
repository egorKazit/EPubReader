package com.yk.bookviewer;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.yk.bookviewer.databinding.ActivityBookViewerBinding;
import com.yk.common.learning.LearningOperator;
import com.yk.common.utils.PreferenceHelper;

@RequiresApi(api = Build.VERSION_CODES.S)
public class BookViewer extends AppCompatActivity {

    private ActivityBookViewerBinding binding;
    private NavController navController;

    @Override
    protected void onStart() {
        super.onStart();
    }

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
        if (PreferenceHelper.Instance.INSTANCE.helper.isNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp();
    }
}