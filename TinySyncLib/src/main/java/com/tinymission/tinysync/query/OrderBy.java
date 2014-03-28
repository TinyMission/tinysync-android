package com.tinymission.tinysync.query;

/**
 * Contains a single 'order by' portion of a query.
 */
public class OrderBy {

    public enum Direction {
        asc, desc
    }

    public OrderBy(String column, Direction direction) {
        _column = column;
        _direction = direction;
    }

    private String _column;

    public String getColumn() {
        return _column;
    }

    private Direction _direction;

    public Direction getDirection() {
        return _direction;
    }

    public String getDirectionString() {
        if (_direction == Direction.asc)
            return "ASC";
        else
            return "DESC";
    }

}
