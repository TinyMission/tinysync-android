package com.tinymission.tinysync.db;

import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;

/**
 * A readonly set of DbModel objects.
 */
public class DbSet<T extends DbModel> implements Iterable<T> {

    static final String LogTag = "tinysync.db.DbSet";

    DbCollection<T> _collection;
    ArrayList<T> _records = null;
    Cursor _cursor;
    int _size = 0;
    int _computedIndex = -1;

    public DbSet(DbCollection<T> collection, Cursor cursor) {
        _collection = collection;
        _cursor = cursor;
        cursor.moveToFirst();
        _size = cursor.getCount();
        _records = new ArrayList<T>(_size);
    }

    public boolean isEmpty() {
        return _size == 0;
    }

    @Override
    public java.util.Iterator<T> iterator() {
        return new Iterator();
    }

    public int size() {
        return _size;
    }

    private T compute(int i) {
        while (_computedIndex < i) {
            try {
                _computedIndex++;
                T record = _collection.deserializeRow(_cursor);
                _records.add(_computedIndex, record);
            }
            catch (Exception ex) {
                Log.w(LogTag, "Error deserializing database row", ex);
                throw new RuntimeException("Error deserializing database row");
            }
        }
        return _records.get(i);
    }

    public T first() {
        return compute(0);
    }

    class Iterator implements java.util.Iterator<T> {

        private int _index = 0;

        @Override
        public boolean hasNext() {
            return _index > _size-1;
        }

        @Override
        public T next() {
            _index += 1;
            return compute(_index);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
