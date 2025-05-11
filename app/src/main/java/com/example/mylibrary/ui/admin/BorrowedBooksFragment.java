package com.example.mylibrary.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.mylibrary.adapter.BorrowedBooksAdapter;
import com.example.mylibrary.databinding.FragmentBorrowedBooksBinding;
import com.example.mylibrary.model.BorrowedBook;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class BorrowedBooksFragment extends Fragment {
    private FragmentBorrowedBooksBinding binding;
    private BorrowedBooksAdapter adapter;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentFilter = "BORROWED";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBorrowedBooksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupTabLayout();
        loadBorrowedBooks();
    }

    private void setupRecyclerView() {
        adapter = new BorrowedBooksAdapter();
        binding.rvBorrowedBooks.setAdapter(adapter);
        binding.rvBorrowedBooks.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentFilter = "BORROWED";
                        break;
                    case 1:
                        currentFilter = "OVERDUE";
                        break;
                    case 2:
                        currentFilter = "RETURNED";
                        break;
                }
                loadBorrowedBooks();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadBorrowedBooks() {
        if (binding == null || !isAdded()) return;
        
        binding.progressBar.setVisibility(View.VISIBLE);
        
        db.collection("borrowed_books")
            .whereEqualTo("status", currentFilter)
            .addSnapshotListener((value, error) -> {
                if (binding == null || !isAdded()) return;
                
                binding.progressBar.setVisibility(View.GONE);
                
                if (error != null) {
                    Toast.makeText(requireContext(), 
                        "Ошибка при загрузке: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                    return;
                }

                if (value != null) {
                    List<BorrowedBook> borrowedBooks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : value) {
                        BorrowedBook borrowedBook = doc.toObject(BorrowedBook.class);
                        borrowedBook.setId(doc.getId());
                        borrowedBooks.add(borrowedBook);
                    }
                    adapter.setBorrowedBooks(borrowedBooks);
                    binding.tvEmpty.setVisibility(borrowedBooks.isEmpty() ? View.VISIBLE : View.GONE);
                }
            });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 