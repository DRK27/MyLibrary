package com.example.mylibrary.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mylibrary.databinding.ItemStudentBinding;
import com.example.mylibrary.model.Student;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {
    private List<Student> students = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStudentBinding binding = ItemStudentBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new StudentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        holder.bind(students.get(position));
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public void setStudents(List<Student> students) {
        this.students = students;
        notifyDataSetChanged();
    }

    class StudentViewHolder extends RecyclerView.ViewHolder {
        private final ItemStudentBinding binding;

        StudentViewHolder(ItemStudentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Student student) {
            binding.tvName.setText(student.getName());
            binding.tvEmail.setText(student.getEmail());
            binding.tvStudentId.setText("ID: " + student.getStudentId());
            binding.tvPhone.setText(student.getPhone());
            
            binding.switchActive.setChecked(student.isActive());
            binding.switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                student.setActive(isChecked);
                updateStudentStatus(student);
            });
        }

        private void updateStudentStatus(Student student) {
            db.collection("students")
                .document(student.getId())
                .update("active", student.isActive())
                .addOnFailureListener(e -> {
                    binding.switchActive.setChecked(!student.isActive());
                    student.setActive(!student.isActive());
                });
        }
    }
} 