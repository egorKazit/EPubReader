package com.yk.bookviewer.ui.dictionary;

import android.content.Context;

import androidx.appcompat.widget.SearchView;

import com.yk.common.model.dictionary.Dictionary;
import com.yk.common.service.dictionary.DictionaryService;
import com.yk.common.utils.Toaster;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import lombok.Getter;

public class DictionaryFragmentSearchMenuHandler implements SearchView.OnQueryTextListener {

    @Getter
    private static String searchPhrase;

    private final Context context;
    private final DictionaryFragmentRecyclerViewAdapter adapter;
    private Future<List<Dictionary>> futureDictionaries;

    public DictionaryFragmentSearchMenuHandler(Context context, DictionaryFragmentRecyclerViewAdapter adapter) {
        this.context = context;
        this.adapter = adapter;
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        updateDictionariesBasedOnSearch(query);
        return false;
    }


    @Override
    public boolean onQueryTextChange(String newText) {
        updateDictionariesBasedOnSearch(newText);
        return false;
    }


    private void updateDictionariesBasedOnSearch(String newText) {
        if (futureDictionaries != null && !futureDictionaries.isDone() && !futureDictionaries.isCancelled())
            futureDictionaries.cancel(true);
        try {
            var initialDictionaries = adapter.getDictionaries();
            futureDictionaries = Executors.newSingleThreadExecutor().submit(() -> DictionaryService.getInstance().searchDictionaries(String.format("%s%%", newText)));
            var dictionaries = futureDictionaries.get();
            if (!dictionaries.equals(initialDictionaries)) {
                adapter.notifyItemRangeRemoved(0, adapter.getItemCount());
                adapter.setDictionaries(dictionaries);
                adapter.notifyItemRangeInserted(0, dictionaries.size());
            }
        } catch (ExecutionException | InterruptedException exception) {
            Toaster.make(context, "Can not load translations", exception);
            adapter.setDictionaries(new ArrayList<>());
        }
        searchPhrase = newText;
    }

}
