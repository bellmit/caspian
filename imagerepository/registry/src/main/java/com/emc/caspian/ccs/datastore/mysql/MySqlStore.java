/**
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */
package com.emc.caspian.ccs.datastore.mysql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.PreparedBatch;
import org.skife.jdbi.v2.TransactionCallback;
import org.skife.jdbi.v2.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.datastore.ColumnName;
import com.emc.caspian.ccs.datastore.DataStore;
import com.emc.caspian.ccs.datastore.PropertyBag;
import com.emc.caspian.ccs.datastore.expressiontree.BinaryExpression;
import com.emc.caspian.ccs.datastore.expressiontree.Expression;
import com.emc.caspian.ccs.datastore.expressiontree.ParameterNameExpression;
import com.emc.caspian.ccs.imagerepo.api.datamodel.EntityType;
import com.emc.caspian.ccs.imagerepo.api.datamodel.MetadataBase;
import com.emc.caspian.ccs.imagerepo.api.exceptionhandling.ExceptionToStatus;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

/**
 * This is a mysql based implementation of DataStore
 *
 * @author shrids
 */
public class MySqlStore implements DataStore {

    public static enum Glancemeta {
        KeyName("keyName"), KeyValue("keyValue"), EntityId("entityId"), EntityType("entityType");

        public String getColumnName() {
            return columnName;
        }

        private final String columnName;

        Glancemeta(final String name) {
            this.columnName = name;
        }

        public static String getTableName() {
            return "glancemeta";
        }
    }

    ;

    private static MySqlStore mMysqlStore;

    private static final ExceptionToStatus exceptionMapper = new MysqlExceptionMapper();

    private static final Logger _log = LoggerFactory.getLogger(MySqlStore.class);

