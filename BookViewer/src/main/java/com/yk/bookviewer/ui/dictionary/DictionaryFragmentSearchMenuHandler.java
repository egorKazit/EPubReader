package com.yk.bookviewer.ui.dictionary;

import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;

import com.yk.common.service.dictionary.DictionaryService;
import com.yk.common.utils.ThreadOperator;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class DictionaryFragmentSearchMenuHandler implements SearchView.OnQueryTextListener {

    private final DictionaryFragmentRecyclerViewAdapter adapter;
    private final ThreadOperator threadOperator = ThreadOperator.getInstance(false);

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
        threadOperator.stop();
        var dictionaries = threadOperator.executeSingle(() -> DictionaryService.getInstance().searchDictionaries(String.format("%s%%", newText)), Exception::new);
        adapter.notifyItemRangeRemoved(0, adapter.getItemCount());
        adapter.setDictionaries(dictionaries);
        adapter.notifyItemRangeInserted(0, dictionaries.size());
    }

}
