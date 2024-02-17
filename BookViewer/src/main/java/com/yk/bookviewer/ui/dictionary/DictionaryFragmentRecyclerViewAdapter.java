package com.yk.bookviewer.ui.dictionary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.yk.bookviewer.R;
import com.yk.common.constants.GlobalConstants;
import com.yk.common.model.dictionary.Dictionary;
import com.yk.common.service.dictionary.DictionaryService;
import com.yk.common.utils.Toaster;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Dictionary fragment recycler view adapter.
 * It inflates a dictionary item layout and set event listeners
 */

public final class DictionaryFragmentRecyclerViewAdapter extends RecyclerView.Adapter<DictionaryFragmentRecyclerViewAdapter.DictionaryFragmentViewHolder> {

    private final Fragment parentFragment;
    @Setter(AccessLevel.PACKAGE)
    @Getter(AccessLevel.PACKAGE)
    private List<Dictionary> dictionaries;

    DictionaryFragmentRecyclerViewAdapter(Fragment parentFragment) {
        this.parentFragment = parentFragment;
        var futureDictionaries = Executors.newSingleThreadExecutor().submit(() -> DictionaryService.getInstance().getDictionaries());
        try {
            dictionaries = futureDictionaries.get();
        } catch (ExecutionException | InterruptedException exception) {
            Toaster.make(parentFragment.requireActivity(), R.string.can_not_load_translations, exception);
            dictionaries = List.of();
        }
    }

    @NonNull
    @Override
    public DictionaryFragmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create new view from LayoutInflater and return new holder
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dictionary_item, parent, false);
        return new DictionaryFragmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DictionaryFragmentViewHolder holder, int position) {
        // fill in data
        holder.dictionaryOriginWord.setText(dictionaries.get(position).getOriginWord().getOriginWord());
        holder.dictionaryTargetWord.setText(DictionaryService.getMainTranslation(dictionaries.get(position)));
        // set listeners
        holder.dictionaryOriginWord.setOnClickListener(v -> navigateOnClick(position));
        holder.dictionaryTargetWord.setOnClickListener(v -> navigateOnClick(position));
        holder.dictionaryContainer.setOnClickListener(v -> navigateOnClick(position));
    }

    @Override
    public int getItemCount() {
        return dictionaries.size();
    }

    /**
     * Method to navigate on item click
     *
     * @param position clicked item position
     */
    private void navigateOnClick(int position) {
        // check if navigation is possible -> definitions should be defined and exist(at least 1)
        if (dictionaries.get(position).getDefinitions() == null ||
                dictionaries.get(position).getDefinitions().isEmpty()) {
            Toaster.make(parentFragment.getContext(), R.string.no_definitions_found, null);
            return;
        }
        // set put data for navigation
        Bundle bundle = new Bundle();
        bundle.putInt(GlobalConstants.ORIGIN_WORD_NAVIGATION, dictionaries.get(position).getOriginWord().getId());
        // navigate via action by navigation controller
        NavHostFragment.findNavController(parentFragment)
                .navigate(R.id.action_navigate_dictionary_to_definition, bundle);
    }

    /**
     * Dictionary fragment item holder
     */
    public static class DictionaryFragmentViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout dictionaryContainer;
        private final TextView dictionaryOriginWord;
        private final TextView dictionaryTargetWord;

        /**
         * Main constructor to set views
         *
         * @param itemView item view
         */
        public DictionaryFragmentViewHolder(@NonNull View itemView) {
            super(itemView);
            dictionaryContainer = itemView.findViewById(R.id.dictionaryContainer);
            dictionaryOriginWord = itemView.findViewById(R.id.dictionaryOriginWord);
            dictionaryTargetWord = itemView.findViewById(R.id.dictionaryTargetWord);
        }
    }

}
