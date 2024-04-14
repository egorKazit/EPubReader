package com.yk.remoteexplorer.ui.book;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yk.common.context.BookFragmentGridLayoutManager;
import com.yk.common.model.remote.Book;
import com.yk.remoteexplorer.R;
import com.yk.remoteexplorer.databinding.FragmentBooksBinding;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BookFragment extends Fragment {

    private FragmentBooksBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentBooksBinding.inflate(getLayoutInflater());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        var booksRecyclerViewAdapter = new BookRecyclerViewAdapter(this);
        binding.books.setLayoutManager(new BookFragmentGridLayoutManager(requireContext()));
        binding.books.setAdapter(booksRecyclerViewAdapter);
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            BookHolder.books.addAll(IntStream.range(0, 10)
                    .mapToObj(i -> Book.builder().id(String.valueOf(i)).author("Creator " + i).name("Tense " + i * 10).build()).collect(Collectors.toList()));
            booksRecyclerViewAdapter.notifyDataSetChanged();
            ((Activity) binding.getRoot().getContext()).runOnUiThread(() -> this.requireActivity().findViewById(R.id.loadingDialog).setVisibility(View.GONE));
        }, 5, TimeUnit.SECONDS);
        return binding.getRoot();
    }

}