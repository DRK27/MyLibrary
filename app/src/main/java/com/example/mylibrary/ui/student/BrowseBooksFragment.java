package com.example.mylibrary.ui.student;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.mylibrary.R;
import com.example.mylibrary.adapter.BooksAdapter;
import com.example.mylibrary.databinding.FragmentBrowseBooksBinding;
import com.example.mylibrary.model.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.FirebaseApp;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import java.util.HashMap;
import java.util.Map;

public class BrowseBooksFragment extends Fragment implements BooksAdapter.OnBookActionListener {
    private static final String TAG = "BrowseBooksFragment";
    private FragmentBrowseBooksBinding binding;
    private BooksAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<Book> books = new ArrayList<>();
    private boolean isLoadingBooks = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFirebase();
    }

    private void initializeFirebase() {
        try {
            FirebaseApp.initializeApp(requireContext());
            
            db = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();
            
            Log.d(TAG, "Firebase successfully initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase", e);
            Toast.makeText(requireContext(), "Ошибка инициализации Firebase", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBrowseBooksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupSearch();
        loadBooks();
    }

    private void setupRecyclerView() {
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        adapter = new BooksAdapter(books, false, currentUserId);
        adapter.setOnBookActionListener(this);
        binding.rvBooks.setAdapter(adapter);
        binding.rvBooks.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void loadBooks() {
        if (isLoadingBooks || !isAdded()) return;

        isLoadingBooks = true;
        Log.d(TAG, "loadBooks: Starting to load books from Firestore");
        
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvNoBooks.setVisibility(View.GONE);

        db.collection("books")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!isAdded()) return;

                Log.d(TAG, "Successfully retrieved " + queryDocumentSnapshots.size() + " books");
                books.clear();

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    try {
                        Book book = document.toObject(Book.class);
                        book.setId(document.getId());
                        books.add(book);
                        Log.d(TAG, "Added book: " + book.getTitle());
                    } catch (Exception e) {
                        Log.e(TAG, "Error converting document: " + document.getId(), e);
                    }
                }

                updateUI();
                isLoadingBooks = false;
            })
            .addOnFailureListener(e -> {
                if (!isAdded()) return;
                
                Log.e(TAG, "Error loading books", e);
                handleLoadError(e);
                isLoadingBooks = false;
            });
    }

    private void updateUI() {
        if (!isAdded()) return;
        adapter.notifyDataSetChanged();
        binding.progressBar.setVisibility(View.GONE);
        if (books.isEmpty()) {
            binding.tvNoBooks.setVisibility(View.VISIBLE);
            binding.tvNoBooks.setText("Книги не найдены");
        } else {
            binding.tvNoBooks.setVisibility(View.GONE);
        }
    }

    private void handleLoadError(Exception e) {
        if (!isAdded()) return;

        binding.progressBar.setVisibility(View.GONE);
        binding.tvNoBooks.setVisibility(View.VISIBLE);

        String errorMessage = e.getMessage();
        if (errorMessage != null && errorMessage.contains("PERMISSION_DENIED")) {
            binding.tvNoBooks.setText("Ошибка доступа к базе данных");
            binding.getRoot().postDelayed(this::loadBooks, 1000);
        } else {
            binding.tvNoBooks.setText("Ошибка при загрузке книг");
            Toast.makeText(requireContext(), 
                "Ошибка при загрузке книг: " + errorMessage, 
                Toast.LENGTH_LONG).show();
        }
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBooks(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterBooks(String query) {
        if (query.isEmpty()) {
            adapter.notifyDataSetChanged();
            return;
        }
        List<Book> filteredBooks = new ArrayList<>();
        for (Book book : books) {
            String title = book.getTitle() != null ? book.getTitle() : "";
            String author = book.getAuthor() != null ? book.getAuthor() : "";
            if (title.toLowerCase().contains(query.toLowerCase()) ||
                author.toLowerCase().contains(query.toLowerCase())) {
                filteredBooks.add(book);
            }
        }
        books.clear();
        books.addAll(filteredBooks);
        adapter.notifyDataSetChanged();
        if (filteredBooks.isEmpty()) {
            binding.tvNoBooks.setVisibility(View.VISIBLE);
            binding.tvNoBooks.setText("Книги не найдены");
        } else {
            binding.tvNoBooks.setVisibility(View.GONE);
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
        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Пожалуйста, войдите в систему", Toast.LENGTH_SHORT).show();
            return;
        }
        if (book.getAvailableQuantity() <= 0 || !book.isAvailable()) {
            Toast.makeText(requireContext(), "Книга недоступна для выдачи", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = auth.getCurrentUser().getUid();
        String bookId = book.getId();

        Map<String, Object> borrowedBook = new HashMap<>();
        borrowedBook.put("userId", userId);
        borrowedBook.put("bookId", bookId);
        borrowedBook.put("borrowDate", System.currentTimeMillis());
        borrowedBook.put("dueDate", System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000));
        borrowedBook.put("status", "BORROWED");

        binding.progressBar.setVisibility(View.VISIBLE);

        db.runTransaction(transaction -> {
            DocumentSnapshot bookDoc = transaction.get(db.collection("books").document(bookId));
            Book currentBook = bookDoc.toObject(Book.class);

            if (currentBook == null || currentBook.getAvailableQuantity() <= 0 || !currentBook.isAvailable()) {
                throw new FirebaseFirestoreException("Книга недоступна", FirebaseFirestoreException.Code.ABORTED);
            }

            transaction.update(db.collection("books").document(bookId),
                "availableQuantity", currentBook.getAvailableQuantity() - 1,
                "borrowedBy", userId);

            transaction.set(db.collection("borrowed_books").document(), borrowedBook);

            return null;
        }).addOnSuccessListener(aVoid -> {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Книга успешно выдана", Toast.LENGTH_SHORT).show();
            loadBooks();
        }).addOnFailureListener(e -> {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Ошибка при выдаче книги: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onReturnBook(Book book) {
        // Не используется в этом фрагменте
    }

    @Override
    public void onBookClick(Book book) {
        // Не используется для студента
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isLoadingBooks = false;
        binding = null;
    }
} 