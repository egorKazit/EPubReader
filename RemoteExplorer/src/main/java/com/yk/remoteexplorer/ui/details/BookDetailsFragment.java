package com.yk.remoteexplorer.ui.details;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.yk.common.model.remote.Book;
import com.yk.remoteexplorer.R;
import com.yk.remoteexplorer.databinding.FragmentDetailsBinding;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BookDetailsFragment extends Fragment {

    private FragmentDetailsBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentDetailsBinding.inflate(getLayoutInflater());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding.bookImage.setVisibility(View.GONE);
        binding.bookNameContainer.setVisibility(View.GONE);
        binding.bookAuthorContainer.setVisibility(View.GONE);
        binding.bookGenreContainer.setVisibility(View.GONE);
        binding.bookAnnotation.setVisibility(View.GONE);
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            var book = Book.builder().name("Test").author("Author").genre("genre").annotation("bla-bal \n bla-bla").build();
            requireActivity().runOnUiThread(() -> {
                binding.bookImage.setVisibility(View.VISIBLE);
                Glide.with(requireActivity()).load(R.mipmap.ic_launcher).into(binding.bookImage);
                binding.bookNameContainer.setVisibility(View.VISIBLE);
                binding.bookNameValue.setText(book.getName());
                binding.bookAuthorContainer.setVisibility(View.VISIBLE);
                binding.bookAuthorValue.setText(book.getAuthor());
                binding.bookGenreContainer.setVisibility(View.VISIBLE);
                binding.bookGenreValue.setText(book.getGenre());
                binding.bookAnnotation.setVisibility(View.VISIBLE);
                binding.bookAnnotation.setText(book.getAnnotation());
                requireActivity().findViewById(R.id.loadingDialog).setVisibility(View.GONE);
            });
        }, 2, TimeUnit.SECONDS);
        return binding.getRoot();
    }
}
