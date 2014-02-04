package com.tinymission.tinysync.serializers;

import android.database.Cursor;
import com.tinymission.tinysync.db.DbModel;
import org.joda.time.DateTime;
import java.lang.reflect.Field;

/**
 * Serializes Joda DateTime values.
 */
public class DateTimeSerializer extends DbSerializer {
    @Override
    public void deserializeColumn(Cursor cursor, DbModel model, int columnIndex, Field field) throws IllegalAccessException {
        String stringValue = cursor.getString(columnIndex);
        field.set(model, DateTime.parse(stringValue));
    }

    @Override
    public String serialize(DbModel model, Field field) throws IllegalAccessException {
        return field.get(model).toString();
    }
}
