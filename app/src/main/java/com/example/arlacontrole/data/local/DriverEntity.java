package com.example.arlacontrole.data.local;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "drivers", indices = {@Index(value = {"name"}, unique = true)})
public class DriverEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name;

    @ColumnInfo(name = "active")
    public boolean active;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    public DriverEntity(@NonNull String name, boolean active, long updatedAt) {
        this.name = name;
        this.active = active;
        this.updatedAt = updatedAt;
    }
}
