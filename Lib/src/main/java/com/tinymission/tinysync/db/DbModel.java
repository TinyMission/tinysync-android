package com.tinymission.tinysync.db;

import android.util.Log;

import com.google.common.base.Joiner;
import com.tinymission.tinysync.validation.RecordError;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base class for model classes that map to a database table.
 */
public abstract class DbModel {

    public enum SyncState {
        created, synced, updated, deleted
    }

    @DbColumn()
    public ObjectId id;

    @DbColumn()
    public DateTime createdAt;

    @DbColumn()
    public DateTime updatedAt;

    @DbColumn()
    public SyncState syncState = SyncState.created;

    boolean _persisted = false;

    /**
     * True if the record is in the database.
     */
    public boolean isPersisted() {
        return _persisted;
    }

    /**
     * Only use this if you know what you're doing.
     * @param persisted
     * @return
     */
    public void setIsPersisted(boolean persisted) {
        _persisted = persisted;
    }

    public DbModel() {
        createdAt = DateTime.now();
        id = new ObjectId();
    }


    //region Validation

    private ArrayList<RecordError> _errors = new ArrayList<RecordError>();

    public List<RecordError> getErrors() {
        return _errors;
    }

    public void addError(RecordError error) {
        _errors.add(error);
    }

    public void addError(String propertyName, String message) {
        _errors.add(new RecordError(this, propertyName, message));
    }

    public void addError(String propertyName, Exception ex) {
        _errors.add(new RecordError(this, propertyName, ex));
    }

    public boolean hasErrors() {
        return _errors.size() > 0;
    }

    void clearErrors() {
        _errors.clear();
    }

    public String getErrorMessage() {
        return Joiner.on(", ").join(_errors);
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

    /**
     * Subclasses can override this method to implement custom validation.
     */
    public void onValidate() {

    }

    //endregion

}
