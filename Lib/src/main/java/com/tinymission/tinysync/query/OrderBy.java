package com.tinymission.tinysync.query;

/**
 * Contains a single 'order by' portion of a query.
 */
public class OrderBy {

    public OrderBy(String column, int direction) {
        _column = column;
        _direction = direction;
    }

    /**
     * Use for ascending sort direction.
     */
    public static final int ASC = 1;

    /**
     * Use for descending sort direction.
     */
    public static final int DESC = -1;

    private String _column;

    public String getColumn() {
        return _column;
    }

    private int _direction;

    public int getDirection() {
        return _direction;
    }

    public String getDirectionString() {
        if (_direction > 0)
            return "ASC";
        else
            return "DESC";
    }

}
