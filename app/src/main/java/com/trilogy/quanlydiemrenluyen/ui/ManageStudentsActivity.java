package com.trilogy.quanlydiemrenluyen.ui;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.trilogy.quanlydiemrenluyen.R;
import com.trilogy.quanlydiemrenluyen.db.DatabaseHelper;

import java.util.ArrayList;

public class ManageStudentsActivity extends AppCompatActivity {

    DatabaseHelper db;
    EditText edtSearch;
    ListView listView;
    ArrayList<String> items = new ArrayList<>();
    ArrayList<String> ids = new ArrayList<>();
    ArrayAdapter<String> adapter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_students);
        db = new DatabaseHelper(this);

        edtSearch = findViewById(R.id.edtSearch);
        listView = findViewById(R.id.listStudents);
        Button btnSearch = findViewById(R.id.btnSearch);
        Button btnAdd = findViewById(R.id.btnAdd);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> load(edtSearch.getText().toString().trim()));
        btnAdd.setOnClickListener(v -> showAddOrEditDialog(null, null, null));

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String sid = ids.get(position);
            showActionsDialog(sid);
        });
    }

    @Override protected void onResume() {
        super.onResume();
        load(null);
    }

    private void load(String key) {
        items.clear(); ids.clear();
        try (Cursor c = db.getStudents(key)) {
            while (c.moveToNext()) {
                String sid = c.getString(c.getColumnIndexOrThrow("student_id"));
                String name = c.getString(c.getColumnIndexOrThrow("name"));
                String clazz = c.getString(c.getColumnIndexOrThrow("clazz"));
                ids.add(sid);
                items.add(sid + " - " + name + " (" + clazz + ")");
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showActionsDialog(String sid) {
        new AlertDialog.Builder(this)
                .setTitle("Chọn thao tác")
                .setItems(new CharSequence[]{"Sửa", "Xóa"}, (d, w) -> {
                    if (w == 0) { // edit
                        // load sv
                        try (Cursor c = db.getStudents(sid)) {
                            String name = null, clazz = null;
                            while (c.moveToNext()) {
                                if (sid.equals(c.getString(c.getColumnIndexOrThrow("student_id")))) {
                                    name = c.getString(c.getColumnIndexOrThrow("name"));
                                    clazz = c.getString(c.getColumnIndexOrThrow("clazz"));
                                    break;
                                }
                            }
                            showAddOrEditDialog(sid, name, clazz);
                        }
                    } else { // delete
                        boolean ok = db.deleteStudent(sid);
                        Toast.makeText(this, ok ? "Đã xóa" : "Không xóa được", Toast.LENGTH_SHORT).show();
                        load(edtSearch.getText().toString().trim());
                    }
                }).show();
    }

    private void showAddOrEditDialog(String id, String name, String clazz) {
        LayoutInflater inf = LayoutInflater.from(this);
        var view = inf.inflate(R.layout.dialog_student, null, false);
        EditText edtId = view.findViewById(R.id.edtId);
        EditText edtName = view.findViewById(R.id.edtName);
        EditText edtClazz = view.findViewById(R.id.edtClazz);

        if (!TextUtils.isEmpty(id)) {
            edtId.setText(id);
            edtId.setEnabled(false);
        }
        if (name != null) edtName.setText(name);
        if (clazz != null) edtClazz.setText(clazz);

        new AlertDialog.Builder(this)
                .setTitle(TextUtils.isEmpty(id) ? "Thêm sinh viên" : "Sửa sinh viên")
                .setView(view)
                .setPositiveButton("Lưu", (d, w) -> {
                    String sid = edtId.getText().toString().trim();
                    String n = edtName.getText().toString().trim();
                    String cl = edtClazz.getText().toString().trim();
                    boolean ok = TextUtils.isEmpty(id)
                            ? db.addStudent(sid, n, cl)
                            : db.updateStudent(sid, n, cl);
                    Toast.makeText(this, ok ? "Đã lưu" : "Lỗi lưu", Toast.LENGTH_SHORT).show();
                    load(edtSearch.getText().toString().trim());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
