package com.saveetha.tricholens.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users ORDER BY loggedInAt DESC LIMIT 1")
    UserEntity getLastLoggedInUser();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserEntity user);

    @Query("DELETE FROM users")
    void clearAll();
}
