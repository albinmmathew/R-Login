package com.example.sampleproject_rlogin;

public class SubjectItem {
    public String name;
    public String code;

    public double attended;
    public String pct;
    public int totalHeld;

    public double dutyAttended;
    public String dutyPct;
    public int dutyTotalHeld;

    public SubjectItem(String name, String code, double attended, String pct, int totalHeld,
                       double dutyAttended, String dutyPct, int dutyTotalHeld) {
        this.name = name;
        this.code = code;
        this.attended = attended;
        this.pct = pct;
        this.totalHeld = totalHeld;
        this.dutyAttended = dutyAttended;
        this.dutyPct = dutyPct;
        this.dutyTotalHeld = dutyTotalHeld;
    }
}
