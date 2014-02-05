package com.tinymission.tinysync.serializers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.tinymission.tinysync.db.DbModel;
import com.tinymission.tinysync.db.ObjectId;

import org.joda.time.DateTime;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Base class for serializers that transform information to and from model objects.
 */
public abstract class DbSerializer {

    private static final String LogTag = "tinysync.serializers.DbSerializer";

    /**
     * This exception will get thrown if you attempt to get a serializer for an unknown type.
     */
    public static class InvalidTypeException extends RuntimeException {
        public InvalidTypeException(Class<?> type) {
            super("Can't find serializer for type " + type.getName());
        }
    }

    /**
     * Deserialize a column value from a cursor into a model object.
     * @param cursor the database cursor
     * @param model the model object
     * @param columnIndex the index of the column to deserialize, the caller is responsible for
     *                    ensuring that the correct index is passed
     * @param field the meta data for the model field
     */
    public abstract void deserializeColumn(Cursor cursor, DbModel model, int columnIndex, Field field)
            throws IllegalAccessException;

    /**
     * Serialize a column value from a model object into a string.
     * @param model the model object
     * @param field the meta data for the model field
     * @return a string representing the value
     */
    public abstract String serialize(DbModel model, Field field)
            throws IllegalAccessException;

    /**
     * @return the column type this serializer maps to.
     * Should be one of Cursor.FIELD_TYPE_* values.
     */
    public abstract int getColumnType();

    public static String columnTypeName(int columnType) {
        switch (columnType) {
            case Cursor.FIELD_TYPE_INTEGER:
                return "INTEGER";
            case Cursor.FIELD_TYPE_FLOAT:
                return "FLOAT";
            case Cursor.FIELD_TYPE_BLOB:
                return "BLOB";
            default:
                return "TEXT";
        }
    }


    private static HashMap<String,DbSerializer> _instances = new HashMap<String, DbSerializer>();

    /**
     * Factory method that return a concrete serializer instance for the given type.
     * @param type the type that needs to be serialized
     * @return the serializer that can be used for the given type
     */
    public static DbSerializer factory(Class<?> type) {
        String name = type.getName();
        if (_instances.containsKey(name))
            return _instances.get(name);

        DbSerializer serializer = null;
        if (type == String.class)
            serializer = new StringSerializer();
        else if (type == int.class || type == Integer.class)
            serializer = new IntSerializer();
        else if (type == ObjectId.class)
            serializer = new ObjectIdSerializer();
        else if (type == DateTime.class)
            serializer = new DateTimeSerializer();
        else if (type.isEnum())
            serializer = new EnumSerializer((Class<Enum>)type);

        if (serializer == null)
            throw new InvalidTypeException(type);

        Log.v(LogTag, "Creating serializer " + serializer.getClass().toString() + " for " + name);
        _instances.put(name, serializer);
        return serializer;
    }

}
