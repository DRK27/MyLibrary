package com.example.mylibrary.ui.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.mylibrary.R;
import com.example.mylibrary.databinding.FragmentStudentMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentMainFragment extends Fragment {
    private FragmentStudentMainBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String name = documentSnapshot.getString("name");
                    if (name != null && binding != null) {
                        binding.tvWelcome.setText("Добро пожаловать, " + name + "!");
                    }
                });
        }

        binding.btnBrowseBooks.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_studentMainFragment_to_browseBooksFragment)
        );

        binding.btnMyBooks.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_studentMainFragment_to_myBooksFragment)
        );

        binding.btnProfile.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_studentMainFragment_to_studentProfileFragment)
        );

        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Navigation.findNavController(v).navigate(R.id.action_studentMainFragment_to_roleSelectionFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 