package com.islamiccompanion.app.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.islamiccompanion.app.data.db.entity.TasbihEntry;

import java.util.List;

/** DAO for Tasbih (dhikr counter) daily session records. */
@Dao
public interface TasbihDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(TasbihEntry entry);

    @Query("SELECT * FROM tasbih_entries WHERE date = :date")
    List<TasbihEntry> getEntriesForDate(String date);

    @Query("SELECT SUM(count) FROM tasbih_entries WHERE date = :date")
    int getTotalForDate(String date);

    @Query("SELECT * FROM tasbih_entries ORDER BY date DESC LIMIT 30")
    LiveData<List<TasbihEntry>> getRecentEntries();
}
