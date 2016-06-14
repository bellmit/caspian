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
package com.emc.caspian.ccs.datastore;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.fabric.util.Validate;
import com.emc.caspian.ccs.imagerepo.api.datamodel.EntityType;
import com.emc.caspian.ccs.imagerepo.api.datamodel.MetadataBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * PropertyBag for a given entity. This is used to convert from a Key value pair to a given object.
 *
 * @author shrids
 *
 */
public final class PropertyBag<T extends MetadataBase> {

    private final Map<String, String> _bag;
    private String _clazzName; // The clazzName is a property since there is no other better
                               // method to create instance of Generic Type

    /**
     * Construct a PropertyBag given the entity
     *
     * @param entity
     */
    public PropertyBag(T entity) {
        _bag = fetchPropertyBag(entity);
    }

    /**
     * Construct a PropertyBag given the propertyBag and EntityClass.
     *
     * @param entityType
     * @param properties
     */
    public PropertyBag(EntityType entityType, Map<String, String> properties) {
        _bag = properties;
        _clazzName = entityType.getClassName();
    }

    /**
     * Fetch the entityObject from PropertyBag.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public T getEntityObject() {

        BeanUtilsBean bean = new BeanUtilsBean(new ConvertUtilsBean() {
            @SuppressWarnings("rawtypes")
            @Override
            public Object convert(String value, Class clazz) {
                ObjectMapper mapper = new ObjectMapper();
                if (clazz.isEnum()) {
                    return Enum.valueOf(clazz, value);
                } else if (clazz.isPrimitive() || clazz.equals(String.class)) {
                    return super.convert(value, clazz);
                } else {
                    try {
                        return mapper.readValue(value, clazz);
                    } catch (IOException e) {
                        _log.error("Error during deserialization", e);
                    }
                }
                return "Could Not Read Value";
            }
        });

        T object = null;
        try {
            object = (T) Class.forName(_clazzName).newInstance();
            bean.populate(object, _bag); // populate the object with properties
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e) {
            _log.error("Error during creation of object from the given key values", e);
            throw new RuntimeException(e); // non recovarable exception.
        }

        return object;
    }

    /**
     * Fetch the properties in a Map
     *
     * @return
     */
    public Map<String, String> getBag() {
        return _bag;
    }

    /**
     * Fetch Entity ID
     * @return
     */
    public String getEntityID() {
        Validate.isNotNull(_bag, "PropertyBag");
        return _bag.get("id");
    }

    /**
     * Fetch EntityType
     * @return
     */
    public EntityType getEntityType() {
        Validate.isNotNull(_bag, "PropertyBag");
        return Enum.valueOf(EntityType.class, _bag.get("entityType"));

    }

    /*
     * This method is used to fetch Property of a given entity in key value pair. Non primitive
     * types and enums are serialized.
     *
     * @param entity
     *
     * @return
     */
    private Map<String, String> fetchPropertyBag(T entity) {
        Map<String, String> bagMap = new HashMap<String, String>();
        BeanMap beanMap = new BeanMap(entity);

        for (Entry<Object, Object> entry : beanMap.entrySet()) {
            if (entry.getValue() == null) // If the value is null do not populate the bag
                continue;
            Class<?> type = beanMap.getType(entry.getKey().toString());
            if (type.isEnum() || type.isPrimitive() || type.equals(String.class)) {
                bagMap.put(entry.getKey().toString(), entry.getValue().toString());
            } else {
                bagMap.put(entry.getKey().toString(), jsonSerializeObject(entry.getValue()));
            }

        }
        return bagMap;
    }

    /*
     * Method to serialize the object to Json. Note: Currently jackson interface is used.
     *
     * @param obj
     *
     * @return
     */
    private String jsonSerializeObject(Object obj) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            _log.error("Error during serialization of object", e);
            return "Error during convertion";
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(PropertyBag.class);
}
