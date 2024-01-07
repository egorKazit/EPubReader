package com.yk.bookviewer.ui.dictionary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yk.bookviewer.R;
import com.yk.bookviewer.databinding.FragmentDictionaryBinding;
import com.yk.common.model.dictionary.Dictionary;
import com.yk.common.service.dictionary.DictionaryService;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import lombok.Getter;
import lombok.SneakyThrows;

/**
 * Dictionary fragment.
 * Contains all translated words which has been saved on disk
 */

public class DictionaryFragment extends Fragment {

    private FragmentDictionaryBinding binding;
    private static String searchPhrase;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDictionaryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // fill recycler view with data
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.dictionaryList.setLayoutManager(linearLayoutManager);
        var dictionaryFragmentRecyclerViewAdapter = new DictionaryFragmentRecyclerViewAdapter(this);
        if (searchPhrase != null && !searchPhrase.isEmpty()) {
            var futureDictionaries = Executors.newSingleThreadExecutor().submit(() -> DictionaryService.getInstance().searchDictionaries(searchPhrase));
            try {
                dictionaryFragmentRecyclerViewAdapter.setDictionaries(futureDictionaries.get());
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        binding.dictionaryList.setAdapter(dictionaryFragmentRecyclerViewAdapter);
        // add divider between items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(),
                linearLayoutManager.getOrientation());
        binding.dictionaryList.addItemDecoration(dividerItemDecoration);

        var menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.search_menu, menu);
                MenuItem dictionarySearchViewItem = menu.findItem(R.id.dictionarySearch);
                var dictionarySearch = (SearchView) dictionarySearchViewItem.getActionView();
                assert dictionarySearch != null;
                dictionarySearch.setOnQueryTextListener(new DictionaryFragmentSearchMenuHandler(requireContext(), dictionaryFragmentRecyclerViewAdapter));
                if (searchPhrase != null && !searchPhrase.isEmpty()) {
                    dictionarySearch.onActionViewExpanded();
                    dictionarySearch.setQuery(searchPhrase, true);
                }
            }

            @Override
            public void onPrepareMenu(@NonNull Menu menu) {
                MenuProvider.super.onPrepareMenu(menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }

            @Override
            public void onMenuClosed(@NonNull Menu menu) {
                MenuProvider.super.onMenuClosed(menu);
            }
        }, this.getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        searchPhrase = DictionaryFragmentSearchMenuHandler.getSearchPhrase();
    }
}