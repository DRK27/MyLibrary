package com.example.mylibrary.ui.admin;

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
import com.example.mylibrary.R;
import com.example.mylibrary.databinding.FragmentManageBooksBinding;
import com.example.mylibrary.adapter.BooksAdapter;
import com.example.mylibrary.model.Book;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ManageBooksFragment extends Fragment implements BooksAdapter.OnBookActionListener {
    private static final String TAG = "ManageBooksFragment";
    private FragmentManageBooksBinding binding;
    private FirebaseFirestore db;
    private BooksAdapter adapter;
    private List<Book> booksList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        booksList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageBooksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupAddBookButton();
        loadBooks();
    }

    private void setupRecyclerView() {
        adapter = new BooksAdapter(booksList, true, null);
        adapter.setOnBookActionListener(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupAddBookButton() {
        binding.fabAddBook.setOnClickListener(v -> showAddBookDialog());
    }

    private void showAddBookDialog() {
        AddEditBookDialog dialog = new AddEditBookDialog();
        dialog.setOnBookAddedListener(this::loadBooks);
        dialog.show(getChildFragmentManager(), "AddBookDialog");
    }

    private void loadBooks() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        db.collection("books")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                booksList.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    try {
                        Book book = document.toObject(Book.class);
                        book.setId(document.getId());
                        booksList.add(book);
                        Log.d(TAG, "Loaded book: " + book.getTitle());
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing book data", e);
                    }
                }
                
                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
                
                if (booksList.isEmpty()) {
                    Toast.makeText(requireContext(), 
                        "Список книг пуст", 
                        Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), 
                        "Загружено книг: " + booksList.size(), 
                        Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading books", e);
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), 
                    "Ошибка при загрузке списка книг: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public void onEditBook(Book book) {
        AddEditBookDialog dialog = AddEditBookDialog.newInstance(book);
        dialog.setOnBookAddedListener(this::loadBooks);
        dialog.show(getChildFragmentManager(), "EditBookDialog");
    }

    @Override
    public void onDeleteBook(Book book) {
        db.collection("books").document(book.getId())
            .delete()
            .addOnSuccessListener(aVoid -> {
                booksList.remove(book);
                adapter.notifyDataSetChanged();
                Toast.makeText(requireContext(), 
                    "Книга удалена", 
                    Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), 
                    "Ошибка при удалении книги: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public void onBorrowBook(Book book) {
        // В режиме админа этот метод не используется
    }

    @Override
    public void onReturnBook(Book book) {
        db.collection("borrowed_books")
            .whereEqualTo("bookId", book.getId())
            .whereEqualTo("status", "BORROWED")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    // Возвращаем все найденные экземпляры (или только первый, если нужно)
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().update(
                            "status", "RETURNED",
                            "returnDate", System.currentTimeMillis()
                        );
                    }
                    db.collection("books").document(book.getId())
                        .update(
                            "availableQuantity", book.getAvailableQuantity() + 1,
                            "borrowedBy", null
                        )
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(requireContext(), "Книга возвращена", Toast.LENGTH_SHORT).show();
                            loadBooks();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(requireContext(), "Ошибка при возврате: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                } else {
                    Toast.makeText(requireContext(), "Нет записей о выданных книгах", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Ошибка при возврате: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public void onBookClick(Book book) {
        showBorrowersDialog(book);
    }

    private void showBorrowersDialog(Book book) {
        db.collection("borrowed_books")
            .whereEqualTo("bookId", book.getId())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<String> userIds = new ArrayList<>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String userId = doc.getString("userId");
                    if (userId != null) userIds.add(userId);
                }
                if (userIds.isEmpty()) {
                    showNamesDialog(java.util.Collections.singletonList("Никто не брал эту книгу"));
                } else {
                    fetchUserNamesAndShow(userIds);
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void fetchUserNamesAndShow(List<String> userIds) {
        List<String> names = new ArrayList<>();
        if (userIds.isEmpty()) {
            showNamesDialog(java.util.Collections.singletonList("Никто не брал эту книгу"));
            return;
        }
        final int[] counter = {0};
        for (String userId : userIds) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String name = userDoc.getString("name");
                    if (name == null) name = userId; // fallback
                    names.add(name);
                    counter[0]++;
                    if (counter[0] == userIds.size()) {
                        showNamesDialog(names);
                    }
                })
                .addOnFailureListener(e -> {
                    names.add(userId); // fallback
                    counter[0]++;
                    if (counter[0] == userIds.size()) {
                        showNamesDialog(names);
                    }
                });
        }
    }

    private void showNamesDialog(List<String> names) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Кто брал книгу")
            .setItems(names.toArray(new String[0]), null)
            .setPositiveButton("Закрыть", null)
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 