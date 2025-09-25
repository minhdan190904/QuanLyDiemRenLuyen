package com.trilogy.quanlydiemrenluyen.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trilogy.quanlydiemrenluyen.R;
import com.trilogy.quanlydiemrenluyen.adapter.StudentSelectAdapter;
import com.trilogy.quanlydiemrenluyen.db.DatabaseHelper;
import com.trilogy.quanlydiemrenluyen.model.Student;

import java.util.ArrayList;
import java.util.List;

public class UpdateScoreActivity extends AppCompatActivity implements StudentSelectAdapter.OnSelect {
    private DatabaseHelper db;

    private EditText edtSearch, edtScore, edtNote;
    private Spinner spSemester;
    private TextView tvChosenId, tvChosenName, tvChosenClazz;
    private Button btnUpdate;

    private StudentSelectAdapter adapter;
    private String selectedStudentId = null;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_score);

        db = new DatabaseHelper(this);

        // list + search
        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentSelectAdapter(this);
        rv.setAdapter(adapter);

        edtSearch = findViewById(R.id.edtSearch);
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { loadStudents(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        // spinner semester
        spSemester = findViewById(R.id.spSemester);
        loadSemesters();

        // chosen student + inputs
        tvChosenId    = findViewById(R.id.tvChosenId);
        tvChosenName  = findViewById(R.id.tvChosenName);
        tvChosenClazz = findViewById(R.id.tvChosenClazz);
        edtScore = findViewById(R.id.edtScore);
        edtNote  = findViewById(R.id.edtNote);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(v -> doUpdate());

        loadStudents(null);
    }

    private void loadStudents(String keyword) {
        List<Student> list = new ArrayList<>();
        try (Cursor c = db.getStudents(keyword)) {
            while (c.moveToNext()) {
                Student s = new Student();
                s.student_id = c.getString(c.getColumnIndexOrThrow("student_id"));
                s.name       = c.getString(c.getColumnIndexOrThrow("name"));
                s.clazz      = c.getString(c.getColumnIndexOrThrow("clazz"));
                list.add(s);
            }
        }
        adapter.submit(list);
    }

    private void loadSemesters() {
        List<String> semesters = new ArrayList<>();
        try (Cursor c = db.getAllSemesters()) {
            while (c.moveToNext()) semesters.add(c.getString(0));
        }
        ArrayAdapter<String> a = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, semesters);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSemester.setAdapter(a);
    }

    @Override public void onSelect(Student s) {
        selectedStudentId = s.student_id;
        tvChosenId.setText(s.student_id);
        tvChosenName.setText(s.name);
        tvChosenClazz.setText(s.clazz);
    }

    private void doUpdate() {
        if (TextUtils.isEmpty(selectedStudentId)) {
            Toast.makeText(this, "Hãy chọn 1 sinh viên từ danh sách", Toast.LENGTH_SHORT).show();
            return;
        }
        if (spSemester.getSelectedItem() == null) {
            Toast.makeText(this, "Danh sách học kỳ trống", Toast.LENGTH_SHORT).show();
            return;
        }

        String sem = String.valueOf(spSemester.getSelectedItem());
        String scStr = edtScore.getText().toString().trim();
        if (TextUtils.isEmpty(scStr)) {
            Toast.makeText(this, "Nhập điểm", Toast.LENGTH_SHORT).show();
            return;
        }
        int score = Integer.parseInt(scStr);
        if (score < 0 || score > 100) {
            Toast.makeText(this, "Điểm phải trong khoảng 0–100!", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean ok = db.addOrUpdateScore(selectedStudentId, sem, score, edtNote.getText().toString().trim());
        Toast.makeText(this, ok ? "Đã cập nhật" : "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
    }
}
