package com.tinymission.tinysync.validation;

import com.tinymission.tinysync.db.DbCollection;
import com.tinymission.tinysync.db.DbModel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Validates the uniqueness of a record.
 */
public class UniquenessValidator extends FieldValidator {
    @Override
    protected boolean validate(DbCollection collection, DbModel record, Field field, Object value) {
        return true; // TODO: implement uniqueness validation
    }

    @Override
    public void populateFromAnnotation(Annotation annotation) {
        ValidateUniqueness ann = safeCastAnnotation(annotation);
        setMessage(ann.message());
    }
}
