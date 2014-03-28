package com.tinymission.tinysync.query;


/**
 * Represents a single criterion on a query.
 */
public class Criterion {

    public Criterion(String column, Object value) {
        _column = column;
        _value = value;
    }

    private String _column;

    public String getColumn() {
        return _column;
    }

    private Object _value;

    public Object getValue() {
        return _value;
    }

}
