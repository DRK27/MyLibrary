package com.example.mylibrary.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mylibrary.R;
import com.example.mylibrary.databinding.ItemBookBinding;
import com.example.mylibrary.model.Book;
import java.util.ArrayList;
import java.util.List;

public class StudentBookAdapter extends RecyclerView.Adapter<StudentBookAdapter.BookViewHolder> {
    private List<Book> books = new ArrayList<>();

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

    public void setBooks(List<Book> books) {
        this.books = books;
        notifyDataSetChanged();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        private final ItemBookBinding binding;

        BookViewHolder(ItemBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Book book) {
            binding.tvTitle.setText(book.getTitle());
            binding.tvAuthor.setText(book.getAuthor());
            binding.tvCategory.setText(book.getCategory());
            binding.tvAvailable.setText(String.format("В наличии: %d/%d", 
                book.getAvailableQuantity(), book.getQuantity()));

            if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                    .load(book.getImageUrl())
                    .placeholder(R.drawable.ic_book)
                    .error(R.drawable.ic_book)
                    .into(binding.ivBook);
            } else {
                binding.ivBook.setImageResource(R.drawable.ic_book);
            }
            binding.btnBorrow.setVisibility(
                book.getAvailableQuantity() > 0 ? View.VISIBLE : View.GONE);
        }
    }
} 