package com.tinymission.tinysync.query;


import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a single criterion on a query.
 */
public class Criterion {

    /**
     * A runtime exception that occurs when an invalid criterion is specified.
     */
    public class InvalidCriterionException extends RuntimeException {
        public InvalidCriterionException(Criterion criterion, String message) {
            super(message);
            _criterion = criterion;
        }

        private Criterion _criterion;

        public Criterion getCriterion() {
            return _criterion;
        }
    }

    public Criterion(String column, Object value) {
        _value = value;

        List<String> comps = Lists.newArrayList(Splitter.on(CharMatcher.anyOf(". ")).trimResults().omitEmptyStrings().split(column));
        _column = comps.get(0);
        if (comps.size() > 2) {
            throw new InvalidCriterionException(this, "You have too many periods or spaces in your criterion column. There should only be one at most!");
        }
        else if (comps.size() > 1) {
            String op = comps.get(1);
            if (_operatorMap.keySet().contains(op))
                _operator = _operatorMap.get(op);
            else if (_operatorMap.values().contains(op))
                _operator = op;
            else
                throw new InvalidCriterionException(this, "Invalid operator " + op);
        }
        else {
            _operator = "=";
        }
    }

    private String _column;

    /**
     * @return the SQL column name used for the criterion statement
     */
    public String getColumn() {
        return _column;
    }

    private String _operator;

    /**
     * @return the SQL operator used for the criterion statement
     */
    public String getOperator() {
        return _operator;
    }

    private Object _value;

    /**
     * @return the value to compare to
     */
    public Object getValue() {
        return _value;
    }


    //region Operators

    private static final HashMap<String,String> _operatorMap = new HashMap<String, String>();

    static {
        _operatorMap.put("eq", "=");
        _operatorMap.put("neq", "<>");
        _operatorMap.put("gt", ">");
        _operatorMap.put("gte", ">=");
        _operatorMap.put("lt", "<");
        _operatorMap.put("lte", "<=");
    }

    //endregion

}
