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
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.mylibrary.R;
import com.example.mylibrary.databinding.FragmentManageStudentsBinding;
import com.example.mylibrary.adapter.StudentsAdapter;
import com.example.mylibrary.model.Student;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ManageStudentsFragment extends Fragment implements StudentsAdapter.OnStudentActionListener {
    private static final String TAG = "ManageStudentsFragment";
    private FragmentManageStudentsBinding binding;
    private FirebaseFirestore db;
    private StudentsAdapter adapter;
    private List<Student> studentsList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        studentsList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageStudentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        loadStudents();
    }

    private void setupRecyclerView() {
        adapter = new StudentsAdapter(studentsList);
        adapter.setOnStudentActionListener(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void loadStudents() {
        binding.progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Starting to load users from Firestore");
        
        db.collection("users")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d(TAG, "Successfully got documents from Firestore. Size: " + queryDocumentSnapshots.size());
                studentsList.clear();
                
                if (queryDocumentSnapshots.isEmpty()) {
                    Log.d(TAG, "No documents found in users collection");
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), 
                        "Список пользователей пуст", 
                        Toast.LENGTH_SHORT).show();
                    return;
                }

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    try {
                        Log.d(TAG, "Processing document with ID: " + document.getId());
                        Map<String, Object> data = document.getData();
                        Log.d(TAG, "Document data: " + data.toString());
                        
                        Student student = new Student();
                        student.setId(document.getId());
                        
                        String name = (String) data.get("name");
                        String email = (String) data.get("email");
                        
                        if (name == null || email == null) {
                            Log.e(TAG, "Missing required fields for user " + document.getId());
                            continue;
                        }
                        
                        student.setName(name);
                        student.setEmail(email);
                        
                        Object activeObj = data.get("active");
                        if (activeObj != null) {
                            student.setActive((Boolean) activeObj);
                            Log.d(TAG, "User " + name + " active status: " + activeObj);
                        } else {
                            student.setActive(true);
                            Log.d(TAG, "User " + name + " active status not set, defaulting to true");
                        }

                        Object phoneObj = data.get("phone");
                        if (phoneObj != null) {
                            student.setPhone((String) phoneObj);
                            Log.d(TAG, "User " + name + " phone: " + phoneObj);
                        }

                        studentsList.add(student);
                        Log.d(TAG, "Successfully added user to list: " + student.getName());
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing user data for document " + document.getId(), e);
                    }
                }
                
                Log.d(TAG, "Finished processing all documents. Total users added: " + studentsList.size());
                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
                
                if (studentsList.isEmpty()) {
                    Toast.makeText(requireContext(), 
                        "Список пользователей пуст", 
                        Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), 
                        "Загружено пользователей: " + studentsList.size(), 
                        Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading users from Firestore", e);
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), 
                    "Ошибка при загрузке списка пользователей: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public void onViewBorrowedBooks(Student student) {
        Toast.makeText(requireContext(), 
            "Просмотр книг пользователя: " + student.getName(), 
            Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBlockStudent(Student student) {
        db.collection("users").document(student.getId())
            .update("active", false)
            .addOnSuccessListener(aVoid -> {
                student.setActive(false);
                adapter.notifyDataSetChanged();
                Toast.makeText(requireContext(), 
                    "Пользователь заблокирован", 
                    Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), 
                    "Ошибка при блокировке пользователя: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public void onUnblockStudent(Student student) {
        db.collection("users").document(student.getId())
            .update("active", true)
            .addOnSuccessListener(aVoid -> {
                student.setActive(true);
                adapter.notifyDataSetChanged();
                Toast.makeText(requireContext(), 
                    "Пользователь разблокирован", 
                    Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), 
                    "Ошибка при разблокировке пользователя: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 