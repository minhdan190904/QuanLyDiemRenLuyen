/* GIỮ NGUYÊN – chính là file bạn gửi */
package com.trilogy.quanlydiemrenluyen.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "drl.db";
    private static final int DB_VERSION = 1;

    // tables
    public static final String T_USERS = "users";
    public static final String T_STUDENTS = "students";
    public static final String T_SCORES = "scores";

    public DatabaseHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE," +
                "password TEXT," +
                "role TEXT," +
                "student_id TEXT)");

        db.execSQL("CREATE TABLE " + T_STUDENTS + " (" +
                "student_id TEXT PRIMARY KEY," +
                "name TEXT," +
                "clazz TEXT)");

        db.execSQL("CREATE TABLE " + T_SCORES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "student_id TEXT," +
                "semester TEXT," +
                "score INTEGER," +
                "note TEXT," +
                "updated_at TEXT," +
                "FOREIGN KEY(student_id) REFERENCES " + T_STUDENTS + "(student_id))");

        // seed admin + vài SV mẫu
        ContentValues v = new ContentValues();
        v.put("username", "admin");
        v.put("password", "admin");
        v.put("role", "admin");
        v.putNull("student_id");
        db.insert(T_USERS, null, v);

        ContentValues sv1 = new ContentValues();
        sv1.put("student_id", "SV001"); sv1.put("name", "Nguyen Van A"); sv1.put("clazz", "K18CNTT1");
        ContentValues sv2 = new ContentValues();
        sv2.put("student_id", "SV002"); sv2.put("name", "Tran Thi B");  sv2.put("clazz", "K18CNTT2");

        db.insert(T_STUDENTS, null, sv1);
        db.insert(T_STUDENTS, null, sv2);

        // tạo tài khoản sinh viên
        ContentValues u1 = new ContentValues();
        u1.put("username", "sv001"); u1.put("password", "123"); u1.put("role", "student"); u1.put("student_id", "SV001");
        db.insert(T_USERS, null, u1);

        ContentValues u2 = new ContentValues();
        u2.put("username", "sv002"); u2.put("password", "123"); u2.put("role", "student"); u2.put("student_id", "SV002");
        db.insert(T_USERS, null, u2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + T_SCORES);
        db.execSQL("DROP TABLE IF EXISTS " + T_STUDENTS);
        db.execSQL("DROP TABLE IF EXISTS " + T_USERS);
        onCreate(db);
    }

    // ===== USERS =====
    public Cursor getUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + T_USERS + " WHERE username=? AND password=?",
                new String[]{username, password});
    }

    // ===== STUDENTS =====
    public boolean addStudent(String id, String name, String clazz) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("student_id", id);
        v.put("name", name);
        v.put("clazz", clazz);
        long r = db.insertWithOnConflict(T_STUDENTS, null, v, SQLiteDatabase.CONFLICT_IGNORE);
        return r != -1;
    }

    public boolean updateStudent(String id, String name, String clazz) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("clazz", clazz);
        int r = db.update(T_STUDENTS, v, "student_id=?", new String[]{id});
        return r > 0;
    }

    public boolean deleteStudent(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(T_SCORES, "student_id=?", new String[]{id}); // xóa điểm liên quan
        int r = db.delete(T_STUDENTS, "student_id=?", new String[]{id});
        return r > 0;
    }

    public Cursor getStudents(String keyword) {
        SQLiteDatabase db = getReadableDatabase();
        if (TextUtils.isEmpty(keyword)) {
            return db.rawQuery("SELECT * FROM " + T_STUDENTS + " ORDER BY student_id", null);
        } else {
            String k = "%" + keyword + "%";
            return db.rawQuery("SELECT * FROM " + T_STUDENTS +
                            " WHERE student_id LIKE ? OR name LIKE ? ORDER BY student_id",
                    new String[]{k, k});
        }
    }

    public String findStudentIdByNameOrId(String q) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT student_id FROM " + T_STUDENTS +
                        " WHERE student_id=? OR name LIKE ? LIMIT 1",
                new String[]{q, "%" + q + "%"});
        try {
            if (c.moveToFirst()) return c.getString(0);
            return null;
        } finally { c.close(); }
    }

    // ===== SCORES =====
    public boolean addOrUpdateScore(String studentId, String semester, int score, String note) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM " + T_SCORES +
                        " WHERE student_id=? AND semester=?",
                new String[]{studentId, semester});
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        try {
            ContentValues v = new ContentValues();
            v.put("student_id", studentId);
            v.put("semester", semester);
            v.put("score", score);
            v.put("note", note);
            v.put("updated_at", now);
            if (c.moveToFirst()) {
                int id = c.getInt(0);
                int r = db.update(T_SCORES, v, "id=?", new String[]{String.valueOf(id)});
                return r > 0;
            } else {
                long r = db.insert(T_SCORES, null, v);
                return r != -1;
            }
        } finally { c.close(); }
    }

    public Cursor getScoresByStudent(String studentId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT semester, score, note, updated_at FROM " + T_SCORES +
                " WHERE student_id=? ORDER BY semester", new String[]{studentId});
    }

    public Cursor getStudentInfo(String studentId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + T_STUDENTS + " WHERE student_id=?",
                new String[]{studentId});
    }
}
