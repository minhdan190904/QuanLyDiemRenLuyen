package com.trilogy.quanlydiemrenluyen.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.trilogy.quanlydiemrenluyen.R;

public class AdminActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Button btnManage = findViewById(R.id.btnManageStudents);
        Button btnUpdate = findViewById(R.id.btnUpdateScore);
        Button btnView = findViewById(R.id.btnViewScore);

        btnManage.setOnClickListener(v -> startActivity(new Intent(this, ManageStudentsActivity.class)));
        btnUpdate.setOnClickListener(v -> startActivity(new Intent(this, UpdateScoreActivity.class)));
        btnView.setOnClickListener(v -> startActivity(new Intent(this, StudentScoreActivity.class)));
    }
}
