package com.tinymission.tinysync.db;

import org.joda.time.DateTime;

/**
 * Base class for model classes that map to a database table.
 */
public abstract class DbModel {

    public enum SyncState {
        ALIVE, NEW, DEAD
    }

    @DbColumn()
    public ObjectId id;

    @DbColumn()
    public DateTime createdAt;

    @DbColumn()
    public DateTime updatedAt;

    @DbColumn()
    public SyncState syncState = SyncState.NEW;

    public DbModel() {
        createdAt = DateTime.now();
        id = new ObjectId();
    }
}
