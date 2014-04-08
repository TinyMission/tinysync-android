package com.tinymission.tinysync.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.common.base.CaseFormat;
import com.tinymission.tinysync.query.AssociationInclude;
import com.tinymission.tinysync.query.Query;
import com.tinymission.tinysync.validation.FieldValidation;
import com.tinymission.tinysync.validation.FieldValidator;
import com.tinymission.tinysync.validation.RecordError;

import org.joda.time.DateTime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

        try {
            T template = modelClass.newInstance();
            for (Field field : modelClass.getFields()) {
                parseField(template, field);
            }
        }
        catch (InstantiationException ex) {
            throw new RuntimeException("Error parsing collection " + modelClass.getSimpleName() + ": " + ex.getMessage());
        }
        catch (IllegalAccessException ex) {
            throw new RuntimeException("Error parsing collection " + modelClass.getSimpleName() + ": " + ex.getMessage());
        }
    }

    private Class<T> _modelClass;

    public Class<T> getModelClass() {
        return _modelClass;
    }

    private DbContext _context;

    public void setContext(DbContext context) {
        _context = context;
    }

    private void parseField(T template, Field field) {
        if (field.getType().isAssignableFrom(DbHasMany.class)) {
            DbHasManyMeta meta = new DbHasManyMeta(field, template);
            _hasManies.put(field.getName(), meta);
        }
        else if (field.getType().isAssignableFrom(DbBelongsTo.class)) {
            DbBelongsToMeta meta = new DbBelongsToMeta(field, template);
            _belongsTos.put(field.getName(), meta);
        }
        for (Annotation ann: field.getDeclaredAnnotations()) {
            Log.v(LogTag, "Annotation " + ann.annotationType() + " on " + _modelClass.getSimpleName() + ":" + field.getName());
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


    //region Columns

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
        for (DbBelongsToMeta meta: _belongsTos.values()) {
            defs.add(meta.getColumnDef());
        }
        return defs;
    }

    /**
     * @param fieldName the name of a model field
     * @return the name of the corresponding database column
     */
    public String fieldToColumnName(String fieldName) {
        for (DbColumnMap columnMap: _columnMaps.values()) {
            if (columnMap.getField().getName().equals(fieldName))
                return columnMap.getColumnName();
        }
        for (DbBelongsToMeta belongsTo: _belongsTos.values()) {
            if (belongsTo.getColumnName().equals(fieldName))
                return fieldName;
        }
        throw new RuntimeException("Invalid field name " + fieldName + " for model " + _modelClass.getSimpleName());
    }

    private String[] _columnNames = null;

    private void readColumnNames(SQLiteDatabase db) {
        if (_columnNames != null) return;
        _context.initialize();
        ArrayList<String> names = new ArrayList<String>();
        Cursor ti = db.rawQuery("PRAGMA table_info(" + _tableName + ")", null);
        while (ti.moveToNext()) {
            names.add(ti.getString(1));
        }
        _columnNames = names.toArray(new String[names.size()]);
    }

    //endregion


    //region Associations

    private HashMap<String, DbHasManyMeta> _hasManies = new HashMap<String,DbHasManyMeta>();

    public Map<String, DbHasManyMeta> getHasManies() {
        return _hasManies;
    }

    /**
     * Gets has-many association meta data by name.
     * @param name the name of the association field
     * @return
     */
    public DbHasManyMeta getHasMany(String name) {
        DbHasManyMeta meta = _hasManies.get(name);
        if (meta == null)
            throw new DbContext.InvalidAssociationException("has-many", name);
        return meta;
    }

    /**
     * @param modelClass the class of the foreign model
     * @return the meta data for the relationship
     */
    public DbHasManyMeta getHasManyMeta(Class<?> modelClass) {
        for (DbHasManyMeta meta: _hasManies.values()) {
            if (meta.getModelClass().equals(modelClass))
                return meta;
        }
        throw new DbContext.InvalidAssociationException("has-many", modelClass.getSimpleName());
    }

    private HashMap<String, DbBelongsToMeta> _belongsTos = new HashMap<String, DbBelongsToMeta>();

    public Map<String, DbBelongsToMeta> getBelongsTos() {
        return _belongsTos;
    }

    /**
     * Gets belongs-to association meta data by name.
     * @param name the name of the association field
     * @return
     */
    public DbBelongsToMeta getBelongsTo(String name) {
        DbBelongsToMeta meta = _belongsTos.get(name);
        if (meta == null)
            throw new DbContext.InvalidAssociationException("belongs-to", name);
        return meta;
    }

    /**
     * @param modelClass the class of the foreign model
     * @return the meta data for the relationship
     */
    public DbBelongsToMeta getBelongsToMeta(Class<?> modelClass) {
        for (DbBelongsToMeta meta: _belongsTos.values()) {
            if (meta.getModelClass().equals(modelClass))
                return meta;
        }
        throw new DbContext.InvalidAssociationException("belongs-to", modelClass.getSimpleName());
    }


    private DbBelongsToMeta getBelongsToByColumn(String columnName) {
        for (DbBelongsToMeta belongsTo: _belongsTos.values()) {
            if (belongsTo.getColumnName().equals(columnName))
                return belongsTo;
        }
        return null;
    }

    /**
     * Gets the direction of the given association, or throws an InvalidAssociationException if the association doesn't exist.
     * @param name
     * @return
     */
    public AssociationInclude.Direction getAssociationDirection(String name) {
        if (_belongsTos.containsKey(name))
            return AssociationInclude.Direction.belongsTo;
        else if (_hasManies.containsKey(name))
            return AssociationInclude.Direction.hasMany;
        throw new DbContext.InvalidAssociationException("has-many or belongs-to", name);
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
        for (DbBelongsToMeta belongsTo: _belongsTos.values()) {
            try {
                belongsTo.assignContentValue(record, values);
            }
            catch (Exception ex) {
                record.addError(new RecordError(record, belongsTo.getColumnName(), ex));
            }
        }
        return values;
    }

    private boolean insertRecord(SQLiteDatabase db, T record) {
        try {
            record.updatedAt = DateTime.now();
            ContentValues values = contentValuesForRecord(record);
            db.insertOrThrow(_tableName, null, values);
            cacheRecord(record);
        }
        catch (Exception ex) {
            record.addError(new RecordError(record, null, ex));
        }
        return !record.hasErrors();
    }

    private boolean updateRecord(SQLiteDatabase db, T record) {
        try {
            record.updatedAt = DateTime.now();
            ContentValues values = contentValuesForRecord(record);
            db.update(_tableName, values, "id = ?", new String[]{record.id.toString()});
            cacheRecord(record);
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

    /**
     * Deserializes a row from a query into a record object.
     * @param cursor the cursor at the current position to deserialize
     * @param includes a list of included associations to deserialize as well
     * @return the deserialized record
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public T deserializeRow(Cursor cursor, Set<AssociationInclude> includes) throws IllegalAccessException, InstantiationException {
        T record = _modelClass.newInstance();
        for (int i=0; i<_columnNames.length; i++) {
            String name = _columnNames[i];
            DbColumnMap columnMap = _columnMaps.get(name);
            if (columnMap != null) {
                columnMap.deserializeColumn(cursor, record, i);
            }
            DbBelongsToMeta belongsTo = getBelongsToByColumn(name);
            if (belongsTo != null) {
                belongsTo.deserializeColumn(cursor, record, i);
            }
        }
        record._persisted = true;

        // deserialize the associations
        if (includes != null) {
            for (AssociationInclude include: includes) {
                if (include.getDirection() == AssociationInclude.Direction.hasMany) {
                    DbHasManyMeta meta = getHasMany(include.getName());
                    DbHasMany hasMany = (DbHasMany)meta.getField().get(record);
                    hasMany.getValues(_context);
                }
                else if (include.getDirection() == AssociationInclude.Direction.belongsTo) {
                    DbBelongsToMeta meta = getBelongsTo(include.getName());
                    DbBelongsTo belongsTo = (DbBelongsTo) meta.getField().get(record);
                    belongsTo.getValue(_context);
                }
            }
        }

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
        return count;
    }

    /**
     * Look up a record by id.
     * This always retrieves the record from the database - it does not use the cache.
     * If you'd like to get a potentially cached value, use cachedFind() instead.
     */
    public T find(ObjectId id) {
        SQLiteDatabase db = _context.getReadableDatabase();
        readColumnNames(db);
        T record = null;
        try {
            Cursor cursor = db.query(_tableName, _columnNames, "id = ?", new String[] {id.toString()}, null, null, null, "1");
            cursor.moveToFirst();
            record = deserializeRow(cursor, null);
            cacheRecord(record);
        }
        catch (Exception ex) {
            Log.w(LogTag, "Error finding record with id " + id.toString(), ex);
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

    /**
     * Begins a query with a where statement.
     * @param property the name of the property, with an optional operator suffix
     * @param value the value to compare against
     * @return a query object
     */
    public Query<T> where(String property, Object value) {
        return new Query<T>(this).where(property, value);
    }

    /**
     * Begins a query with an orderBy statement
     * @param column the name of the column (or model field) to order by
     * @param direction the order direction (>= 0 for ascending, < 0 for descending)
     * @return a query object
     */
    public Query<T> orderBy(String column, int direction) {
        return new Query<T>(this).orderBy(column, direction);
    }

    /**
     * Executes a query on this collection.
     * @param query the query object to execute
     * @return a set of the results
     */
    public DbSet<T> runQuery(Query<T> query) {
        SQLiteDatabase db = _context.getReadableDatabase();
        readColumnNames(db);
        Cursor cursor = db.query(_tableName, _columnNames, query.getSelection(), query.getSelectionArgs(), null, null, query.getOrderBy());
        return new DbSet<T>(this, cursor, query.getIncludes());
    }

    //endregion


    //region Cache

    private HashMap<ObjectId, T> _cache = new HashMap<ObjectId, T>();

    /**
     * Clears any objects held in the cache, forcing them to be reloaded if requested again.
     */
    public void clearCache() {
        _cache.clear();
    }

    /**
     * Inserts the record into the cache so it can be used later by methods like cachedFind().
     * @param record
     */
    public void cacheRecord(T record) {
        _cache.put(record.id, record);
    }

    /**
     * Attempts to find a record in the cache, or returns a fresh record if it isn't cached.
     * @param id
     * @return
     */
    public T cachedFind(ObjectId id) {
        if (_cache.containsKey(id))
            return _cache.get(id);
        return find(id);
    }

    //endregion


}
