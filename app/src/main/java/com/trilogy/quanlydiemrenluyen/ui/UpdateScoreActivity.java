package com.trilogy.quanlydiemrenluyen.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.trilogy.quanlydiemrenluyen.R;
import com.trilogy.quanlydiemrenluyen.db.DatabaseHelper;

public class UpdateScoreActivity extends AppCompatActivity {
    DatabaseHelper db;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_score);
        db = new DatabaseHelper(this);

        EditText edtKey = findViewById(R.id.edtKey);
        EditText edtSemester = findViewById(R.id.edtSemester);
        EditText edtScore = findViewById(R.id.edtScore);
        EditText edtNote = findViewById(R.id.edtNote);
        Button btnUpdate = findViewById(R.id.btnUpdate);

        btnUpdate.setOnClickListener(v -> {
            String key = edtKey.getText().toString().trim();
            String sid = db.findStudentIdByNameOrId(key);
            if (sid == null) {
                Toast.makeText(this, "Không tìm thấy sinh viên theo khóa nhập", Toast.LENGTH_SHORT).show();
                return;
            }
            String sem = edtSemester.getText().toString().trim();
            String scStr = edtScore.getText().toString().trim();
            if (TextUtils.isEmpty(sem) || TextUtils.isEmpty(scStr)) {
                Toast.makeText(this, "Nhập học kỳ và điểm", Toast.LENGTH_SHORT).show();
                return;
            }
            int sc = Integer.parseInt(scStr);
            boolean ok = db.addOrUpdateScore(sid, sem, sc, edtNote.getText().toString().trim());
            Toast.makeText(this, ok ? "Đã cập nhật" : "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
        });
    }
}
