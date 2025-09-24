package com.trilogy.quanlydiemrenluyen.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.trilogy.quanlydiemrenluyen.R;
import com.trilogy.quanlydiemrenluyen.db.DatabaseHelper;

import java.util.ArrayList;

public class StudentScoreActivity extends AppCompatActivity {
    DatabaseHelper db;

    EditText edtKey;
    TextView tvName, tvId, tvClazz;
    ListView list;
    ArrayList<String> rows = new ArrayList<>();
    ArrayAdapter<String> adapter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_score);
        db = new DatabaseHelper(this);

        edtKey = findViewById(R.id.edtKey);
        Button btnFind = findViewById(R.id.btnFind);
        tvName = findViewById(R.id.tvName);
        tvId = findViewById(R.id.tvId);
        tvClazz = findViewById(R.id.tvClazz);
        list = findViewById(R.id.listScores);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rows);
        list.setAdapter(adapter);

        String sidFromLogin = getIntent().getStringExtra("student_id");
        if (!TextUtils.isEmpty(sidFromLogin)) {
            // Ẩn ô tìm kiếm, hiển thị trực tiếp
            edtKey.setText(sidFromLogin);
            edtKey.setEnabled(false);
            loadStudentAndScores(sidFromLogin);
        }

        btnFind.setOnClickListener(v -> {
            String key = edtKey.getText().toString().trim();
            String sid = db.findStudentIdByNameOrId(key);
            if (sid == null) {
                Toast.makeText(this, "Không tìm thấy sinh viên", Toast.LENGTH_SHORT).show();
            } else {
                loadStudentAndScores(sid);
            }
        });
    }

    private void loadStudentAndScores(String sid) {
        // info
        try (Cursor c = db.getStudentInfo(sid)) {
            if (c.moveToFirst()) {
                tvId.setText(c.getString(c.getColumnIndexOrThrow("student_id")));
                tvName.setText(c.getString(c.getColumnIndexOrThrow("name")));
                tvClazz.setText(c.getString(c.getColumnIndexOrThrow("clazz")));
            }
        }
        // scores
        rows.clear();
        try (Cursor c2 = db.getScoresByStudent(sid)) {
            while (c2.moveToNext()) {
                String sem = c2.getString(0);
                int score = c2.getInt(1);
                String note = c2.getString(2);
                String time = c2.getString(3);
                rows.add(sem + " - " + score + (TextUtils.isEmpty(note) ? "" : (" (" + note + ")")) + " • " + time);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
