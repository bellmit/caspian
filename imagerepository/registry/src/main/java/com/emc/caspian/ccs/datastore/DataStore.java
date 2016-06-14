package com.emc.caspian.ccs.datastore;

import java.util.List;

import com.emc.caspian.ccs.datastore.expressiontree.Expression;
import com.emc.caspian.ccs.imagerepo.api.datamodel.EntityType;
import com.emc.caspian.ccs.imagerepo.api.datamodel.MetadataBase;

/**
 * Interface to the DataStore.
 * Created by shivesh on 2/10/15.
 */
public interface DataStore {


    /**
     * Fetch a PropertyBag for a given entityType and entityID.
     * @param entityType
     * @param entityId
     * @return
     */
    public PropertyBag<? extends MetadataBase> get(EntityType entityType, String entityId);

    /**
     * Fetch a list of PropertyBags given the propertyValues.
     * @param entityType
     * @param filter
     * @param limit
     * @param marker
     * @param sortKey
     * @param sortOrder
     * @return
     */
    public PropertyBag<? extends MetadataBase>[] get(EntityType entityType,
                                Expression filter,
                                int limit,
                                String marker,
                                String sortKey,
                                String sortOrder);

    /**
     * Fetch a Property Value given the entityType and EntityID.
     * @param entityType
     * @param entityId
     * @param propertyName
     * @return
     */
    public String getPropertyValue(EntityType entityType, String entityId, String propertyName);

    /**
     * Modify entries for a given entityID and entityType (PropertyBag)
     * @param entityProperties
     */
    public void update(PropertyBag<?> entityProperties);

    /**
     * Modify array of entries for a given entityID and entityType
     * @param entityProperties
     */
    public void update(List<PropertyBag<?>> entityProperties);

    /**
     * Add entries for a given entityID and entityType
     * @param entityProperties
     */
    public void insert(PropertyBag<?> entityProperties);

    /**
     * Add array of entries for a given entityID and entityType
     * @param entityProperties
     */
    public void insert(List<PropertyBag<?>> entityProperties);

    /**
     * Delete all entries for a given entityType and entityID.
     * @param entityType
     * @param entityId
     */
    public void delete(EntityType entityType, String entityId);


}
