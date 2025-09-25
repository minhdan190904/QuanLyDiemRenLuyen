package com.trilogy.quanlydiemrenluyen.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.trilogy.quanlydiemrenluyen.R;
import com.trilogy.quanlydiemrenluyen.adapter.ClassAdapter;
import com.trilogy.quanlydiemrenluyen.db.DatabaseHelper;

import java.util.ArrayList;

public class ManageClassesActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private ArrayList<String> classes = new ArrayList<>();
    private ClassAdapter adapter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_classes);

        db = new DatabaseHelper(this);

        RecyclerView rv = findViewById(R.id.recyclerClasses);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClassAdapter(classes, this::onDeleteClass);
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddClass);
        fab.setOnClickListener(v -> showAddDialog());

        ImageView back = findViewById(R.id.imageViewBack);
        back.setOnClickListener(v -> finish());

        load();
    }

    private void showAddDialog() {
        var view = LayoutInflater.from(this).inflate(R.layout.dialog_add_class, null, false);
        EditText edt = view.findViewById(R.id.edtClassName);
        new AlertDialog.Builder(this)
                .setTitle("Thêm lớp")
                .setView(view)
                .setPositiveButton("Lưu", (d, w) -> {
                    String name = edt.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Tên lớp trống", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean ok = db.addClass(name);
                    Toast.makeText(this, ok ? "Đã thêm" : "Đã tồn tại/Không thêm được", Toast.LENGTH_SHORT).show();
                    load();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void onDeleteClass(String name) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa lớp?")
                .setMessage("Bạn có chắc muốn xóa lớp: " + name + " ?")
                .setPositiveButton("Xóa", (d, w) -> {
                    boolean ok = db.deleteClass(name);
                    Toast.makeText(this, ok ? "Đã xóa" : "Không xóa được", Toast.LENGTH_SHORT).show();
                    load();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void load() {
        classes.clear();
        try (var c = db.getAllClasses()) {
            while (c.moveToNext()) classes.add(c.getString(0));
        }
        adapter.notifyDataSetChanged();
    }
}
