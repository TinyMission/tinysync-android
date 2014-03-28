package com.tinymission.tinysync.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.common.base.CaseFormat;
import com.tinymission.tinysync.query.Query;
import com.tinymission.tinysync.validation.FieldValidation;
import com.tinymission.tinysync.validation.FieldValidator;
import com.tinymission.tinysync.validation.RecordError;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides an interface to query and persist records to a single table.
 */
public class DbCollection<T extends DbModel> {
    private static final String LogTag = "tinysync.db.DbCollection";

    public DbCollection(Class<T> modelClass) {
        _modelClass = modelClass;
        _tableName = tableizeClassName(modelClass.getSimpleName());
        Log.v(LogTag, "Parsing columns for table " + _tableName + " (" + modelClass.getName() + ")");

        for (Field field: modelClass.getFields()) {
            for (Annotation ann: field.getDeclaredAnnotations()) {
                if (ann instanceof DbColumn) {
                    DbColumnMap columnMap = new DbColumnMap((DbColumn)ann, field);
                    _columnMaps.put(columnMap.getColumnName(), columnMap);
                    Log.v(LogTag, "  " + field.getName() + " is a column named " + columnMap.getColumnName() + " of type " + field.getType());
                }
                else {
                    Annotation[] metaAnns = ann.annotationType().getAnnotations();
                    for (Annotation metaAnn: metaAnns) {
                        if (metaAnn instanceof FieldValidation) {
                            Class<?> validatorType = ((FieldValidation) metaAnn).value();
                            addFieldValidator(field, ann, validatorType);
                            break;
                        }
                    }
                }
            }
        }
    }

    private Class<T> _modelClass;

    private DbContext _context;

    public void setContext(DbContext context) {
        _context = context;
    }


    //region Schema

    private String _tableName;
    /**
     * @return the name of the table that this set maps to
     */
    public String getTableName() {
        return _tableName;
    }

