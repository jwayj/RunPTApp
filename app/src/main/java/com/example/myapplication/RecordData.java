package com.example.myapplication;

public class RecordData {
    private String id;
    private long insertionDate;
    private int time;
    private double distance;

    // 생성자, getter, setter
    public RecordData() {}

    public RecordData(String id, long insertionDate, int time, double distance) {
        this.id = id;
        this.insertionDate = insertionDate;
        this.time = time;
        this.distance = distance;
    }

    // getter, setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getInsertionDate() { return insertionDate; }
    public void setInsertionDate(long insertionDate) { this.insertionDate = insertionDate; }

    public int getTime() { return time; }
    public void setTime(int time) { this.time = time; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }
}

