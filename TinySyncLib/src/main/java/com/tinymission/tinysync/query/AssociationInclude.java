package com.tinymission.tinysync.query;

/**
 * Stores the fact that a belongs-to or has-many association should be included in the query.
 */
public class AssociationInclude {

    public static enum Direction {
        hasMany, belongsTo
    }

    public AssociationInclude(String name) {
        _name = name;
    }

    private String _name;

    public String getName() {
        return _name;
    }

    private Direction _direction;

    public Direction getDirection() {
        return _direction;
    }

    public void setDirection(Direction direction) {
        _direction = direction;
    }
}
