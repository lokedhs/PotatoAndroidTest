package com.dhsdevelopments.potato.common;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "channels",
        foreignKeys = @ForeignKey(entity = DomainDescriptor.class,
                parentColumns = "id",
                childColumns = "domain_id"),
        indices = @Index("domain_id"))
//        foreignKeys = [ForeignKey(entity = DomainDescriptor::class,
//        parentColumns = ["id"],
//        childColumns = ["domain_id"],
//        indices = arrayOf(Index("domain_id"))]
public class ChannelDescriptor {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public String id = "";
    @ColumnInfo(name = "name")
    public String name = "";
    @ColumnInfo(name = "private_user")
    public String privateUser = null;
    @ColumnInfo(name = "hidden")
    public boolean hidden = false;
    @ColumnInfo(name = "domain_id")
    public String domainId = "";
    @ColumnInfo(name = "unread")
    public int unreadCount;

    public ChannelDescriptor() {
    }

    @Ignore
    public ChannelDescriptor(@NonNull String id, String name, String privateUser, boolean hidden, String domainId, int unreadCount) {
        this.id = id;
        this.name = name;
        this.privateUser = privateUser;
        this.hidden = hidden;
        this.domainId = domainId;
        this.unreadCount = unreadCount;
    }
}
