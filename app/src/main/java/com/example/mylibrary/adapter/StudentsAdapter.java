package com.example.mylibrary.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mylibrary.databinding.ItemStudentBinding;
import com.example.mylibrary.model.Student;
import java.util.List;

public class StudentsAdapter extends RecyclerView.Adapter<StudentsAdapter.StudentViewHolder> {
    private final List<Student> students;
    private OnStudentActionListener listener;

    public interface OnStudentActionListener {
        void onViewBorrowedBooks(Student student);
        void onBlockStudent(Student student);
        void onUnblockStudent(Student student);
    }

    public StudentsAdapter(List<Student> students) {
        this.students = students;
    }

    public void setOnStudentActionListener(OnStudentActionListener listener) {
        this.listener = listener;
    }

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

    class StudentViewHolder extends RecyclerView.ViewHolder {
        private final ItemStudentBinding binding;

        StudentViewHolder(ItemStudentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Student student) {
            binding.tvName.setText(student.getName());
            binding.tvEmail.setText(student.getEmail());
            binding.tvStudentId.setText("ID: " + student.getId());
            
            if (student.getPhone() != null && !student.getPhone().isEmpty()) {
                binding.tvPhone.setText("Телефон: " + student.getPhone());
                binding.tvPhone.setVisibility(ViewGroup.VISIBLE);
            } else {
                binding.tvPhone.setVisibility(ViewGroup.GONE);
            }

            binding.switchActive.setChecked(student.isActive());
            binding.switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (listener != null) {
                        listener.onUnblockStudent(student);
                    }
                } else {
                    if (listener != null) {
                        listener.onBlockStudent(student);
                    }
                }
            });

            binding.btnViewBooks.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewBorrowedBooks(student);
                }
            });

            binding.btnBlock.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBlockStudent(student);
                }
            });

            binding.btnUnblock.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUnblockStudent(student);
                }
            });

            binding.btnBlock.setVisibility(student.isActive() ? ViewGroup.VISIBLE : ViewGroup.GONE);
            binding.btnUnblock.setVisibility(student.isActive() ? ViewGroup.GONE : ViewGroup.VISIBLE);
        }
    }
} 