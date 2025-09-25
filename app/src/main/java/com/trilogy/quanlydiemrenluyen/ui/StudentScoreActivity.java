package com.trilogy.quanlydiemrenluyen.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.trilogy.quanlydiemrenluyen.R;
import com.trilogy.quanlydiemrenluyen.adapter.StudentPickAdapter;
import com.trilogy.quanlydiemrenluyen.db.DatabaseHelper;
import com.trilogy.quanlydiemrenluyen.model.Student;

import java.util.ArrayList;
import java.util.List;

public class StudentScoreActivity extends AppCompatActivity implements StudentPickAdapter.OnStudentClick {

    private DatabaseHelper db;
    private StudentPickAdapter adapter;
    private EditText edtSearch;
    private TextView tvEmpty;
    private ImageView imageViewBack;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long DEBOUNCE_MS = 300L;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_score);

        db = new DatabaseHelper(this);


        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentPickAdapter(this);
        rv.setAdapter(adapter);

        edtSearch = findViewById(R.id.edtSearch);
        tvEmpty = findViewById(R.id.tvEmpty);
        imageViewBack = findViewById(R.id.imageViewBack);

        imageViewBack.setOnClickListener(v -> finish());

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                // no-op (we debounce in afterTextChanged)
            }
            @Override public void afterTextChanged(Editable s) {
                final String k = s == null ? "" : s.toString().trim();
                if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
                searchRunnable = () -> load(k.isEmpty() ? null : k);
                handler.postDelayed(searchRunnable, DEBOUNCE_MS);
            }
        });

        load(null);
    }

    private void load(String keyword) {
        List<Student> list = new ArrayList<>();
        try (Cursor c = db.getStudents(keyword)) {
            while (c.moveToNext()) {
                Student s = new Student();
                s.student_id = c.getString(c.getColumnIndexOrThrow("student_id"));
                s.name      = c.getString(c.getColumnIndexOrThrow("name"));
                s.clazz     = c.getString(c.getColumnIndexOrThrow("clazz"));
                list.add(s);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        adapter.submit(list);
        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override public void onViewScore(Student s) {
        List<String> rows = new ArrayList<>();
        try (Cursor c = db.getScoresByStudent(s.student_id)) {
            while (c.moveToNext()) {
                String sem  = c.getString(0);
                int score   = c.getInt(1);
                String note = c.getString(2);
                String time = c.getString(3);
                rows.add(sem + " — " + score + (note == null || note.isEmpty() ? "" : (" ("+note+")")) + " • " + time);
            }
        }
        if (rows.isEmpty()) {
            Toast.makeText(this, "SV chưa có bản ghi điểm", Toast.LENGTH_SHORT).show();
            return;
        }
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Điểm rèn luyện\n" + s.student_id + " - " + s.name + " (" + s.clazz + ")")
                .setItems(rows.toArray(new CharSequence[0]), null)
                .setPositiveButton("Đóng", null)
                .show();
    }
}
