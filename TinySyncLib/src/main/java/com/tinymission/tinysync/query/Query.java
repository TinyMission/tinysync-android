package com.tinymission.tinysync.query;

import android.database.sqlite.SQLiteDatabase;

import com.google.common.base.Joiner;
import com.tinymission.tinysync.db.DbCollection;
import com.tinymission.tinysync.db.DbColumn;
import com.tinymission.tinysync.db.DbModel;
import com.tinymission.tinysync.db.DbSet;

import java.util.ArrayList;

/**
 * Encapsulates all information about a query and provides a fluent API for creating them.
 */
public class Query<T extends DbModel> {

    public Query(DbCollection<T> collection) {
        _collection = collection;
    }

    DbCollection<T> _collection;

    /**
     * Executes the query against the collection.
     * @return a list of the query results.
     */
    public DbSet<T> run() {
        return _collection.runQuery(this);
    }

    //region Criteria

    private ArrayList<Criterion> _criteria = new ArrayList<Criterion>();

    /**
     * Add an explicit criterion to this query.
     * You probably don't need to call this directly, and can use where() instead.
     * @return this
     */
    public Query<T> addCriterion(Criterion criterion) {
        _criteria.add(criterion);
        return this;
    }

    /**
     * Adds a Criterion to the query, filtering by the given column value.
     * @param column the name of the column (or model field) to filter by
     * @param value the value of the column to include in the filter
     * @return this
     */
    public Query<T> where(String column, Object value) {
        return addCriterion(new Criterion(column, value));
    }

    //endregion


    //region Order

    private ArrayList<OrderBy> _orderBys = new ArrayList<OrderBy>();

    /**
     * Add an explicit OrderBy to this query.
     * You probably don't need to call this directly, and can use orderBy() instead.
     * @return this
     */
    public Query<T> addOrderBy(OrderBy orderBy) {
        _orderBys.add(orderBy);
        return this;
    }

    /**
     * Adds an OrderBy to the query, ordering the result by the given column and direction.
     * @param column the name of the column (or model field) to order by
     * @param direction the direction of the ordering
     * @return this
     */
    public Query<T> orderBy(String column, OrderBy.Direction direction) {
        return addOrderBy(new OrderBy(column, direction));
    }

    //endregion


    //region Query Parameters

    /**
     * @return the selection string for executing the query.
     */
    public String getSelection() {
        String[] statements = new String[_criteria.size()];
        for (int i=0; i<_criteria.size(); i++) {
            String column = _criteria.get(i).getColumn();
            statements[i] = column + " = ?";
        }
        return Joiner.on(" AND ").join(statements);
    }

    /**
     * @return an array of arguments for the selection statement.
     */
    public String[] getSelectionArgs() {
        String[] args = new String[_criteria.size()];
        for (int i=0; i<_criteria.size(); i++) {
            args[i] =  _criteria.get(i).getValue().toString();
        }
        return args;
    }

    public String getOrderBy() {
        String[] statements = new String[_orderBys.size()];
        for (int i=0; i<_orderBys.size(); i++) {
            OrderBy orderBy = _orderBys.get(i);
            String column = orderBy.getColumn();
            String direction = orderBy.getDirectionString();
            statements[i] = column + " " + direction;
        }
        return Joiner.on(", ").join(statements);
    }

    //endregion

}
