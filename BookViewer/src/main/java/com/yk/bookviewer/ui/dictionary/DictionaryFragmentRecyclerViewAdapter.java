package com.yk.bookviewer.ui.dictionary;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.yk.bookviewer.R;
import com.yk.common.constants.GlobalConstants;
import com.yk.common.model.dictionary.DictionaryPool;
import com.yk.common.utils.Toaster;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Dictionary fragment recycler view adapter.
 * It inflates a dictionary item layout and set event listeners
 */
@RequiresApi(api = Build.VERSION_CODES.S)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class DictionaryFragmentRecyclerViewAdapter extends RecyclerView.Adapter<DictionaryFragmentRecyclerViewAdapter.DictionaryFragmentViewHolder> {

    private final Fragment parentFragment;

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
        holder.dictionaryOriginWord.setText(DictionaryPool.getDictionaries().get(position).getOriginWord().getOriginWord());
        holder.dictionaryTargetWord.setText(DictionaryPool.getDictionaries().get(position).getTranslations().get(0).getTranslation());
        // set listeners
        holder.dictionaryOriginWord.setOnClickListener(v -> navigateOnClick(position));
        holder.dictionaryTargetWord.setOnClickListener(v -> navigateOnClick(position));
        holder.dictionaryContainer.setOnClickListener(v -> navigateOnClick(position));
    }

    @Override
    public int getItemCount() {
        return DictionaryPool.getDictionaries().size();
    }

    /**
     * Method to navigate on item click
     *
     * @param position clicked item position
     */
    private void navigateOnClick(int position) {
        // check if navigation is possible -> definitions should be defined and exist(at least 1)
        if (DictionaryPool.getDictionaries().get(position).getDefinitions() == null ||
                DictionaryPool.getDictionaries().get(position).getDefinitions().size() == 0) {
            Toaster.make(parentFragment.getContext(), "No definitions found", null);
            return;
        }
        // set put data for navigation
        Bundle bundle = new Bundle();
        bundle.putInt(GlobalConstants.ORIGIN_WORD_POSITION, position);
        // navigate via action by navigation controller
        NavHostFragment.findNavController(parentFragment)
                .navigate(R.id.action_navigate_dictionary_to_definition, bundle);
    }

    /**
     * Dictionary fragment item holder
     */
    protected static class DictionaryFragmentViewHolder extends RecyclerView.ViewHolder {

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
