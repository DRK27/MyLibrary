package com.example.mylibrary.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.mylibrary.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class StudentLoginFragment extends Fragment {
    private TextInputEditText etLogin;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private Button btnRegister;
    private TextView tvError;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_login, container, false);

        etLogin = view.findViewById(R.id.etStudentLogin);
        etPassword = view.findViewById(R.id.etStudentPassword);
        btnLogin = view.findViewById(R.id.btnStudentLogin);
        btnRegister = view.findViewById(R.id.btnStudentRegister);
        tvError = view.findViewById(R.id.tvStudentError);

        btnLogin.setOnClickListener(v -> {
            String email = etLogin.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                showError("Пожалуйста, заполните все поля");
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Успешный вход", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(v).navigate(R.id.action_studentLoginFragment_to_studentMainFragment);
                    } else {
                        showError("Неверный email или пароль");
                    }
                });
        });

        btnRegister.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_studentLoginFragment_to_studentRegisterFragment)
        );

        return view;
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
} 