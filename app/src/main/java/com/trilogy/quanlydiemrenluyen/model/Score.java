package com.trilogy.quanlydiemrenluyen.model;

public class Score {
    public String semester;
    public int score;
    public String note;
    public String updated_at;

    public Score() {}
    public Score(String semester, int score, String note, String updated_at) {
        this.semester = semester; this.score = score; this.note = note; this.updated_at = updated_at;
    }
}
