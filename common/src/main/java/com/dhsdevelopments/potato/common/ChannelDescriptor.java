package com.dhsdevelopments.potato.common;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@SuppressWarnings("WeakerAccess")
@Entity(tableName = "channels",
        foreignKeys = @ForeignKey(entity = DomainDescriptor.class,
                parentColumns = "id",
                childColumns = "domain_id"),
        indices = @Index("domain_id"))
public class ChannelDescriptor {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public String id = "";

    @ColumnInfo(name = "name")
    @NonNull
    public String name = "";

    @ColumnInfo(name = "private_user")
    public String privateUser = null;

    @ColumnInfo(name = "hidden")
    public boolean hidden = false;

    @ColumnInfo(name = "domain_id")
    @NonNull
    public String domainId = "";

    @ColumnInfo(name = "unread")
    public int unreadCount;

    public ChannelDescriptor() {
    }

    @Ignore
    public ChannelDescriptor(@NonNull String id, @NonNull String name, String privateUser, boolean hidden, @NonNull String domainId, int unreadCount) {
        this.id = id;
        this.name = name;
        this.privateUser = privateUser;
        this.hidden = hidden;
        this.domainId = domainId;
        this.unreadCount = unreadCount;
    }
}
