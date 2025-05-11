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
import androidx.navigation.Navigation;
import com.example.mylibrary.R;
import com.example.mylibrary.databinding.FragmentAdminLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AdminLoginFragment extends Fragment {
    private static final String TAG = "AdminLoginFragment";
    private FragmentAdminLoginBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupLoginButton();
    }

    private void setupLoginButton() {
        binding.btnAdminLogin.setOnClickListener(v -> {
            String email = binding.etAdminLogin.getText().toString().trim();
            String password = binding.etAdminPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                showError("Пожалуйста, заполните все поля");
                return;
            }

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnAdminLogin.setEnabled(false);

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        createAdminDocument(user);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Login failed", e);
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnAdminLogin.setEnabled(true);
                    showError("Ошибка входа: " + e.getMessage());
                });
        });
    }

    private void createAdminDocument(FirebaseUser user) {
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("name", "Administrator");
        adminData.put("email", user.getEmail());
        adminData.put("role", "admin");
        adminData.put("active", true);

        db.collection("users").document(user.getUid())
            .set(adminData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Admin document created successfully");
                navigateToAdminPanel();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error creating admin document", e);
                binding.progressBar.setVisibility(View.GONE);
                binding.btnAdminLogin.setEnabled(true);
                showError("Ошибка создания документа администратора: " + e.getMessage());
            });
    }

    private void navigateToAdminPanel() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnAdminLogin.setEnabled(true);
        Navigation.findNavController(requireView())
            .navigate(R.id.action_adminLoginFragment_to_adminMainFragment);
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 