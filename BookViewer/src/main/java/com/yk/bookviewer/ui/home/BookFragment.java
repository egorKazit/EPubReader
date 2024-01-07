package com.yk.bookviewer.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.yk.bookviewer.databinding.FragmentBookBinding;
import com.yk.common.constants.GlobalConstants;
import com.yk.common.context.ActivityResultLauncherWrapper;
import com.yk.common.model.book.Book;
import com.yk.common.service.book.BookPool;
import com.yk.common.service.book.BookService;
import com.yk.common.service.book.BookServiceException;
import com.yk.common.service.book.BookServiceHelper;
import com.yk.common.utils.Toaster;
import com.yk.contentviewer.ContentViewer;

import java.util.Objects;

/**
 * Fragment to observe all books.
 * New book can be added by "add" button. Once it's pressed, new activity is started to explore file systems.
 * Existing book can be open by clicking on book cover or name. The implementation is done in @BookRecyclerViewAdapter class
 */

public class BookFragment extends Fragment {

    private FragmentBookBinding binding;
    private ActivityResultLauncher<Intent> intentActivityResultLauncher;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // register activity result
        intentActivityResultLauncher =
                ActivityResultLauncherWrapper
                        .getLauncher(this::registerForActivityResult,
                                new ActivityResultContracts.StartActivityForResult(),
                                intent -> loadBook(Objects.requireNonNull(intent.getExtras()).getString(GlobalConstants.BOOK_PATH)),
                                intent -> {
                                });
    }

    @SuppressLint("NotifyDataSetChanged")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // get binding and then retrieve view
        binding = FragmentBookBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set grid layout and recycler view adapter
        GridLayoutManager gridLayoutManager = new BookFragmentGridLayoutManager(getContext());
        binding.bookList.setLayoutManager(gridLayoutManager);
        binding.bookList.setAdapter(new BookFragmentRecyclerViewAdapter());
        // Upload books in a separate thread
        new Thread(() -> {
            BookPool.uploadBooks();
            requireActivity().runOnUiThread(() -> {
                if (binding == null)
                    return;
                Objects.requireNonNull(binding.bookList.getAdapter()).notifyDataSetChanged();
            });
        }).start();
        // set on scroll adapter
        binding.bookList.addOnScrollListener(new BookFragmentOnScrollListener(binding.addBook));
        binding.addBook.setOnClickListener(new BookFragmentOnClickListener(intentActivityResultLauncher::launch));

        return root;
    }

    @SuppressLint("NotifyDataSetChanged")

    private void loadBook(String bookPath) {
        try {
            boolean[] isNewBook = {false};
            Book book = BookPool.uploadBook(bookPath, isNewBook);
            if (isNewBook[0])
                Objects.requireNonNull(binding.bookList.getAdapter()).notifyDataSetChanged();
            BookServiceHelper.updateLatestBookPath(requireContext(), book.getFilePath());
            // create new intent and start activity
            Intent intent = new Intent(requireContext(), ContentViewer.class);
            BookService.initFromPath(book.getFilePath());
            requireContext().startActivity(intent);
        } catch (BookServiceException bookServiceException) {
            Toaster.make(getContext(), "Can not load book", null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}