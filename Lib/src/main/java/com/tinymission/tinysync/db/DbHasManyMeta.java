package com.tinymission.tinysync.db;

import com.google.common.base.CaseFormat;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Contains meta information about the association.
 */
public class DbHasManyMeta {
    public DbHasManyMeta(Field field, DbModel template) {
        _field = field;
        try {
            DbHasMany hasMany = (DbHasMany)field.get(template);
            _modelClass = hasMany.getModelClass();
        }
        catch (Exception ex) {
            throw new RuntimeException("Error getting HasMany meta data for " + field.getName() + ": " + ex.getMessage());
        }

        // compute the foreign key
        for (Annotation ann: field.getAnnotations()) {
            if (ann instanceof DbForeignKey) {
                _foreignKey = ((DbForeignKey) ann).value();
            }
        }
        if (_foreignKey == null) {
            String className = field.getDeclaringClass().getSimpleName();
            _foreignKey = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, className) + "_id";
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

    private String _foreignKey;

    public String getForeignKey() {
        return _foreignKey;
    }
}
