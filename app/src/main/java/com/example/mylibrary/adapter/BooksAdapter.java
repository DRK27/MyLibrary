package com.example.mylibrary.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mylibrary.databinding.ItemBookBinding;
import com.example.mylibrary.model.Book;
import java.util.List;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BookViewHolder> {
    private final List<Book> books;
    private final boolean isAdminMode;
    private final String currentUserId;
    private OnBookActionListener listener;

    public interface OnBookActionListener {
        void onEditBook(Book book);
        void onDeleteBook(Book book);
        void onBorrowBook(Book book);
        void onReturnBook(Book book);
        void onBookClick(Book book);
    }

    public BooksAdapter(List<Book> books, boolean isAdminMode, String currentUserId) {
        this.books = books;
        this.isAdminMode = isAdminMode;
        this.currentUserId = currentUserId;
    }

    public void setOnBookActionListener(OnBookActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookBinding binding = ItemBookBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new BookViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        holder.bind(books.get(position));
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    class BookViewHolder extends RecyclerView.ViewHolder {
        private final ItemBookBinding binding;

        BookViewHolder(ItemBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Book book) {
            binding.tvTitle.setText(book.getTitle());
            binding.tvAuthor.setText(book.getAuthor());
            binding.tvDescription.setText(book.getDescription());
            binding.tvAvailable.setText("В наличии: " + (book.getAvailableQuantity() != 0 ? book.getAvailableQuantity() : 0) + "/" + book.getQuantity());
            binding.tvIsbn.setText("ISBN: " + (book.getIsbn() != null ? book.getIsbn() : "-"));

            if (isAdminMode) {
                binding.btnEdit.setVisibility(View.VISIBLE);
                binding.btnDelete.setVisibility(View.VISIBLE);
                binding.btnBorrow.setVisibility(View.GONE);
                binding.btnReturn.setVisibility(View.GONE);

                binding.btnEdit.setOnClickListener(v -> {
                    if (listener != null) listener.onEditBook(book);
                });
                binding.btnDelete.setOnClickListener(v -> {
                    if (listener != null) listener.onDeleteBook(book);
                });
                binding.getRoot().setOnClickListener(v -> {
                    if (listener != null) listener.onBookClick(book);
                });
            } else {
                binding.btnEdit.setVisibility(View.GONE);
                binding.btnDelete.setVisibility(View.GONE);
                binding.getRoot().setOnClickListener(null);

                if (book.getBorrowedBy() != null && book.getBorrowedBy().equals(currentUserId)) {
                    binding.btnBorrow.setVisibility(View.GONE);
                    binding.btnReturn.setVisibility(View.VISIBLE);
                    binding.btnReturn.setOnClickListener(v -> {
                        if (listener != null) listener.onReturnBook(book);
                    });
                } else if (book.isAvailable()) {
                    binding.btnBorrow.setVisibility(View.VISIBLE);
                    binding.btnReturn.setVisibility(View.GONE);
                    binding.btnBorrow.setOnClickListener(v -> {
                        if (listener != null) listener.onBorrowBook(book);
                    });
                } else {
                    binding.btnBorrow.setVisibility(View.GONE);
                    binding.btnReturn.setVisibility(View.GONE);
                }
            }
        }
    }
} 