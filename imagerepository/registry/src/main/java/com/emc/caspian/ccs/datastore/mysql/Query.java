package com.emc.caspian.ccs.datastore.mysql;

import com.emc.caspian.ccs.datastore.FilterExpression;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import jersey.repackaged.com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by shivesh on 2/25/15.
 */
public class Query
{
    private Select select;
    private From from;
    private Where where;
    private OrderBy orderBy;
    private Limit limit;

    public Query select(boolean distinct, String... selectParams) {
        this.select = new Select(distinct, selectParams);
        return this;
    }

    public Query from(String... tableNames) {
        this.from = new From(tableNames);
        return this;
    }

    public Query where(FilterExpression expression) {
        if (expression != null)
            this.where = new Where(expression);
        return this;
    }

    public Query orderBy(String sortKey, String sortDirection) {
        if (StringUtils.isNotEmpty(sortKey)) {
            this.orderBy = new OrderBy(sortKey, sortDirection);
        }
        return this;
    }

    public Query limit(int limit, int offset) {
        if (limit > 0)
            this.limit = new Limit(limit, offset);
        return this;
    }

    @Override
    public String toString() {
        String retVal = this.select.toString() + " " + this.from.toString();

        if (this.where != null)
            retVal += " " + this.where.toString();
        if (this.orderBy != null)
            retVal += " " + this.orderBy.toString();
        if (this.limit != null)
            retVal += " " + this.limit.toString();

        return retVal;
    }

    static class Select
    {
        private List<String> selectParams;
        boolean distinct;

        public Select(String... selectParams) {
            this.distinct = false;
            this.selectParams = ImmutableList.copyOf(selectParams);
        }

        public Select(boolean distinct, String... selectParams) {
            this.distinct = distinct;
            this.selectParams = ImmutableList.copyOf(selectParams);
        }

        @Override
        public String toString() {

            final String select = this.distinct ? "SELECT DISTINCT " : "SELECT ";
            final String retVal = new StringBuilder().append(select).append(Joiner.on(",").join(selectParams)).toString();
            return retVal;
        }
    }

    static class From
    {
        private List<String> dataSource;

        public From(String... dataSource) {
            this.dataSource = ImmutableList.copyOf(dataSource);
        }

        @Override
        public String toString() {
            final String from = "FROM ";
            final String retVal = new StringBuilder().append(Joiner.on(",").join(dataSource)).toString();

            return from + retVal;
        }

    }

    static class Where
    {
        private FilterExpression filterExpression;

        public Where(FilterExpression expression) {
            this.filterExpression = expression;
        }

        @Override
        public String toString() {

            final String where = "WHERE ";
            return where + filterExpression.evaluate();
        }
    }

    static class OrderBy
    {
        private final String sortKey;
        private final String sortDirection;

        public OrderBy(String sortKey, String sortDirection) {
            this.sortKey = sortKey;
            this.sortDirection = sortDirection;
        }

        @Override
        public String toString() {
            StringBuilder retVal = new StringBuilder();
            if( !Strings.isNullOrEmpty(sortKey)) {
                retVal.append("ORDER BY ").append(sortKey).toString();
            }
            if(!Strings.isNullOrEmpty(sortDirection)){
                retVal.append(",").append(sortDirection).toString();
            }
            return retVal.toString();
        }
    }

    static class Limit
    {
        private final int limit;
        private final int offset;

        public Limit(int limit) {
            this.limit = limit;
            this.offset = 0;
        }

        public Limit(int limit, int offset) {
            this.limit = limit;
            this.offset = offset;
        }

        @Override
        public String toString() {
            String retVal = "LIMIT " + limit;
            if (offset > 0)
                retVal += " OFFSET " + offset;

            return retVal;
        }
    }
}
