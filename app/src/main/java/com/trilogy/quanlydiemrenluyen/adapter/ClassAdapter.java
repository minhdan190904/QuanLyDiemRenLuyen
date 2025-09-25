package com.trilogy.quanlydiemrenluyen.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trilogy.quanlydiemrenluyen.R;

import java.util.ArrayList;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.VH> {
    public interface OnDeleteListener { void onDelete(String name); }

    private final ArrayList<String> data;
    private final OnDeleteListener listener;

    public ClassAdapter(ArrayList<String> list, OnDeleteListener l) {
        this.data = list;
        this.listener = l;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_class, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        String name = data.get(pos);
        h.tvClassName.setText(name);
        h.itemView.setOnLongClickListener(v -> {
            listener.onDelete(name);
            return true;
        });
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvClassName;
        VH(View v) {
            super(v);
            tvClassName = v.findViewById(R.id.tvClassName);
        }
    }
}
