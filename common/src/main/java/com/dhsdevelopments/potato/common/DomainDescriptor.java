package com.dhsdevelopments.potato.common;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@SuppressWarnings("WeakerAccess")
@Entity(tableName = "domains")
public class DomainDescriptor {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public String id = "";

    @ColumnInfo(name = "name")
    @NonNull
    public String name = "";

    public DomainDescriptor() {
    }

    public DomainDescriptor(@NonNull String id, @NonNull String name) {
        this.id = id;
        this.name = name;
    }
}
