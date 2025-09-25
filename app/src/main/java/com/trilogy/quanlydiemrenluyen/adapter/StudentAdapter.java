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


public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.VH> {

    public interface OnItemAction {
        void onItemClick(Student student);
        void onEdit(Student student);
        void onDelete(Student student);
    }

    private final OnItemAction listener;
    private final List<Student> data = new ArrayList<>();

    public StudentAdapter(OnItemAction listener) {
        this.listener = listener;
    }

    public void setData(List<Student> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH holder, int position) {
        Student s = data.get(position);
        String title = (s.student_id == null ? "" : s.student_id) + " - " + (s.name == null ? "" : s.name);
        holder.title.setText(title);
        holder.subtitle.setText(s.clazz != null ? s.clazz : "");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(s);
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(s);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(s);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, subtitle;
        Button btnEdit, btnDelete;
        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.tvStudentTitle);
            subtitle = v.findViewById(R.id.tvStudentSubtitle);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
