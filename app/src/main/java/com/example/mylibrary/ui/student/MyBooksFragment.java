package com.example.mylibrary.ui.student;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.mylibrary.adapter.BooksAdapter;
import com.example.mylibrary.databinding.FragmentMyBooksBinding;
import com.example.mylibrary.model.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MyBooksFragment extends Fragment implements BooksAdapter.OnBookActionListener {
    private FragmentMyBooksBinding binding;
    private BooksAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<Book> myBooks = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyBooksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        setupRecyclerView();
        loadMyBooks();
    }

    private void setupRecyclerView() {
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        adapter = new BooksAdapter(myBooks, false, currentUserId);
        adapter.setOnBookActionListener(this);
        binding.rvMyBooks.setAdapter(adapter);
        binding.rvMyBooks.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void loadMyBooks() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(requireContext(), "Пожалуйста, войдите в систему", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("borrowed_books")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<String> bookIds = new ArrayList<>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String bookId = doc.getString("bookId");
                    if (bookId != null) bookIds.add(bookId);
                }
                if (bookIds.isEmpty()) {
                    myBooks.clear();
                    adapter.notifyDataSetChanged();
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvNoBooks.setVisibility(View.VISIBLE);
                    binding.tvNoBooks.setText("У вас нет взятых книг");
                    return;
                }
                fetchBooksByIds(bookIds);
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Ошибка при загрузке: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void fetchBooksByIds(List<String> bookIds) {
        myBooks.clear();
        for (String bookId : bookIds) {
            db.collection("books").document(bookId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Book book = documentSnapshot.toObject(Book.class);
                    if (book != null) {
                        book.setId(documentSnapshot.getId());
                        myBooks.add(book);
                    }
                    adapter.notifyDataSetChanged();
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvNoBooks.setVisibility(myBooks.isEmpty() ? View.VISIBLE : View.GONE);
                    if (myBooks.isEmpty()) {
                        binding.tvNoBooks.setText("У вас нет взятых книг");
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Ошибка при получении книги: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        }
    }

    @Override
    public void onEditBook(Book book) {
        // Не используется в этом фрагменте
    }

    @Override
    public void onDeleteBook(Book book) {
        // Не используется в этом фрагменте
    }

    @Override
    public void onBorrowBook(Book book) {
        // Не используется в этом фрагменте
    }

    @Override
    public void onReturnBook(Book book) {
        Log.d("MyBooksFragment", "onReturnBook called for: " + book.getTitle() + ", borrowedBy=" + book.getBorrowedBy());
        Toast.makeText(requireContext(), "onReturnBook called", Toast.LENGTH_SHORT).show();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("borrowed_books")
            .whereEqualTo("userId", userId)
            .whereEqualTo("bookId", book.getId())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    db.collection("borrowed_books").document(queryDocumentSnapshots.getDocuments().get(0).getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            db.collection("books").document(book.getId())
                                .update(
                                    "availableQuantity", book.getAvailableQuantity() + 1,
                                    "borrowedBy", null
                                )
                                .addOnSuccessListener(aVoid2 -> {
                                    Toast.makeText(requireContext(), "Книга возвращена", Toast.LENGTH_SHORT).show();
                                    loadMyBooks();
                                })
                                .addOnFailureListener(e -> {
                                    binding.progressBar.setVisibility(View.GONE);
                                    Toast.makeText(requireContext(), "Ошибка при возврате: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                        })
                        .addOnFailureListener(e -> {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(requireContext(), "Ошибка при возврате: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Запись о взятой книге не найдена", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Ошибка при возврате: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public void onBookClick(Book book) {
        // Не используется для студента
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 