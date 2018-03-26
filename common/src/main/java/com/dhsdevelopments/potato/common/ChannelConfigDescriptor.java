package com.dhsdevelopments.potato.common;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@SuppressWarnings("WeakerAccess")
@Entity(tableName = "channel_config")
public class ChannelConfigDescriptor {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public String channelId = "";

    @ColumnInfo(name = "show_notification")
    public boolean showNotification = false;

    @ColumnInfo(name = "show_unread")
    public boolean showUnread = false;

    public ChannelConfigDescriptor() {
    }

    public ChannelConfigDescriptor(@NonNull String channelId, boolean showNotification, boolean showUnread) {
        this.channelId = channelId;
        this.showNotification = showNotification;
        this.showUnread = showUnread;
    }
}
