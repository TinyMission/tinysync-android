package com.tinymission.tinysync.validation;

import com.tinymission.tinysync.db.DbModel;

/**
 * Contains a single error for a record.
 */
public class RecordError {

    public RecordError(DbModel record, String propertyName, String message) {
        _record = record;
        _propertyName = propertyName;
        _message = message;
    }

    public RecordError(DbModel record, String propertyName, Exception exception) {
        _record = record;
        _propertyName = propertyName;
        _message = exception.getMessage();
        _exception = exception;
    }

    private DbModel _record;

    public DbModel getRecord() {
        return _record;
    }

    private String _propertyName;

    public String getPropertyName() {
        return _propertyName;
    }

    private String _message;

    public String getMessage() {
        return _message;
    }

    private Exception _exception;

    public Exception getException() {
        return _exception;
    }


}
