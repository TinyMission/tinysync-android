package com.tinymission.tinysync.validation;

import com.tinymission.tinysync.db.DbCollection;
import com.tinymission.tinysync.db.DbModel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Validates that a field value is not null.
 */
public class NotNullValidator extends FieldValidator {

    @Override
    public boolean validate(DbCollection collection, DbModel record, Field field, Object value) {
        if (value == null) {
            record.addError(field.getName(), getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void populateFromAnnotation(Annotation annotation) {
        ValidateNotNull ann = safeCastAnnotation(annotation);
        setMessage(ann.message());
    }
}
