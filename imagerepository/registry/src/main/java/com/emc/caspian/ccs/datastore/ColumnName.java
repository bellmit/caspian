package com.emc.caspian.ccs.datastore;

/**
 * The various column names used in the datastore
 * @author shrids
 *
 */
public enum ColumnName {
    ENTITY_TYPE("entityType"),
    ENTITY_ID("entityId"),
    KEYNAME("keyName"),
    KEYVALUE("keyValue");

    private String name;

    private ColumnName(String value) {
        this.name = value;
    }

    public String value() {
        return name;
    }

}
