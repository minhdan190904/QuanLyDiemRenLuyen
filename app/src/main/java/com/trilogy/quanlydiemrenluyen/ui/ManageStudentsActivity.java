package com.trilogy.quanlydiemrenluyen.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trilogy.quanlydiemrenluyen.R;
import com.trilogy.quanlydiemrenluyen.adapter.StudentAdapter;
import com.trilogy.quanlydiemrenluyen.db.DatabaseHelper;
import com.trilogy.quanlydiemrenluyen.model.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManageStudentsActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private EditText edtSearch;
    private RecyclerView recycler;
    private StudentAdapter adapter;
    private View fabAdd;
    private ImageView btnBack;

    private final List<Student> allStudents = new ArrayList<>();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long DEBOUNCE_MS = 300L;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_students);

        db = new DatabaseHelper(this);

        edtSearch = findViewById(R.id.edtSearch);
        recycler = findViewById(R.id.recyclerStudents);
        fabAdd = findViewById(R.id.fabAdd);
        btnBack = findViewById(R.id.imageViewBack);

        adapter = new StudentAdapter(new StudentAdapter.OnItemAction() {
            @Override public void onItemClick(Student student) {

                showActionsDialog(student.student_id);
            }
            @Override public void onEdit(Student student) {
                showAddOrEditDialog(student.student_id, student.name, student.clazz);
            }
            @Override public void onDelete(Student student) {
                confirmAndDelete(student.student_id);
            }
        });

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        loadAllStudents();

        // debounce search (local filter)
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                final String q = s == null ? "" : s.toString().trim();
                if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
                searchRunnable = () -> filterLocal(q);
                handler.postDelayed(searchRunnable, DEBOUNCE_MS);
            }
        });

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                filterLocal(edtSearch.getText() == null ? "" : edtSearch.getText().toString().trim());
                return true;
            }
            return false;
        });

        fabAdd.setOnClickListener(v -> showAddOrEditDialog(null, null, null));
        btnBack.setOnClickListener(v -> finish());
    }

    @Override protected void onResume() {
        super.onResume();
        loadAllStudents();
        filterLocal(edtSearch.getText() == null ? "" : edtSearch.getText().toString().trim());
    }

    private void loadAllStudents() {
        allStudents.clear();
        try (Cursor c = db.getStudents(null)) {
            while (c != null && c.moveToNext()) {
                Student s = new Student();
                s.student_id = c.getString(c.getColumnIndexOrThrow("student_id"));
                s.name = c.getString(c.getColumnIndexOrThrow("name"));
                s.clazz = c.getString(c.getColumnIndexOrThrow("clazz"));
                allStudents.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        adapter.setData(allStudents);
    }

    private void filterLocal(String q) {
        if (TextUtils.isEmpty(q)) {
            adapter.setData(allStudents);
            return;
        }
        String key = q.toLowerCase(Locale.getDefault());
        List<Student> filtered = new ArrayList<>();
        for (Student s : allStudents) {
            if ((s.student_id != null && s.student_id.toLowerCase(Locale.getDefault()).contains(key))
                    || (s.name != null && s.name.toLowerCase(Locale.getDefault()).contains(key))) {
                filtered.add(s);
            }
        }
        adapter.setData(filtered);
    }

    private void confirmAndDelete(String studentId) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa sinh viên")
                .setMessage("Bạn có chắc muốn xóa MSSV: " + studentId + " ?")
                .setPositiveButton("Xóa", (d, w) -> {
                    boolean ok = db.deleteStudent(studentId);
                    Toast.makeText(ManageStudentsActivity.this, ok ? "Đã xóa" : "Không xóa được", Toast.LENGTH_SHORT).show();
                    loadAllStudents();
                    filterLocal(edtSearch.getText() == null ? "" : edtSearch.getText().toString().trim());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // auxiliary: shows same actions dialog as backup for item click
    private void showActionsDialog(String sid) {
        new AlertDialog.Builder(this)
                .setTitle("Chọn thao tác")
                .setItems(new CharSequence[]{"Sửa", "Xóa"}, (dialog, which) -> {
                    if (which == 0) {
                        String name = null, clazz = null;
                        try (Cursor c = db.getStudents(sid)) {
                            while (c != null && c.moveToNext()) {
                                if (sid.equals(c.getString(c.getColumnIndexOrThrow("student_id")))) {
                                    name = c.getString(c.getColumnIndexOrThrow("name"));
                                    clazz = c.getString(c.getColumnIndexOrThrow("clazz"));
                                    break;
                                }
                            }
                        }
                        showAddOrEditDialog(sid, name, clazz);
                    } else {
                        confirmAndDelete(sid);
                    }
                }).show();
    }

    private void showAddOrEditDialog(String id, String name, String clazz) {
        LayoutInflater inf = LayoutInflater.from(this);
        View view = inf.inflate(R.layout.dialog_student, null, false);

        EditText edtId   = view.findViewById(R.id.edtId);
        EditText edtName = view.findViewById(R.id.edtName);
        Spinner spClazz  = view.findViewById(R.id.spClazz);

        // load lớp -> spinner
        ArrayList<String> classList = new ArrayList<>();
        try (Cursor c = db.getAllClasses()) {
            while (c != null && c.moveToNext()) classList.add(c.getString(0));
        }
        if (classList.isEmpty()) {
            classList.add("K18CNTT1");
            classList.add("K18CNTT2");
        }
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classList);
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spClazz.setAdapter(classAdapter);

        if (!TextUtils.isEmpty(id)) {
            edtId.setText(id);
            edtId.setEnabled(false);
        } else {
            edtId.setEnabled(true);
        }
        if (name != null) edtName.setText(name);
        if (clazz != null) {
            int idx = classList.indexOf(clazz);
            if (idx >= 0) spClazz.setSelection(idx);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(TextUtils.isEmpty(id) ? "Thêm sinh viên" : "Sửa sinh viên")
                .setView(view)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", (d, w) -> { })
                .create();

        dialog.setOnShowListener(d -> {
            Button b = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            b.setOnClickListener(v -> {
                String sid = edtId.getText().toString().trim();
                String n   = edtName.getText().toString().trim();
                String cl  = (String) spClazz.getSelectedItem();

                if (sid.isEmpty() || n.isEmpty() || cl == null) {
                    Toast.makeText(ManageStudentsActivity.this, "Nhập MSSV, Họ tên và chọn Lớp", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean ok = TextUtils.isEmpty(id) ? db.addStudent(sid, n, cl)
                        : db.updateStudent(sid, n, cl);
                Toast.makeText(ManageStudentsActivity.this, ok ? "Đã lưu" : "Lỗi lưu", Toast.LENGTH_SHORT).show();
                if (ok) {
                    dialog.dismiss();
                    loadAllStudents();
                    filterLocal(edtSearch.getText() == null ? "" : edtSearch.getText().toString().trim());
                }
            });
        });

        dialog.show();
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            View v = getCurrentFocus();
            if (v != null && imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        } catch (Exception ignored) {}
    }
}
