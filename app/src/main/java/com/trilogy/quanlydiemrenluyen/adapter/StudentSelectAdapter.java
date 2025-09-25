package com.trilogy.quanlydiemrenluyen.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trilogy.quanlydiemrenluyen.R;
import com.trilogy.quanlydiemrenluyen.model.Student;

import java.util.ArrayList;
import java.util.List;

public class StudentSelectAdapter extends RecyclerView.Adapter<StudentSelectAdapter.VH> {

    public interface OnSelect {
        void onSelect(Student s);
    }

    private final OnSelect listener;
    private final List<Student> data = new ArrayList<>();

    public StudentSelectAdapter(OnSelect l) {
        this.listener = l;
    }

    public void submit(List<Student> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_pick, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Student s = data.get(pos);

        // hiển thị: MSSV - Tên
        h.tvTitle.setText(s.student_id + " - " + s.name);
        // hiển thị: Lớp
        h.tvSubtitle.setText(s.clazz);

        // đổi nút "Xem" thành "Chọn"
        h.btnView.setText("Chọn");

        h.btnView.setOnClickListener(v -> listener.onSelect(s));
        h.itemView.setOnClickListener(v -> listener.onSelect(s));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;
        Button btnView;

        VH(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvStudentTitle);
            tvSubtitle = v.findViewById(R.id.tvStudentSubtitle);
            btnView = v.findViewById(R.id.btnView);
        }
    }
}
