package com.trilogy.quanlydiemrenluyen.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "drl.db";
    private static final int DB_VERSION = 2; // bump vì thêm semesters + classes

    // tables
    public static final String T_USERS     = "users";
    public static final String T_STUDENTS  = "students";
    public static final String T_SCORES    = "scores";
    public static final String T_SEMESTERS = "semesters";
    public static final String T_CLASSES   = "classes";

    public DatabaseHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // USERS
        db.execSQL("CREATE TABLE " + T_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE," +
                "password TEXT," +
                "role TEXT," +
                "student_id TEXT)");

        // STUDENTS
        db.execSQL("CREATE TABLE " + T_STUDENTS + " (" +
                "student_id TEXT PRIMARY KEY," +
                "name TEXT," +
                "clazz TEXT)");

        // SCORES
        db.execSQL("CREATE TABLE " + T_SCORES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "student_id TEXT," +
                "semester TEXT," +
                "score INTEGER," +
                "note TEXT," +
                "updated_at TEXT," +
                "FOREIGN KEY(student_id) REFERENCES " + T_STUDENTS + "(student_id))");

        // SEMESTERS
        db.execSQL("CREATE TABLE " + T_SEMESTERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT UNIQUE)");

        // CLASSES
        db.execSQL("CREATE TABLE " + T_CLASSES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT UNIQUE)");

        seedInitialData(db);
    }

    private void seedInitialData(SQLiteDatabase db) {
        // admin
        ContentValues v = new ContentValues();
        v.put("username", "admin");
        v.put("password", "admin");
        v.put("role", "admin");
        v.putNull("student_id");
        db.insert(T_USERS, null, v);

        // some classes
        addClassInternal(db, "K18CNTT1");
        addClassInternal(db, "K18CNTT2");
        addClassInternal(db, "K18CNDL1");

        // sample students
        ContentValues sv1 = new ContentValues();
        sv1.put("student_id", "SV001"); sv1.put("name", "Nguyen Van A"); sv1.put("clazz", "K18CNTT1");
        ContentValues sv2 = new ContentValues();
        sv2.put("student_id", "SV002"); sv2.put("name", "Tran Thi B");  sv2.put("clazz", "K18CNTT2");
        db.insert(T_STUDENTS, null, sv1);
        db.insert(T_STUDENTS, null, sv2);

        // sample user (student)
        ContentValues u1 = new ContentValues();
        u1.put("username", "sv001"); u1.put("password", "123"); u1.put("role", "student"); u1.put("student_id", "SV001");
        db.insert(T_USERS, null, u1);
        ContentValues u2 = new ContentValues();
        u2.put("username", "sv002"); u2.put("password", "123"); u2.put("role", "student"); u2.put("student_id", "SV002");
        db.insert(T_USERS, null, u2);

        // seed semesters from current year to 2030 (HK1, HK2)
        int yearNow = Calendar.getInstance().get(Calendar.YEAR);
        int endYear = 2030;
        for (int y = yearNow; y <= endYear; y++) {
            for (int hk = 1; hk <= 2; hk++) {
                ContentValues sem = new ContentValues();
                sem.put("name", y + "-" + (y + 1) + " HK" + hk);
                db.insert(T_SEMESTERS, null, sem);
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        if (oldV < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + T_SEMESTERS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE)");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + T_CLASSES   + " (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE)");
            // seed semesters if empty
            try (Cursor c = db.rawQuery("SELECT COUNT(1) FROM " + T_SEMESTERS, null)) {
                if (c.moveToFirst() && c.getInt(0) == 0) {
                    int yearNow = Calendar.getInstance().get(Calendar.YEAR);
                    int endYear = 2030;
                    for (int y = yearNow; y <= endYear; y++) {
                        for (int hk = 1; hk <= 2; hk++) {
                            ContentValues sem = new ContentValues();
                            sem.put("name", y + "-" + (y + 1) + " HK" + hk);
                            db.insert(T_SEMESTERS, null, sem);
                        }
                    }
                }
            }
        }
    }

    // ===== USERS =====
    public Cursor getUser(String username, String password) {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM " + T_USERS + " WHERE username=? AND password=?",
                new String[]{username, password});
    }

    // ===== STUDENTS =====
    public boolean addStudent(String id, String name, String clazz) {
        ContentValues v = new ContentValues();
        v.put("student_id", id);
        v.put("name", name);
        v.put("clazz", clazz);
        long r = getWritableDatabase().insertWithOnConflict(T_STUDENTS, null, v, SQLiteDatabase.CONFLICT_IGNORE);
        return r != -1;
    }

    public boolean updateStudent(String id, String name, String clazz) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("clazz", clazz);
        int r = getWritableDatabase().update(T_STUDENTS, v, "student_id=?", new String[]{id});
        return r > 0;
    }

    public boolean deleteStudent(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(T_SCORES, "student_id=?", new String[]{id});
        int r = db.delete(T_STUDENTS, "student_id=?", new String[]{id});
        return r > 0;
    }

    /** keyword null/rỗng -> lấy tất cả; tìm theo id hoặc name (LIKE) */
    public Cursor getStudents(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            return getReadableDatabase().rawQuery(
                    "SELECT * FROM " + T_STUDENTS + " ORDER BY student_id", null);
        } else {
            String k = "%" + keyword + "%";
            return getReadableDatabase().rawQuery(
                    "SELECT * FROM " + T_STUDENTS +
                            " WHERE student_id LIKE ? OR name LIKE ? ORDER BY student_id",
                    new String[]{k, k});
        }
    }

    public String findStudentIdByNameOrId(String q) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT student_id FROM " + T_STUDENTS + " WHERE student_id=? OR name LIKE ? LIMIT 1",
                new String[]{q, "%" + q + "%"});
        try {
            if (c.moveToFirst()) return c.getString(0);
            return null;
        } finally { c.close(); }
    }

    // ===== SCORES =====
    public boolean addOrUpdateScore(String studentId, String semester, int score, String note) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM " + T_SCORES + " WHERE student_id=? AND semester=?",
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
        return getReadableDatabase().rawQuery(
                "SELECT semester, score, note, updated_at FROM " + T_SCORES +
                        " WHERE student_id=? ORDER BY semester",
                new String[]{studentId});
    }

    public Cursor getStudentInfo(String studentId) {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM " + T_STUDENTS + " WHERE student_id=?",
                new String[]{studentId});
    }

    // ===== SEMESTERS =====
    public Cursor getAllSemesters() {
        return getReadableDatabase().rawQuery(
                "SELECT name FROM " + T_SEMESTERS + " ORDER BY name", null);
    }

    // ===== CLASSES =====
    private void addClassInternal(SQLiteDatabase db, String name) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        db.insertWithOnConflict(T_CLASSES, null, v, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public boolean addClass(String name) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        long r = getWritableDatabase().insertWithOnConflict(T_CLASSES, null, v, SQLiteDatabase.CONFLICT_IGNORE);
        return r != -1;
    }

    public boolean deleteClass(String name) {
        int r = getWritableDatabase().delete(T_CLASSES, "name=?", new String[]{name});
        return r > 0;
    }

    public Cursor getAllClasses() {
        return getReadableDatabase().rawQuery(
                "SELECT name FROM " + T_CLASSES + " ORDER BY name", null);
    }
}
