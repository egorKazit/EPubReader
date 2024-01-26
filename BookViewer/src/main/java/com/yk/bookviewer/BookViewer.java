package com.yk.bookviewer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yk.bookviewer.databinding.ActivityBookViewerBinding;
import com.yk.common.service.learning.LearningOperator;
import com.yk.common.utils.PreferenceHelper;

import java.util.Objects;

public class BookViewer extends AppCompatActivity {

    @SuppressWarnings("FieldCanBeLocal")
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
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_book_viewer);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dictionary, R.id.navigation_settings)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        new LearningOperator(this).startLearning();
        if (PreferenceHelper.PreferenceHelperHolder.INSTANCE.helper.isNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        var floatingActionButton = (FloatingActionButton) findViewById(R.id.library);
        floatingActionButton.setOnClickListener(v -> {
            if (!Objects.equals(Objects.requireNonNull(navController.getCurrentDestination()).getId(), R.id.navigation_home))
                navController.navigate(R.id.navigation_home);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp();
    }

}