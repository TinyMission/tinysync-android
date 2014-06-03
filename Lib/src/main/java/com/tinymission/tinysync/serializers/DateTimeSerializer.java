package com.tinymission.tinysync.serializers;

import android.content.ContentValues;
import android.database.Cursor;
import com.tinymission.tinysync.db.DbModel;
import org.joda.time.DateTime;
import java.lang.reflect.Field;

/**
 * Serializes Joda DateTime values.
 */
public class DateTimeSerializer extends DbSerializer {

    private Long valueToLong(DbModel model, Field field) throws IllegalAccessException {
        DateTime dt = (DateTime)field.get(model);
        if (dt == null)
            return null;
        return dt.getMillis();
    }

    @Override
    public void deserializeColumn(Cursor cursor, DbModel model, int columnIndex, Field field) throws IllegalAccessException {
        long longValue = cursor.getLong(columnIndex);
        if (longValue > 0)
            field.set(model, new DateTime(longValue));
        else
            field.set(model, null);
    }

    @Override
    public String serialize(DbModel model, Field field) throws IllegalAccessException {
        return valueToLong(model, field).toString();
    }

    @Override
    public void serialize(DbModel model, Field field, ContentValues values, String name) throws IllegalAccessException {
        values.put(name, valueToLong(model, field));
    }

    @Override
    public int getColumnType() {
        return Cursor.FIELD_TYPE_INTEGER;
    }
}
