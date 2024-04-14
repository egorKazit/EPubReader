package com.yk.remoteexplorer;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;

import com.yk.remoteexplorer.databinding.ActivityRemoteExplorerBinding;

public class RemoteExplorer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(ActivityRemoteExplorerBinding.inflate(getLayoutInflater()).getRoot());

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_remote_explorer);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        new AppBarConfiguration.Builder(R.id.navigation_books).build();
        navController.navigate(R.id.navigation_books);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                var currentDestination = navController.getCurrentDestination();
                if (currentDestination == null) return;
                if (currentDestination.getId() != R.id.navigation_details) {
                    finish();
                } else {
                    navController.navigateUp();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}