package com.tinymission.tinysync.query;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.common.base.Joiner;
import com.google.gson.*;
import com.tinymission.tinysync.db.DbCollection;
import com.tinymission.tinysync.db.DbColumn;
import com.tinymission.tinysync.db.DbModel;
import com.tinymission.tinysync.db.DbSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates all information about a query and provides a fluent API for creating them.
 */
public class Query<T extends DbModel> {

    static final String LogTag = "tinysync.query.Query";

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
     * @param direction the order direction (>= 0 for ascending, < 0 for descending)
     * @return this
     */
    public Query<T> orderBy(String column, int direction) {
        return addOrderBy(new OrderBy(column, direction));
    }

    //endregion


    //region Includes

    private HashSet<AssociationInclude> _includes = new HashSet<AssociationInclude>();

    /**
     * @return all association includes added to this query.
     */
    public Set<AssociationInclude> getIncludes() {
        return _includes;
    }

    /**
     * Tells the collection to include the given association in the query results.
     * @param include the association include object
     * @return this
     */
    public Query<T> addInclude(AssociationInclude include) {
        include.setDirection(_collection.getAssociationDirection(include.getName()));
        _includes.add(include);
        return this;
    }

    /**
     * Tells the collection to include the given association in the query results.
     * @param name the name of a has-many or belongs-to association
     * @return this
     */
    public Query<T> include(String name) {
        return addInclude(new AssociationInclude(name));
    }

    //endregion


    //region Query Parameters

    /**
     * @return the selection string for executing the query.
     */
    public String getSelection() {
        String[] statements = new String[_criteria.size()];
        for (int i=0; i<_criteria.size(); i++) {
            Criterion criterion = _criteria.get(i);
            String column = criterion.getColumn();
            column = _collection.fieldToColumnName(column);
            String operator = criterion.getOperator();
            statements[i] = column + " " + operator + " ?";
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
            column = _collection.fieldToColumnName(column);
            String direction = orderBy.getDirectionString();
            statements[i] = column + " " + direction;
        }
        return Joiner.on(", ").join(statements);
    }

    private Integer _limit = null;

    /**
     * @param limit the maximum number of results to return
     */
    public Query<T> limit(Integer limit) {
        _limit = limit;
        return this;
    }

    /**
     * @return the maximum number of results to return
     */
    public Integer getLimit() {
        return _limit;
    }

    public String getLimitString() {
        if (_limit == null)
            return null;
        return _limit.toString();
    }

    //endregion


    //region JSON Serialization

    /**
     * Deserializes a JSON string into a query object.
     * @param collection the collection that the query will be run against
     * @param json a string containing a query
     * @param <T> the model type
     * @return the query object
     */
    public static <T extends DbModel> Query<T> fromJson(DbCollection<T> collection, String json) {
        JsonParser parser = new JsonParser();
        JsonElement root = parser.parse(json);
        if (!root.isJsonObject())
            throw new InvalidJsonQueryException("Root element must be an object");
        JsonObject rootObject = root.getAsJsonObject();

        Query<T> query = new Query<T>(collection);

        JsonObject whereObject = rootObject.getAsJsonObject("where");
        if (whereObject != null) {
            for (Map.Entry<String, JsonElement> entry : whereObject.entrySet()) {
                if (!entry.getValue().isJsonPrimitive())
                    throw new InvalidJsonQueryException("Where clause " + entry.getKey() + " must contain a primitive");
                query.where(entry.getKey(), entry.getValue().getAsString());
            }
        }

        JsonObject orderObject = rootObject.getAsJsonObject("order");
        if (orderObject != null) {
            for (Map.Entry<String, JsonElement> entry : orderObject.entrySet()) {
                JsonElement value = entry.getValue();
                try {
                    int direction;
                    String directionString = value.getAsString();
                    if (directionString.equalsIgnoreCase("asc"))
                        direction = 1;
                    else if (directionString.equalsIgnoreCase("desc"))
                        direction = -1;
                    else
                        throw new InvalidJsonQueryException("Order clause " + entry.getKey() + " must contain either ASC or DESC");
                    query.orderBy(entry.getKey(), direction);
                }
                catch (Exception ex) {
                    Log.w(LogTag, "Error parsing orderBy clause " + entry.getKey(), ex);
                    if (!entry.getValue().isJsonPrimitive())
                        throw new InvalidJsonQueryException("Order clause " + entry.getKey() + " must contain either ASC or DESC");
                }
            }
        }

        JsonElement limitElement = rootObject.get("limit");
        if (limitElement != null) {
            try {
                query.limit(limitElement.getAsInt());
            }
            catch (Exception ex) {
                throw new InvalidJsonQueryException("Limit clause must contain and integer");
            }
        }

        return query;
    }

    //endregion


    //region Exceptions

    public static class InvalidJsonQueryException extends RuntimeException {
        public InvalidJsonQueryException(String message) {
            super("Invalid JSON query: " + message);

        }
    }

    //endregion

}
