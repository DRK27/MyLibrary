package com.example.mylibrary.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.mylibrary.R;
import com.example.mylibrary.databinding.FragmentAdminProfileBinding;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminProfileFragment extends Fragment {
    private FragmentAdminProfileBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
    }

    private void setupListeners() {
        binding.btnChangePassword.setOnClickListener(v -> changePassword());
        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void changePassword() {
        String currentPassword = binding.etCurrentPassword.getText().toString().trim();
        String newPassword = binding.etNewPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(requireContext(), "Новые пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!currentPassword.equals("admin")) { // Проверяем текущий пароль
            Toast.makeText(requireContext(), "Неверный текущий пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("admin")
            .document("credentials")
            .update("password", newPassword)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(requireContext(), "Пароль успешно изменен", Toast.LENGTH_SHORT).show();
                clearFields();
            })
            .addOnFailureListener(e -> 
                Toast.makeText(requireContext(), "Ошибка при смене пароля", Toast.LENGTH_SHORT).show());
    }

    private void clearFields() {
        binding.etCurrentPassword.setText("");
        binding.etNewPassword.setText("");
        binding.etConfirmPassword.setText("");
    }

    private void logout() {
        Navigation.findNavController(requireView()).navigate(R.id.action_adminProfileFragment_to_loginFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 