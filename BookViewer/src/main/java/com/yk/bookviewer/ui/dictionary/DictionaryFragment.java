package com.yk.bookviewer.ui.dictionary;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yk.bookviewer.databinding.FragmentDictionaryBinding;

/**
 * Dictionary fragment.
 * Contains all translated words which has been saved on disk
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class DictionaryFragment extends Fragment {

    private FragmentDictionaryBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDictionaryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // fill recycler view with data
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.dictionaryList.setLayoutManager(linearLayoutManager);
        binding.dictionaryList.setAdapter(new DictionaryFragmentRecyclerViewAdapter(this));
        // add divider between items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(),
                linearLayoutManager.getOrientation());
        binding.dictionaryList.addItemDecoration(dividerItemDecoration);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}