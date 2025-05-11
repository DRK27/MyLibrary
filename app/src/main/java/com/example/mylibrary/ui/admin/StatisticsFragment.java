package com.example.mylibrary.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.mylibrary.adapter.BookAdapter;
import com.example.mylibrary.databinding.FragmentStatisticsBinding;
import com.example.mylibrary.model.Book;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class StatisticsFragment extends Fragment {
    private FragmentStatisticsBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private BookAdapter popularBooksAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        loadStatistics();
    }

    private void setupRecyclerView() {
        popularBooksAdapter = new BookAdapter(null);
        binding.rvPopularBooks.setAdapter(popularBooksAdapter);
        binding.rvPopularBooks.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void loadStatistics() {
        binding.progressBar.setVisibility(View.VISIBLE);

        db.collection("books").get().addOnSuccessListener(books -> {
            int totalBooks = 0;
            int availableBooks = 0;
            for (var doc : books) {
                Book book = doc.toObject(Book.class);
                totalBooks += book.getQuantity();
                availableBooks += book.getAvailableQuantity();
            }
            binding.tvTotalBooks.setText(String.format("Всего книг: %d (доступно: %d)", totalBooks, availableBooks));
        });

        db.collection("students").get().addOnSuccessListener(students -> {
            int totalStudents = students.size();
            int activeStudents = 0;
            for (var doc : students) {
                if (doc.getBoolean("active")) {
                    activeStudents++;
                }
            }
            binding.tvTotalStudents.setText("Всего студентов: " + totalStudents);
            binding.tvActiveStudents.setText("Активных студентов: " + activeStudents);
        });

        db.collection("borrowed_books")
            .whereEqualTo("status", "BORROWED")
            .get()
            .addOnSuccessListener(borrowed -> 
                binding.tvBorrowedBooks.setText("Книг на руках: " + borrowed.size()));

        db.collection("borrowed_books")
            .whereEqualTo("status", "OVERDUE")
            .get()
            .addOnSuccessListener(overdue -> 
                binding.tvOverdueBooks.setText("Просроченных книг: " + overdue.size()));

        db.collection("borrowed_books")
            .whereEqualTo("status", "RETURNED")
            .get()
            .addOnSuccessListener(returned -> 
                binding.tvReturnedBooks.setText("Возвращено книг: " + returned.size()));

        db.collection("books")
            .orderBy("borrowCount", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener(popularBooks -> {
                List<Book> books = new ArrayList<>();
                popularBooks.forEach(doc -> books.add(doc.toObject(Book.class)));
                popularBooksAdapter.setBooks(books);
                binding.progressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> binding.progressBar.setVisibility(View.GONE));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 