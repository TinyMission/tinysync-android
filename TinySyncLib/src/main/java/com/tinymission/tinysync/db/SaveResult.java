package com.tinymission.tinysync.db;

import java.util.HashSet;
import java.util.Set;

/**
 * Stores information about an attempt to save some records to the database.
 */
public class SaveResult {

    private HashSet<DbModel> _inserted = new HashSet<DbModel>();
    private HashSet<DbModel> _updated = new HashSet<DbModel>();
    private HashSet<DbModel> _errored = new HashSet<DbModel>();

    void addInserted(DbModel record) {
        _inserted.add(record);
    }

    public Set<DbModel> getInserted() {
        return _inserted;
    }

    void addUpdated(DbModel record) {
        _updated.add(record);
    }

    public Set<DbModel> getUpdated() {
        return _updated;
    }

    void addErrored(DbModel record) {
        _errored.add(record);
    }

    public Set<DbModel> getErrored() {
        return _errored;
    }



    /**
     * Merges all information from other into this.
     */
    public void mergeFrom(SaveResult other) {
        _inserted.addAll(other._inserted);
        _updated.addAll(other._updated);
        _errored.addAll(other._errored);
    }

}
