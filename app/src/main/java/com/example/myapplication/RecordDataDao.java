package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RecordDataDao {

    // 단일 레코드 삽입 (성공 시 새로 삽입된 행의 ID를 반환)
    @Insert
    long insertRecord(RecordData record);

    // 여러 레코드 한 번에 삽입
    @Insert
    List<Long> insertRecords(List<RecordData> records);

    // 레코드 수정 (영향받은 행의 수를 반환)
    @Update
    int updateRecord(RecordData record);

    // 레코드 삭제 (영향받은 행의 수를 반환)
    @Delete
    int deleteRecord(RecordData record);

    // 모든 레코드 조회
    @Query("SELECT * FROM datas ORDER BY id DESC")
    List<RecordData> getAllRecords();

    // 특정 id로부터 레코드 조회
    @Query("SELECT * FROM datas WHERE id = :recordId")
    RecordData getRecordById(int recordId);

    /*@Query("SELECT * FROM datas ORDER BY id DESC LIMIT 1")
    RecordData getLastRecord(int recordId);*/

    @Query("SELECT * FROM datas ORDER BY id DESC LIMIT 1")
    RecordData getLastRecord();

    // 전체 레코드 삭제
    @Query("DELETE FROM datas")
    void deleteAllRecords();

}
