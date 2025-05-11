package com.example.mylibrary.ui.admin;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.mylibrary.databinding.DialogAddEditBookBinding;
import com.example.mylibrary.model.Book;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddEditBookDialog extends DialogFragment {
    private DialogAddEditBookBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Book bookToEdit;
    private OnBookAddedListener listener;

    public interface OnBookAddedListener {
        void onBookAdded();
    }

    public static AddEditBookDialog newInstance(Book book) {
        AddEditBookDialog dialog = new AddEditBookDialog();
        dialog.bookToEdit = book;
        return dialog;
    }

    public void setOnBookAddedListener(OnBookAddedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogAddEditBookBinding.inflate(LayoutInflater.from(getContext()));

        if (bookToEdit != null) {
            binding.etTitle.setText(bookToEdit.getTitle());
            binding.etAuthor.setText(bookToEdit.getAuthor());
            binding.etIsbn.setText(bookToEdit.getIsbn());
            binding.etDescription.setText(bookToEdit.getDescription());
            binding.etQuantity.setText(String.valueOf(bookToEdit.getQuantity()));
            binding.etCategory.setText(bookToEdit.getCategory());
        }

        return new AlertDialog.Builder(requireContext())
            .setTitle(bookToEdit == null ? "Добавить книгу" : "Редактировать книгу")
            .setView(binding.getRoot())
            .setPositiveButton("Сохранить", null) // Устанавливаем null, чтобы предотвратить автоматическое закрытие
            .setNegativeButton("Отмена", (dialog, which) -> dismiss())
            .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> saveBook());
        }
    }

    private void saveBook() {
        String title = binding.etTitle.getText().toString().trim();
        String author = binding.etAuthor.getText().toString().trim();
        String isbn = binding.etIsbn.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String quantityStr = binding.etQuantity.getText().toString().trim();
        String category = binding.etCategory.getText().toString().trim();

        Log.d("AddEditBookDialog", "Starting to save book with title: " + title);

        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() ||
            description.isEmpty() || quantityStr.isEmpty() || category.isEmpty()) {
            Log.w("AddEditBookDialog", "Validation failed: empty fields");
            Toast.makeText(getContext(), "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity < 0) {
                Log.w("AddEditBookDialog", "Validation failed: negative quantity");
                Toast.makeText(getContext(), "Количество не может быть отрицательным", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Log.w("AddEditBookDialog", "Validation failed: invalid quantity format");
            Toast.makeText(getContext(), "Введите корректное количество", Toast.LENGTH_SHORT).show();
            return;
        }

        Book book = new Book(title, author, isbn, description, quantity, category, null);
        book.setAvailableQuantity(quantity);
        book.setBorrowedBy(null);
        book.setAddedDate(System.currentTimeMillis());

        if (bookToEdit != null) {
            book.setId(bookToEdit.getId());
            db.collection("books").document(bookToEdit.getId())
                .set(book)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Книга обновлена", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onBookAdded();
                    dismiss();
                })
                .addOnFailureListener(e ->
                    Toast.makeText(getContext(), "Ошибка обновления: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
        } else {
            db.collection("books")
                .add(book)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Книга добавлена", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onBookAdded();
                    dismiss();
                })
                .addOnFailureListener(e ->
                    Toast.makeText(getContext(), "Ошибка добавления: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 