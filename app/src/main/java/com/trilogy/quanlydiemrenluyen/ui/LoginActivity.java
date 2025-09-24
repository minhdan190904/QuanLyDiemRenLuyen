package com.trilogy.quanlydiemrenluyen.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.trilogy.quanlydiemrenluyen.R;
import com.trilogy.quanlydiemrenluyen.db.DatabaseHelper;

public class LoginActivity extends AppCompatActivity {
    DatabaseHelper db;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        db = new DatabaseHelper(this);

        EditText user = findViewById(R.id.edtUser);
        EditText pass = findViewById(R.id.edtPass);
        Button btn = findViewById(R.id.btnLogin);

        btn.setOnClickListener(v -> {
            try (Cursor c = db.getUser(user.getText().toString().trim(),
                    pass.getText().toString().trim())) {
                if (c.moveToFirst()) {
                    String role = c.getString(c.getColumnIndexOrThrow("role"));
                    String sid  = c.getString(c.getColumnIndexOrThrow("student_id"));
                    if ("admin".equalsIgnoreCase(role)) {
                        startActivity(new Intent(this, AdminActivity.class));
                    } else {
                        Intent i = new Intent(this, StudentScoreActivity.class);
                        i.putExtra("student_id", sid);
                        startActivity(i);
                    }
                } else {
                    Toast.makeText(this, "Sai tài khoản/mật khẩu", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