    /**
     * Converts a class name into something that's appropriate to use for a table.
     * @param name a PascalCase class name
     * @return
     */
    public static String tableizeClassName(String name) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
    }

    private HashMap<String,DbColumnMap> _columnMaps = new HashMap<String, DbColumnMap>();

    /**
     * @return the column definitions, for use with CREATE TABLE.
     */
    public List<String> getColumnDefs() {
        ArrayList<String> defs = new ArrayList<String>();
        for (DbColumnMap columnMap: _columnMaps.values()) {
            defs.add(columnMap.getColumnDef());
        }
        return defs;
    }

    private String[] _columnNames = null;

    private void readColumnNames(SQLiteDatabase db) {
        if (_columnNames != null) return;
        ArrayList<String> names = new ArrayList<String>();
        Cursor ti = db.rawQuery("PRAGMA table_info(" + _tableName + ")", null);
        while (ti.moveToNext()) {
            names.add(ti.getString(1));
        }
        _columnNames = names.toArray(new String[names.size()]);
    }

    //endregion


    //region Persistence

    private HashSet<T> _newRecords = new HashSet<T>();

    /**
     * @return all unpersisted records that have been added to the set since it was last saved or cleared
     */
    public Set<T> getNew() {
        return _newRecords;
    }

    private HashSet<T> _changedRecords = new HashSet<T>();

    /**
     * @return all persisted records that have been added to the set since it was last saved or cleared
     */
    public Set<T> getChanged() {
        return _changedRecords;
    }

    /**
     * Adds a new or existing record to be persisted when save() is called.
     */
    public void add(T record) {
        if (record.isPersisted())
            _changedRecords.add(record);
        else
            _newRecords.add(record);
    }

    private ContentValues contentValuesForRecord(T record) {
        ContentValues values = new ContentValues(_columnMaps.size());
        for (DbColumnMap columnMap : _columnMaps.values()) {
            try {
                columnMap.assignContentValue(record, values);
            }
            catch (Exception ex) {
                record.addError(new RecordError(record, columnMap.getColumnName(), ex));
            }
        }
        return values;
    }

    private boolean insertRecord(SQLiteDatabase db, T record) {
        try {
            ContentValues values = contentValuesForRecord(record);
            db.insertOrThrow(_tableName, null, values);
        }
        catch (Exception ex) {
            record.addError(new RecordError(record, null, ex));
        }
        return !record.hasErrors();
    }

    private boolean updateRecord(SQLiteDatabase db, T record) {
        try {
            ContentValues values = contentValuesForRecord(record);
            db.update(_tableName, values, "id = ?", new String[]{record.id.toString()});
        }
        catch (Exception ex) {
            record.addError(new RecordError(record, null, ex));
        }
        return !record.hasErrors();
    }

    /**
     * Saves all new and changed records to the database.
     * @return a SaveResult containing information about the operation
     */
    SaveResult save(DbContext context, SQLiteDatabase db) {
        SaveResult result = new SaveResult();

        // insert new records
        for (T record: _newRecords) {
            if (insertRecord(db, record)) {
                record._persisted = true;
                result.addInserted(record);
            }
            else {
                result.addErrored(record);
                record.logErrors();
            }
        }

        // update changed records
        for (T record: _changedRecords) {
            if (updateRecord(db, record)) {
                result.addUpdated(record);
            }
            else {
                record.logErrors();
                result.addErrored(record);
            }
        }

        // remove all successfully persisted records from the queues
        for (DbModel record: result.getInserted()) {
            if (_newRecords.contains(record))
                _newRecords.remove(record);
        }
        for (DbModel record: result.getUpdated()) {
            if (_changedRecords.contains(record))
                _changedRecords.remove(record);
        }

        return result;
    }

    //endregion


    //region Querying

    public T deserializeRow(Cursor cursor) throws IllegalAccessException, InstantiationException {
        T record = _modelClass.newInstance();
        for (int i=0; i<_columnNames.length; i++) {
            String name = _columnNames[i];
            DbColumnMap columnMap = _columnMaps.get(name);
            if (columnMap != null) {
                columnMap.deserializeColumn(cursor, record, i);
            }
        }
        record._persisted = true;
        return record;
    }

    /**
     * @return the number of rows in the table
     */
    public long count() {
        SQLiteDatabase db = _context.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + _tableName, null);
        cursor.moveToFirst();
        long count = cursor.getLong(0);
        db.close();
        return count;
    }

    /**
     * Look up a record by id.
     */
    public T find(ObjectId id) {
        SQLiteDatabase db = _context.getReadableDatabase();
        readColumnNames(db);
        T record = null;
        try {
            Cursor cursor = db.query(_tableName, _columnNames, "id = ?", new String[] {id.toString()}, null, null, null, "1");
            cursor.moveToFirst();
            record = deserializeRow(cursor);
        }
        catch (Exception ex) {
            Log.w(LogTag, "Error finding record with id " + id.toString(), ex);
        }
        finally {
            db.close();
        }
        return record;
    }

    //endregion


    //region Validation

    void addFieldValidator(Field field, Annotation annotation, Class<?> validatorType) {
        Log.d(LogTag, "adding field validator: " + annotation + " of type " + validatorType);
        try {
            FieldValidator validator = (FieldValidator) validatorType.newInstance();
            validator.setField(field);
            validator.populateFromAnnotation(annotation);
            _fieldValidators.add(validator);
        }
        catch (Exception ex) {
            Log.w(LogTag, "Error making validator for " + annotation.annotationType().getSimpleName() + " of type " + validatorType.getSimpleName());
        }
    }

    private List<FieldValidator> _fieldValidators = new ArrayList<FieldValidator>();

    /**
     * Performs all validations on the record (from annotations and the record's implementation of DbModel#onValidate)
     * @param record
     * @return true if there are no validation errors
     */
    public boolean validate(T record) {
        record.clearErrors();
        for (FieldValidator validator: _fieldValidators) {
            validator.validate(this, record);
            record.onValidate();
        }
        return !record.hasErrors();
    }

    //endregion


    //region Querying

    public Query<T> where(String property, Object value) {
        return new Query<T>(this).where(property, value);
    }

    public DbSet<T> runQuery(Query<T> query) {
        SQLiteDatabase db = _context.getReadableDatabase();
        readColumnNames(db);
        Cursor cursor = db.query(_tableName, _columnNames, query.getSelection(), query.getSelectionArgs(), null, null, query.getOrderBy());
//        db.close();
        return new DbSet<T>(this, cursor);
    }

    //endregion
}
