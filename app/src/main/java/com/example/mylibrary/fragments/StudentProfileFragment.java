package com.example.mylibrary.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.mylibrary.databinding.FragmentStudentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentProfileFragment extends Fragment {
    private FragmentStudentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadUserProfile();

        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("students")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        binding.etFullName.setText(document.getString("fullName"));
                        binding.etEmail.setText(user.getEmail());
                        binding.etPhone.setText(document.getString("phone"));
                        binding.etGroup.setText(document.getString("group"));
                    }
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(getContext(), "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
        }
    }

    private void saveProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String fullName = binding.etFullName.getText().toString().trim();
            String phone = binding.etPhone.getText().toString().trim();
            String group = binding.etGroup.getText().toString().trim();

            if (fullName.isEmpty() || phone.isEmpty() || group.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("students")
                .document(user.getUid())
                .update(
                    "fullName", fullName,
                    "phone", phone,
                    "group", group
                )
                .addOnSuccessListener(aVoid -> 
                    Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e -> 
                    Toast.makeText(getContext(), "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
        }
    }

    private void changePassword() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            mAuth.sendPasswordResetEmail(user.getEmail())
                .addOnSuccessListener(aVoid -> 
                    Toast.makeText(getContext(), "Password reset email sent", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e -> 
                    Toast.makeText(getContext(), "Error sending reset email: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 