    /*
     * (non-Javadoc)
     *
     * @see com.emc.caspian.ccs.datastore.DataStore#get(com.emc.caspian.ccs.imagerepo.api.datamodel.
     * EntityType, java.lang.String)
     */
    @Override
    public PropertyBag<? extends MetadataBase> get(EntityType entityType, String entityId) {
        Map<String, String> bag = null;
        try (Handle handle = db.getConnection().open()) {

            List<Map<String, Object>> result = handle.select(
                    "select keyName, keyValue from glancemeta where entityId = :id AND entityType = :type", entityId,
                    entityType.toString());

            bag = extractProperties(entityType, entityId, result); // read the properties
        }
        return new PropertyBag<>(entityType, bag);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.emc.caspian.ccs.datastore.DataStore#get(com.emc.caspian.ccs.imagerepo.api.datamodel.
     * EntityType, com.emc.caspian.ccs.datastore.expressiontree.Expression, int, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public PropertyBag<? extends MetadataBase>[] get(EntityType entityType, Expression filterValues, int limit, String marker,
            String sortKey, String sortDirection) {

        PropertyBag<?>[] propertyBags = null;
        try (Handle handle = db.getConnection().open()) {
            final String glancemetaTable = Glancemeta.getTableName();

            final MySqlFilterExpression expression = filterValues == null ? null : new MySqlFilterExpression(filterValues);
            Query query = new Query().select(true, Glancemeta.EntityId.getColumnName()).from(glancemetaTable).where(expression)
                    .limit(limit, 0);
            final String innerQuery = query.toString();

            final String innertableAlias = "innertable";
            final String innerTable = "(" + innerQuery + ") " + innertableAlias;

            final BinaryExpression equals = BinaryExpression.equalTo(new ParameterNameExpression(innertableAlias + "."
                    + Glancemeta.EntityId.getColumnName()), new ParameterNameExpression(glancemetaTable + "."
                    + Glancemeta.EntityId.getColumnName()));
            Query uberQuery = new Query()
                    .select(false, glancemetaTable + "." + Glancemeta.KeyName.getColumnName(),
                            glancemetaTable + "." + Glancemeta.KeyValue.getColumnName(),
                            glancemetaTable + "." + Glancemeta.EntityId.getColumnName()).from(glancemetaTable, innerTable)
                    .where(new MySqlFilterExpression(equals)).orderBy(sortKey, sortDirection);

            final String outerQuery = uberQuery.toString();

            List<Map<String, Object>> result = handle.select(outerQuery);

            propertyBags = extractPropertyBags(entityType, result);
        } catch (Exception e) {
            throw e;
        }

        return propertyBags;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.emc.caspian.ccs.datastore.DataStore#getPropertyValue(com.emc.caspian.ccs.imagerepo.api
     * .datamodel.EntityType, java.lang.String, java.lang.String)
     */
    @Override
    public String getPropertyValue(EntityType entityType, String entityId, String propertyName) {
        String value = null;
        try (Handle handle = db.getConnection().open()) {
            List<Map<String, Object>> result = handle
                    .select("select keyName, keyValue from glancemeta where entityId = :id AND entityType = :type AND keyName = :propName",
                            entityId, entityType.toString(), propertyName);
            if (result.isEmpty()) {
                _log.info("No results obtained for EntityType: {}, entityId: {}, propertyName: {} ",
                        new String[] { entityType.toString(), entityId, propertyName });
            } else {
                value = result.get(0).get("keyValue").toString();
            }
        }
        return value;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.emc.caspian.shared.datastore.DataStore#insert(com.emc.caspian.shared.datastore.PropertyBag
     * )
     */
    @Override
    public void insert(final PropertyBag<?> entityProperties) {
        try (Handle handle = db.getConnection().open()) {

            Integer statementCount = handle.inTransaction(new TransactionCallback<Integer>() {
                @Override
                public Integer inTransaction(Handle handle, TransactionStatus status) {
                    return insertRow(handle, entityProperties);
                }
            });

            _log.info("The number of rows inserted for entityID: {} entityType: {} is : {}",
                    new Object[] { entityProperties.getEntityID(), entityProperties.getEntityType(), statementCount });
        }// TransactionFailedException is thrown incase of an error.
    }

    private int insertRow(Handle handle, PropertyBag<?> entityProperties) {
        String entityId = entityProperties.getEntityID();
        String entityType = entityProperties.getEntityType().toString();

        PreparedBatch batchInsert = handle
                .prepareBatch("insert into glancemeta (entityId, entityType, keyName, keyValue) values (?, ?, ?, ?)");

        // add all the properties into a batch
        for (Entry<String, String> entry : entityProperties.getBag().entrySet()) {
            batchInsert.add().bind(0, entityId).bind(1, entityType).bind(2, entry.getKey()).bind(3, entry.getValue());
        }
        int[] insertResults = batchInsert.execute();
        return insertResults.length;
    }

    @Override
    public void insert(final List<PropertyBag<?>> entityPropertiesList) {
        try (Handle handle = db.getConnection().open()) {

            Integer statementCount = handle.inTransaction(new TransactionCallback<Integer>() {
                @Override
                public Integer inTransaction(Handle handle, TransactionStatus status) {
                    int numberOfRowsInserted = 0;
                    for (PropertyBag<?> entityProperties : entityPropertiesList) {
                        numberOfRowsInserted += insertRow(handle, entityProperties);
                        _log.info("The number of rows inserted for entityID: {} entityType: {} is : {}",
                                new Object[] { entityProperties.getEntityID(), entityProperties.getEntityType(), numberOfRowsInserted });
                    }
                    return numberOfRowsInserted;
                }
            });

        }// TransactionFailedException is thrown incase of an error.
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.emc.caspian.ccs.datastore.DataStore#update(com.emc.caspian.ccs.datastore.PropertyBag)
     */
    @Override
    public void update(final PropertyBag<?> entityProperties) {
        // TODO: handle concurrent updates to the same entry.
        // Perform a read to fetch the current properties
        final Map<String, String> currentMap = get(entityProperties.getEntityType(), entityProperties.getEntityID()).getBag();

        final Map<String, String> updatedMap = entityProperties.getBag();

        try (Handle handle = db.getConnection().open()) {
            // create a transaction
            Integer statementCount = handle.inTransaction(new TransactionCallback<Integer>() {
                @Override
                public Integer inTransaction(Handle handle, TransactionStatus status) {

                    String entityId = entityProperties.getEntityID();
                    String entityType = entityProperties.getEntityType().toString();
                    MapDifference<String, String> difference = Maps.difference(currentMap, updatedMap); // compute
                                                                                                        // difference

                    // This is used to lock rows before the update operation.
                    handle.select(
                            "SELECT keyName, keyValue FROM glancemeta WHERE entityId = :id AND entityType = :type FOR UPDATE",
                            entityId, entityType);

                    // Batch Insert
                    PreparedBatch batchInsert = handle
                            .prepareBatch("insert into glancemeta (entityId, entityType, keyName, keyValue) values (?, ?, ?, ?)");
                    for (Entry<String, String> entry : difference.entriesOnlyOnRight().entrySet()) {
                        batchInsert.add().bind(0, entityId).bind(1, entityType).bind(2, entry.getKey()).bind(3, entry.getValue());
                    }
                    int[] insertResults = batchInsert.execute();

                    // Batch Update
                    PreparedBatch batchUpdate = handle
                            .prepareBatch("update glancemeta set keyValue = ? where (entityId= ?  AND entityType= ? AND keyName= ?)");
                    for (String entry : difference.entriesDiffering().keySet()) {
                        batchUpdate.add().bind(0, updatedMap.get(entry)).bind(1, entityId).bind(2, entityType).bind(3, entry);
                    }
                    int[] updateResults = batchUpdate.execute();

                    return insertResults.length + updateResults.length; // return number of
                                                                        // statements executed.
                }
            });
            _log.info("The number of rows changed for entityID: {} entityType: {} is : {}",
                    new Object[] { entityProperties.getEntityID(), entityProperties.getEntityType(), statementCount });
        }// TransactionFailedException is thrown incase of an error.
    }

    public void update(List<PropertyBag<?>> entityProperties){
        //TODO
    }
    /*
     * (non-Javadoc)
     *
     * @see
     * com.emc.caspian.ccs.datastore.DataStore#delete(com.emc.caspian.ccs.imagerepo.api.datamodel
     * .EntityType, java.lang.String)
     */
    @Override
    public void delete(EntityType entityType, String entityId) {
        try (Handle handle = db.getConnection().open()) {
            handle.execute("DELETE FROM glancemeta WHERE entityId = :id AND entityType = :type", entityId, entityType.toString());
        }
    }

    public final MySqlConnector getDb() {
        return db;
    }

    private PropertyBag<? extends MetadataBase>[] extractPropertyBags(EntityType entityType, List<Map<String, Object>> result) {
        Map<String, Map<String, String>> bags = new HashMap<String, Map<String, String>>();

        for (Map<String, Object> entry : result) {// parse the result and fetch properties for a
            // given entityID

            String entityId = entry.get("entityId").toString();
            if (!bags.containsKey(entityId))
                bags.put(entityId, new HashMap<String, String>());
            bags.get(entityId).put(entry.get(ColumnName.KEYNAME.value()).toString(),
                    entry.get(ColumnName.KEYVALUE.value()).toString());

        }

        List<PropertyBag<? extends MetadataBase>> bagList = new ArrayList<>();
        for (Entry<String, Map<String, String>> entry : bags.entrySet()) {
            entry.getValue().put("id", entry.getKey());
            entry.getValue().put("entityType", entityType.toString());
            bagList.add(new PropertyBag<>(entityType, entry.getValue()));
        }
        return bagList.toArray(new PropertyBag<?>[0]);
    }

    private final Map<String, String> extractProperties(EntityType entityType, String entityId, List<Map<String, Object>> result) {
        Map<String, String> bag = new HashMap<String, String>();

        if (result.isEmpty())
            return bag;

        bag.put("id", entityId);
        bag.put("entityType", entityType.toString());
        for (Map<String, Object> entry : result) {
            bag.put(entry.get(ColumnName.KEYNAME.value()).toString(), entry.get(ColumnName.KEYVALUE.value()).toString());
        }
        return bag;
    }

    private final MySqlConnector db = new MySqlConnector();

    private MySqlStore() {
    }

    public static synchronized DataStore getMysqlStoreSingleton() {
        if (mMysqlStore == null) {
            mMysqlStore = new MySqlStore();
        }
        return mMysqlStore;
    }

    public static ExceptionToStatus getExceptionMapper() {
        return exceptionMapper;
    }

}
