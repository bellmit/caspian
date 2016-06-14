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
package com.emc.caspian.ccs.datastore.expressiontree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.emc.caspian.ccs.datastore.PropertyBag;
import com.emc.caspian.ccs.imagerepo.api.datamodel.EntityType;
import com.emc.caspian.ccs.imagerepo.api.datamodel.MetadataBase;

/**
 * Test PropertyBag class file
 * @author shrids
 *
 */
public class PropertyBagTest {

    private Entity_A entity_A;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        entity_A = new Entity_A("id", EntityType.ENTITY_A, "StringValue", 123, 123l, Arrays.asList(new String[] { "s1", "s2" }),
                new URL[] { new URL("http://123"), new URL("http://456") }, new Object());
        entity_A.setCreatedAt("CreationTime");
        entity_A.setUpdatedAt("UpdationTime");
    }

    @Test
    public final void fetchPropertyBagTest() {
        PropertyBag<Entity_A> result = new PropertyBag<Entity_A>(entity_A); // create a propertyBag from an entity

        assertEquals(entity_A.getId(), result.getBag().get("id")); // verify from bag
        assertEquals(entity_A.getEntityType().toString(), result.getBag().get("entityType"));
        assertEquals("123", result.getBag().get("intProperty"));
        assertEquals("123", result.getBag().get("longProperty"));
        assertTrue(result.getBag().get("stringList").toString().contains("s2"));
        assertTrue(result.getBag().get("urlList").toString().contains("456"));
    }

    @Test
    public final void fetchObjectTest() {

        Map<String, String> inputMap = new PropertyBag<MetadataBase>(entity_A).getBag();

        //create a propertyBag from a Map of values
        PropertyBag<Entity_A> result = new PropertyBag<Entity_A> (EntityType.ENTITY_A, inputMap);

        Entity_A resultObj = result.getEntityObject(); // read the entity
        assertTrue(entity_A.equals(resultObj)); // verify the values

    }
}
