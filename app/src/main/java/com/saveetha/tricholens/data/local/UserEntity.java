package com.saveetha.tricholens.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey
    public int id;
    public String name;
    public String email;
    public String mobile;
    public String dob;
    public String gender;
    public String age;
    public long loggedInAt;

    public UserEntity() {
        this.dob = null;
    }

    @Ignore
    public UserEntity(int id, String name, String email, String mobile, String dob, String gender, String age,
            long loggedInAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.dob = dob;
        this.gender = gender;
        this.age = age;
        this.loggedInAt = loggedInAt;
    }
}
