package com.tinymission.tinysync.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.common.base.CaseFormat;
import com.tinymission.tinysync.serializers.DbSerializer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Contains meta information about a belongs-to relationship.
 */
public class DbBelongsToMeta {

    public DbBelongsToMeta(Field field, DbModel template) {
        _field = field;
        try {
            DbBelongsTo belongsTo = (DbBelongsTo)field.get(template);
            _modelClass = belongsTo.getModelClass();
        }
        catch (Exception ex) {
            throw new RuntimeException("Error getting BelongsTo meta data for " + field.getName() + ": " + ex.getMessage());
        }

        // compute the column name
        for (Annotation ann: field.getAnnotations()) {
            if (ann instanceof DbColumn) {
                _columnName = ((DbColumn) ann).name();
            }
        }
        if (_columnName == null) {
            String className = field.getName();
            _columnName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, className) + "_id";
        }
    }

    private Field _field;

    public Field getField() {
        return _field;
    }

    private Class<?> _modelClass;

    public Class<?> getModelClass() {
        return _modelClass;
    }

    private String _columnName;

    public String getColumnName() {
        return _columnName;
    }

    /**
     * @return the SQL column definition for this relationship's column
     */
    public String getColumnDef() {
        return _columnName + " TEXT";
    }


    public void assignContentValue(DbModel record, ContentValues contentValues) throws IllegalAccessException {
        DbBelongsTo belongsTo = (DbBelongsTo) _field.get(record);
        contentValues.put(_columnName, String.valueOf(belongsTo.getKey()));
    }

    public void deserializeColumn(Cursor cursor, DbModel record, int index) throws IllegalAccessException {
        DbBelongsTo belongsTo = (DbBelongsTo) _field.get(record);
        ObjectId key = new ObjectId(cursor.getString(index));
        belongsTo.setKey(key);
    }
}
