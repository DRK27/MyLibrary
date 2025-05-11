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
import com.example.mylibrary.databinding.FragmentStudentRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.HashMap;
import java.util.Map;

public class StudentRegisterFragment extends Fragment {
    private static final String TAG = "StudentRegisterFragment";
    private FragmentStudentRegisterBinding binding;
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
        binding = FragmentStudentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRegisterButton();
    }

    private void setupRegisterButton() {
        binding.btnRegister.setOnClickListener(v -> {
            Log.d(TAG, "Register button clicked");
            registerStudent();
        });
    }

    private void registerStudent() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String name = binding.etName.getText().toString().trim();

        Log.d(TAG, "Starting registration process for email: " + email);

        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            showError("Пожалуйста, заполните все поля");
            return;
        }

        if (password.length() < 6) {
            showError("Пароль должен содержать минимум 6 символов");
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Firebase Auth success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        saveUserDataToFirestore(user.getUid(), name, email);
                    }
                } else {
                    Log.e(TAG, "Firebase Auth failed", task.getException());
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnRegister.setEnabled(true);
                    
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        showError("Этот email уже зарегистрирован. Пожалуйста, используйте другой email или войдите в систему.");
                    } else {
                        showError("Ошибка регистрации: " + task.getException().getMessage());
                    }
                }
            });
    }

    private void saveUserDataToFirestore(String userId, String name, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("role", "student");

        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "User data saved to Firestore");
                verifyUserData(userId);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error saving user data to Firestore", e);
                verifyUserData(userId);
            });
    }

    private void verifyUserData(String userId) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Log.d(TAG, "User data verified in Firestore");
                    navigateToStudentMain();
                } else {
                    Log.e(TAG, "User data not found in Firestore after save attempt");
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnRegister.setEnabled(true);
                    showError("Ошибка при сохранении данных. Пожалуйста, попробуйте войти в систему.");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error verifying user data", e);
                navigateToStudentMain();
            });
    }

    private void navigateToStudentMain() {
        try {
            Log.d(TAG, "Navigating to student main fragment");
            binding.progressBar.setVisibility(View.GONE);
            binding.btnRegister.setEnabled(true);
            Navigation.findNavController(requireView())
                .navigate(R.id.action_studentRegisterFragment_to_studentMainFragment);
        } catch (Exception e) {
            Log.e(TAG, "Navigation failed", e);
            binding.progressBar.setVisibility(View.GONE);
            binding.btnRegister.setEnabled(true);
            Toast.makeText(requireContext(), 
                "Ошибка при переходе в главное меню", 
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
        binding = null;
    }
} 