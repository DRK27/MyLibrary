package com.example.mylibrary.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.mylibrary.R;
import com.example.mylibrary.databinding.FragmentAdminMainBinding;

public class AdminMainFragment extends Fragment {
    private FragmentAdminMainBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnManageBooks.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_adminMainFragment_to_manageBooksFragment)
        );

        binding.btnManageStudents.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_adminMainFragment_to_manageStudentsFragment)
        );

        binding.btnBorrowedBooks.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_adminMainFragment_to_borrowedBooksFragment)
        );

        binding.btnStatistics.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_adminMainFragment_to_statisticsFragment)
        );

        binding.btnAdminProfile.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_adminMainFragment_to_adminProfileFragment)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 