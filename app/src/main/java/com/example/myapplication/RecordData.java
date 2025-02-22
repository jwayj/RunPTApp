package com.example.myapplication;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "datas")
public class RecordData {

    @PrimaryKey(autoGenerate = true)
    public int id;

    //단위: sec
    @ColumnInfo(name="running_time")
    public int time;

    //단위: m
    @ColumnInfo(name="running_distance")
    public double distance;


    @ColumnInfo(name="running_pace")
    public double pace;

    @ColumnInfo(name="insertion_data")
    public Long insertionDate;
}
