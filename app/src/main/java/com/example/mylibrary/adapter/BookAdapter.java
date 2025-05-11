package com.example.mylibrary.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mylibrary.databinding.ItemBookAdminBinding;
import com.example.mylibrary.model.Book;
import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private List<Book> books = new ArrayList<>();
    private final OnBookActionListener listener;

    public interface OnBookActionListener {
        void onEditBook(Book book);
        void onDeleteBook(Book book);
    }

    public BookAdapter(OnBookActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookAdminBinding binding = ItemBookAdminBinding.inflate(
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

    public void setBooks(List<Book> books) {
        this.books = books;
        notifyDataSetChanged();
    }

    class BookViewHolder extends RecyclerView.ViewHolder {
        private final ItemBookAdminBinding binding;

        BookViewHolder(ItemBookAdminBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Book book) {
            binding.tvTitle.setText(book.getTitle());
            binding.tvAuthor.setText(book.getAuthor());
            binding.tvIsbn.setText("ISBN: " + book.getIsbn());
            binding.tvQuantity.setText("В наличии: " + book.getAvailableQuantity() + "/" + book.getQuantity());

            if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                    .load(book.getImageUrl())
                    .into(binding.ivBook);
            }

            binding.btnEdit.setOnClickListener(v -> listener.onEditBook(book));
            binding.btnDelete.setOnClickListener(v -> listener.onDeleteBook(book));
        }
    }
}