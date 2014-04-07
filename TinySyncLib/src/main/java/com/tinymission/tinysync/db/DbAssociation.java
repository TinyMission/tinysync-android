package com.tinymission.tinysync.db;

import java.lang.reflect.Field;

/**
 * Stores information about a one-many association between two Fields.
 */
public class DbAssociation {

    public DbAssociation(Field oneField, Field manyField, String foreignKey) {
        _oneField = oneField;
        _manyField = manyField;
        _foreignKey = foreignKey;
    }

    private Field _oneField;

    public Field getOneField() {
        return _oneField;
    }

    private Field _manyField;

    public Field getManyField() {
        return _manyField;
    }

    private String _foreignKey;

    private DbCollection<?> _oneCollection = null;

    public DbCollection<?> getOneCollection() {
        return _oneCollection;
    }

    public void setOneCollection(DbCollection<?> collection) {
        _oneCollection = collection;
    }

    private DbCollection<?> _manyCollection = null;

    public DbCollection<?> getManyCollection() {
        return _manyCollection;
    }

    public void setManyCollection(DbCollection<?> collection) {
        _manyCollection = collection;
    }

}
