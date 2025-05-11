package com.example.mylibrary.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mylibrary.databinding.ItemBorrowedBookBinding;
import com.example.mylibrary.model.BorrowedBook;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BorrowedBooksAdapter extends RecyclerView.Adapter<BorrowedBooksAdapter.BorrowedBookViewHolder> {
    private List<BorrowedBook> borrowedBooks = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    @NonNull
    @Override
    public BorrowedBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBorrowedBookBinding binding = ItemBorrowedBookBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new BorrowedBookViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BorrowedBookViewHolder holder, int position) {
        holder.bind(borrowedBooks.get(position));
    }

    @Override
    public int getItemCount() {
        return borrowedBooks.size();
    }

    public void setBorrowedBooks(List<BorrowedBook> borrowedBooks) {
        this.borrowedBooks = borrowedBooks;
        notifyDataSetChanged();
    }

    class BorrowedBookViewHolder extends RecyclerView.ViewHolder {
        private final ItemBorrowedBookBinding binding;

        BorrowedBookViewHolder(ItemBorrowedBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(BorrowedBook borrowedBook) {
            // Название книги
            if (borrowedBook.getBookTitle() != null && !borrowedBook.getBookTitle().isEmpty()) {
                binding.tvBookTitle.setText(borrowedBook.getBookTitle());
            } else if (borrowedBook.getBookId() != null) {
                db.collection("books").document(borrowedBook.getBookId())
                    .get()
                    .addOnSuccessListener(bookDoc -> {
                        String title = bookDoc.getString("title");
                        binding.tvBookTitle.setText(title != null ? title : "Без названия");
                    });
            } else {
                binding.tvBookTitle.setText("Без названия");
            }

            // Имя студента (ищем сначала studentId, потом userId)
            String studentId = borrowedBook.getStudentId();
            if (studentId == null || studentId.isEmpty()) {
                studentId = borrowedBook.getUserId();
            }
            if (studentId != null && !studentId.isEmpty()) {
                db.collection("users").document(studentId)
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        String name = userDoc.getString("name");
                        if (name != null && !name.isEmpty()) {
                            binding.tvStudentName.setText(name);
                        } else {
                            binding.tvStudentName.setText("Неизвестный студент");
                        }
                    })
                    .addOnFailureListener(e -> {
                        binding.tvStudentName.setText("Неизвестный студент");
                    });
            } else {
                binding.tvStudentName.setText("Неизвестный студент");
            }

            binding.tvBorrowDate.setText("Взята: " + dateFormat.format(new Date(borrowedBook.getBorrowDate())));
            binding.tvDueDate.setText("Вернуть до: " + dateFormat.format(new Date(borrowedBook.getDueDate())));

            updateStatus(borrowedBook);

            if ("BORROWED".equals(borrowedBook.getStatus())) {
                binding.btnReturn.setVisibility(View.VISIBLE);
                binding.btnReturn.setOnClickListener(v -> returnBook(borrowedBook));
            } else {
                binding.btnReturn.setVisibility(View.GONE);
            }
        }

        private void updateStatus(BorrowedBook borrowedBook) {
            long now = System.currentTimeMillis();
            String status = borrowedBook.getStatus();
            int statusColor = 0;

            if ("RETURNED".equals(status)) {
                binding.tvStatus.setText("Возвращена");
                statusColor = itemView.getContext().getColor(android.R.color.holo_green_dark);
            } else if (now > borrowedBook.getDueDate()) {
                binding.tvStatus.setText("Просрочена");
                statusColor = itemView.getContext().getColor(android.R.color.holo_red_dark);
                if (!"OVERDUE".equals(status)) {
                    borrowedBook.setStatus("OVERDUE");
                    updateBookStatus(borrowedBook);
                }
            } else {
                binding.tvStatus.setText("На руках");
                statusColor = itemView.getContext().getColor(android.R.color.holo_blue_dark);
            }

            binding.tvStatus.setTextColor(statusColor);
        }

        private void returnBook(BorrowedBook borrowedBook) {
            borrowedBook.setStatus("RETURNED");
            borrowedBook.setReturnDate(System.currentTimeMillis());

            db.collection("borrowed_books")
                .document(borrowedBook.getId())
                .set(borrowedBook)
                .addOnSuccessListener(aVoid -> {
                    db.collection("books")
                        .document(borrowedBook.getBookId())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                int availableQuantity = documentSnapshot.getLong("availableQuantity").intValue();
                                documentSnapshot.getReference().update("availableQuantity", availableQuantity + 1);
                            }
                        });
                });
        }

        private void updateBookStatus(BorrowedBook borrowedBook) {
            db.collection("borrowed_books")
                .document(borrowedBook.getId())
                .update("status", borrowedBook.getStatus());
        }
    }
} 