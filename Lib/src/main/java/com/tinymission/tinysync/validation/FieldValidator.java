package com.tinymission.tinysync.validation;

import com.tinymission.tinysync.db.DbCollection;
import com.tinymission.tinysync.db.DbModel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Base class for all classes that perform validation on a model field.
 */
public abstract class FieldValidator {

    protected abstract boolean validate(DbCollection collection, DbModel record, Field field, Object value);

    public boolean validate(DbCollection collection, DbModel record) {
        try {
            Object value = _field.get(record);
            return validate(collection, record, _field, value);
        }
        catch (Exception ex) {
            record.addError(_field.getName(), "is not present");
            return false;
        }
    }

    Field _field;

    public Field getField() {
        return _field;
    }

    public void setField(Field _field) {
        this._field = _field;
    }

    String message = "failed validation";

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public abstract void populateFromAnnotation(Annotation annotation);

    protected <T> T safeCastAnnotation(Annotation ann) {
        return (T)ann;
    }
}
