package com.islamiccompanion.app.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.islamiccompanion.app.data.db.entity.Bookmark;
import com.islamiccompanion.app.data.db.entity.ReadingProgress;

import java.util.List;

/** DAO for Quran bookmarks and reading progress. */
@Dao
public interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBookmark(Bookmark bookmark);

    @Delete
    void deleteBookmark(Bookmark bookmark);

    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    LiveData<List<Bookmark>> getAllBookmarks();

    @Query("SELECT * FROM bookmarks WHERE surahNumber = :surah AND ayahNumber = :ayah LIMIT 1")
    Bookmark getBookmark(int surah, int ayah);

    @Query("DELETE FROM bookmarks WHERE surahNumber = :surah AND ayahNumber = :ayah")
    void deleteByAyah(int surah, int ayah);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveReadingProgress(ReadingProgress progress);

    @Query("SELECT * FROM reading_progress WHERE id = 1 LIMIT 1")
    ReadingProgress getReadingProgress();
}
