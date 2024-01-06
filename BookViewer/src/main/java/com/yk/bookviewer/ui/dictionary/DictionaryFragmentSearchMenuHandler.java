package com.yk.bookviewer.ui.dictionary;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;

import com.yk.common.model.dictionary.Dictionary;
import com.yk.common.service.dictionary.DictionaryService;
import com.yk.common.utils.Toaster;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DictionaryFragmentSearchMenuHandler implements SearchView.OnQueryTextListener {

    private final Context context;
    private final DictionaryFragmentRecyclerViewAdapter adapter;
    private Future<List<Dictionary>> futureDictionaries;

    public DictionaryFragmentSearchMenuHandler(Context context, DictionaryFragmentRecyclerViewAdapter adapter) {
        this.context = context;
        this.adapter = adapter;
    }


    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public boolean onQueryTextSubmit(String query) {
        updateDictionariesBasedOnSearch(query);
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public boolean onQueryTextChange(String newText) {
        updateDictionariesBasedOnSearch(newText);
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void updateDictionariesBasedOnSearch(String newText) {
        if (futureDictionaries != null && !futureDictionaries.isDone() && !futureDictionaries.isCancelled())
            futureDictionaries.cancel(true);
        adapter.notifyItemRangeRemoved(0, adapter.getItemCount());
        futureDictionaries = Executors.newSingleThreadExecutor().submit(() -> DictionaryService.getInstance().searchDictionaries(String.format("%s%%", newText)));
        try {
            var dictionaries = futureDictionaries.get();
            adapter.setDictionaries(dictionaries);
            adapter.notifyItemRangeInserted(0, dictionaries.size());
        } catch (ExecutionException | InterruptedException exception) {
            Toaster.make(context, "Can not load translations", exception);
            adapter.setDictionaries(new ArrayList<>());
        }
    }

}
