package com.example.mylibrary.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.mylibrary.R;
import com.google.android.material.card.MaterialCardView;

public class RoleSelectionFragment extends Fragment {
    private MaterialCardView btnStudent;
    private MaterialCardView btnAdmin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_role_selection, container, false);

        btnStudent = view.findViewById(R.id.btnStudent);
        btnAdmin = view.findViewById(R.id.btnAdmin);

        btnStudent.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_roleSelectionFragment_to_studentLoginFragment)
        );

        btnAdmin.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_roleSelectionFragment_to_adminLoginFragment)
        );

        return view;
    }
} 