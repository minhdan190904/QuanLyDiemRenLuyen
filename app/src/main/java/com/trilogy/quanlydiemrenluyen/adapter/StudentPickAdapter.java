package com.trilogy.quanlydiemrenluyen.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trilogy.quanlydiemrenluyen.R;
import com.trilogy.quanlydiemrenluyen.model.Student;

import java.util.ArrayList;
import java.util.List;

public class StudentPickAdapter extends RecyclerView.Adapter<StudentPickAdapter.Holder> {

    public interface OnStudentClick {
        void onViewScore(Student s);
    }

    private final OnStudentClick listener;
    private final List<Student> data = new ArrayList<>();

    public StudentPickAdapter(OnStudentClick listener) {
        this.listener = listener;
    }

    public void submit(List<Student> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_pick, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Student s = data.get(position);
        h.tvStudentTitle.setText(s.student_id + " - " + s.name);
        h.tvStudentSubtitle.setText(s.clazz);

        // avatar có thể load sau, tạm dùng ic_avatar_placeholder

        h.btnView.setOnClickListener(v -> {
            if (listener != null) listener.onViewScore(s);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvStudentTitle, tvStudentSubtitle;
        ImageView ivAvatar;
        Button btnView;

        Holder(@NonNull View v) {
            super(v);
            ivAvatar = v.findViewById(R.id.ivAvatar);
            tvStudentTitle = v.findViewById(R.id.tvStudentTitle);
            tvStudentSubtitle = v.findViewById(R.id.tvStudentSubtitle);
            btnView = v.findViewById(R.id.btnView);
        }
    }
}
