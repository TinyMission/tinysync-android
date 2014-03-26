package com.tinymission.tinysync.db;

import android.util.Log;

import com.tinymission.tinysync.validation.RecordError;

import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class for model classes that map to a database table.
 */
public abstract class DbModel {

    public enum SyncState {
        infant, alive, dead
    }

    @DbColumn()
    public ObjectId id;

    @DbColumn()
    public DateTime createdAt;

    @DbColumn()
    public DateTime updatedAt;

    @DbColumn()
    public SyncState syncState = SyncState.infant;

    boolean _persisted = false;

    /**
     * True if the record is in the database.
     */
    public boolean isPersisted() {
        return _persisted;
    }

    public DbModel() {
        createdAt = DateTime.now();
        id = new ObjectId();
    }


    //region Validation

    private HashSet<RecordError> _errors = new HashSet<RecordError>();

    public Set<RecordError> getErrors() {
        return _errors;
    }

    public void addError(RecordError error) {
        _errors.add(error);
    }

    public boolean hasErrors() {
        return _errors.size() > 0;
    }

    public void logErrors() {
        String modelName = getClass().getSimpleName();
        String logTag = "tinysync.db.DbModel: " + modelName;
        for (RecordError error: _errors) {
            if (error.getException() == null)
                Log.w(logTag, "Error saving " + modelName + ": " + error.getMessage());
            else
                Log.w(logTag, "Error saving " + modelName, error.getException());
        }
    }


    //endregion

}
