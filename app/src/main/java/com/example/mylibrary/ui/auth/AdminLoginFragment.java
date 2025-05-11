package com.example.mylibrary.ui.auth;

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

public class AdminLoginFragment extends Fragment {
    private static final String TAG = "AdminLoginFragment";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    
    private FragmentAdminLoginBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Creating admin login view");
        binding = FragmentAdminLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: Setting up click listeners");
        setupLoginButton();
    }

    private void setupLoginButton() {
        binding.btnAdminLogin.setOnClickListener(v -> {
            String username = binding.etAdminLogin.getText().toString().trim();
            String password = binding.etAdminPassword.getText().toString().trim();
            
            Log.d(TAG, "Login attempt with username: " + username);

            if (username.isEmpty() || password.isEmpty()) {
                showError("Пожалуйста, заполните все поля");
                return;
            }

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnAdminLogin.setEnabled(false);

            if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
                Log.d(TAG, "Admin login successful");
                navigateToAdminPanel();
            } else {
                Log.e(TAG, "Invalid admin credentials");
                binding.progressBar.setVisibility(View.GONE);
                binding.btnAdminLogin.setEnabled(true);
                showError("Неверные учетные данные администратора");
            }
        });
    }

    private void navigateToAdminPanel() {
        try {
            Log.d(TAG, "Navigating to admin main fragment");
            binding.progressBar.setVisibility(View.GONE);
            binding.btnAdminLogin.setEnabled(true);
            Navigation.findNavController(requireView())
                .navigate(R.id.action_adminLoginFragment_to_adminMainFragment);
        } catch (Exception e) {
            Log.e(TAG, "Navigation failed", e);
            binding.progressBar.setVisibility(View.GONE);
            binding.btnAdminLogin.setEnabled(true);
            Toast.makeText(requireContext(), 
                "Ошибка при переходе в админ панель", 
                Toast.LENGTH_LONG).show();
        }
    }

    private void showError(String message) {
        Log.d(TAG, "Showing error: " + message);
        binding.tvError.setText(message);
        binding.tvError.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: Cleaning up resources");
        binding = null;
    }
} 