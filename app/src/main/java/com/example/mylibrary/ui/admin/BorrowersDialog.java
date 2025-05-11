package com.example.mylibrary.ui.admin;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.mylibrary.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class BorrowersDialog extends DialogFragment {
    private static final String ARG_BOOK_ID = "book_id";
    private static final String ARG_BOOK_TITLE = "book_title";
    private String bookId;
    private String bookTitle;
    private FirebaseFirestore db;

    public static BorrowersDialog newInstance(String bookId, String bookTitle) {
        BorrowersDialog dialog = new BorrowersDialog();
        Bundle args = new Bundle();
        args.putString(ARG_BOOK_ID, bookId);
        args.putString(ARG_BOOK_TITLE, bookTitle);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_borrowers, null);
        ListView listView = view.findViewById(R.id.listViewBorrowers);
        ProgressBar progressBar = view.findViewById(R.id.progressBarBorrowers);
        TextView tvEmpty = view.findViewById(R.id.tvEmptyBorrowers);
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);

        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            bookId = getArguments().getString(ARG_BOOK_ID);
            bookTitle = getArguments().getString(ARG_BOOK_TITLE);
        }
        tvTitle.setText("Кто взял: " + bookTitle);
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        List<String> borrowers = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, borrowers);
        listView.setAdapter(adapter);

        db.collection("borrowed_books")
            .whereEqualTo("bookId", bookId)
            .whereEqualTo("status", "BORROWED")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<String> userIds = new ArrayList<>();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    String userId = doc.getString("userId");
                    if (userId != null) userIds.add(userId);
                }
                if (userIds.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Никто не взял эту книгу");
                    return;
                }
                for (String userId : userIds) {
                    db.collection("users").document(userId)
                        .get()
                        .addOnSuccessListener(userDoc -> {
                            String name = userDoc.getString("name");
                            String email = userDoc.getString("email");
                            String display = (name != null ? name : "Без имени") + (email != null ? " (" + email + ")" : "");
                            borrowers.add(display);
                            adapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                        });
                }
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Ошибка: " + e.getMessage());
            });

        return new AlertDialog.Builder(requireContext())
            .setView(view)
            .setPositiveButton("Закрыть", (dialog, which) -> dismiss())
            .create();
    }
} 