package com.emc.caspian.ccs.datastore.mysql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.exceptions.TransactionFailedException;

import com.emc.caspian.ccs.datastore.DataStoreFactory;
import com.emc.caspian.ccs.datastore.DataStoreType;
import com.emc.caspian.ccs.datastore.PropertyBag;
import com.emc.caspian.ccs.datastore.expressiontree.BinaryExpression;
import com.emc.caspian.ccs.datastore.expressiontree.ConstantExpression;
import com.emc.caspian.ccs.datastore.expressiontree.Entity_A;
import com.emc.caspian.ccs.datastore.expressiontree.ParameterNameExpression;
import com.emc.caspian.ccs.imagerepo.api.datamodel.EntityType;
import com.emc.caspian.ccs.imagerepo.api.datamodel.MetadataBase;
import com.emc.caspian.fabric.config.Configuration;

public class MySqlStoreTest {

    static {
        try {
            Configuration.load("src/test/resources/registry.conf");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static MySqlStore mysqlStore = (MySqlStore) DataStoreFactory.getImageStore(DataStoreType.MYSQL);

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUp() throws Exception {

        try (Handle h = mysqlStore.getDb().getConnection().open()) {
            deleteIfExists(h);
            h.execute("insert INTO glancemeta (entityId, entityType, keyName, keyValue) VALUES (?,?,?,?)", "4444", "ENTITY_A",
                    "stringProperty", "stringA");
            h.execute("insert INTO glancemeta (entityId, entityType, keyName, keyValue) VALUES (?,?,?,?)", "4444", "ENTITY_A",
                    "intProperty", "1");
            h.execute("insert INTO glancemeta (entityId, entityType, keyName, keyValue) VALUES (?,?,?,?)", "4444", "ENTITY_A",
                    "longProperty", "123");
            h.execute("insert INTO glancemeta (entityId, entityType, keyName, keyValue) VALUES (?,?,?,?)", "4444", "ENTITY_A",
                    "stringList", "[\"s1\",\"s2\"]");
            h.execute("insert INTO glancemeta (entityId, entityType, keyName, keyValue) VALUES (?,?,?,?)", "4444", "ENTITY_A",
                    "urlList", "[\"http://123\",\"http://456\"]");

            h.execute("insert INTO glancemeta (entityId, entityType, keyName, keyValue) VALUES (?,?,?,?)", "4445", "ENTITY_A",
                    "stringProperty", "string5");
            h.execute("insert INTO glancemeta (entityId, entityType, keyName, keyValue) VALUES (?,?,?,?)", "4445", "ENTITY_A",
                    "intProperty", "5");
            h.execute("insert INTO glancemeta (entityId, entityType, keyName, keyValue) VALUES (?,?,?,?)", "4445", "ENTITY_A",
                    "longProperty", "125");
            h.execute("insert INTO glancemeta (entityId, entityType, keyName, keyValue) VALUES (?,?,?,?)", "4445", "ENTITY_A",
                    "stringList", "[\"s5\",\"s51\"]");
            h.execute("insert INTO glancemeta (entityId, entityType, keyName, keyValue) VALUES (?,?,?,?)", "4445", "ENTITY_A",
                    "urlList", "[\"http://551\",\"http://552\"]");

            h.execute("insert INTO glancemeta (entityId, entityType, keyName, keyValue) VALUES (?,?,?,?)", "4446", "ENTITY_A",
                    "stringProperty", "stringA");
            h.execute("insert INTO glancemeta (entityId, entityType, keyName, keyValue) VALUES (?,?,?,?)", "4446", "ENTITY_A",
                    "intProperty", "6");
            h.execute("insert INTO glancemeta (entityId, entityType, keyName, keyValue) VALUES (?,?,?,?)", "4446", "ENTITY_A",
                    "longProperty", "126");
            h.execute("insert INTO glancemeta (entityId, entityType, keyName, keyValue) VALUES (?,?,?,?)", "4446", "ENTITY_A",
                    "stringList", "[\"s6\",\"s51\"]");
            h.execute("insert INTO glancemeta (entityId, entityType, keyName, keyValue) VALUES (?,?,?,?)", "4446", "ENTITY_A",
                    "urlList", "[\"http://651\",\"http://552\"]");
        }

    }

    private static void deleteIfExists(final Handle h) {
        h.execute("DELETE FROM glancemeta");
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.datastore.mysql.MySqlStore#get(com.emc.caspian.ccs.imagerepo.api.datamodel.EntityType, java.lang.String)}
     * .
     */
    @Test
    public final void testGetEntity() {
        PropertyBag<? extends MetadataBase> result = mysqlStore.get(EntityType.ENTITY_A, "4444");

        Entity_A entity = (Entity_A) result.getEntityObject();
        assertEquals("stringA", entity.getStringProperty());
        assertEquals("4444", entity.getId());
        assertEquals(1, entity.getIntProperty());
        assertEquals(123, entity.getLongProperty());
        assertTrue(entity.getStringList().contains("s2"));
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.datastore.mysql.MySqlStore#getPropertyValue(com.emc.caspian.ccs.imagerepo.api.datamodel.EntityType, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public final void testGetPropertyValue() {
        String result = mysqlStore.getPropertyValue(EntityType.ENTITY_A, "4444", "stringProperty");

        assertEquals("stringA", result);
    }

    @Test
    public final void testInsertMethod() throws MalformedURLException {

        // create test entity
        Entity_A newEntity = new Entity_A("5555", EntityType.ENTITY_A, "string_update", 444, 443l, Arrays.asList(new String[] {
                "s1", "s2" }), new URL[] { new URL("http://123"), new URL("http://456") }, new Object());
        newEntity.setCreatedAt("CreationTime");
        newEntity.setUpdatedAt("UpdationTime");
        PropertyBag<Entity_A> propBag = new PropertyBag<Entity_A>(newEntity);

        // Create a propertyBag with Properties
        PropertyBag<Entity_A> propBag1 = new PropertyBag<Entity_A>(EntityType.ENTITY_A, propBag.getBag());
        try {
            mysqlStore.insert(propBag1);
        } catch (TransactionFailedException e) {
            Assert.fail();
        }

    }

    @Test
    public final void testUpdateMethodError() throws MalformedURLException {

        // create test entity
        Entity_A newEntity = new Entity_A("5557", EntityType.ENTITY_A, "string_update", 444, 443l, Arrays.asList(new String[] {
                "s1", "s2" }), new URL[] { new URL("http://123"), new URL("http://456") }, new Object());
        newEntity.setCreatedAt("CreationTime");
        newEntity.setUpdatedAt("UpdationTime");
        PropertyBag<Entity_A> propBag = new PropertyBag<Entity_A>(newEntity);

        // Create a propertyBag with Properties
        PropertyBag<Entity_A> propBag1 = new PropertyBag<Entity_A>(EntityType.ENTITY_A, propBag.getBag());
        propBag1.getBag().remove("stringProperty"); // insert without one property.
        try {
            propBag1.getBag().put("stringProperty", "newValue");
            propBag1.getBag().put("intProperty", "999");
            propBag1.getBag().put("longProperty", "999999");
            mysqlStore.update(propBag1);

        } catch (TransactionFailedException e) {
            Assert.fail();
        }
        PropertyBag<?> actualOuput = mysqlStore.get(EntityType.ENTITY_A, "5557");
        assertEquals("newValue", actualOuput.getBag().get("stringProperty"));
        assertEquals("999", actualOuput.getBag().get("intProperty"));
        assertEquals("999999", actualOuput.getBag().get("longProperty"));

    }

    @Test
    public final void testUpdateMethod() throws MalformedURLException {

        // create test entity
        Entity_A newEntity = new Entity_A("5556", EntityType.ENTITY_A, "string_update", 444, 443l, Arrays.asList(new String[] {
                "s1", "s2" }), new URL[] { new URL("http://123"), new URL("http://456") }, new Object());
        newEntity.setCreatedAt("CreationTime");
        newEntity.setUpdatedAt("UpdationTime");
        PropertyBag<Entity_A> propBag = new PropertyBag<Entity_A>(newEntity);

        // Create a propertyBag with Properties
        PropertyBag<Entity_A> propBag1 = new PropertyBag<Entity_A>(EntityType.ENTITY_A, propBag.getBag());
        propBag1.getBag().remove("stringProperty"); // insert without one property.
        try {
            mysqlStore.insert(propBag1);
            propBag1.getBag().put("stringProperty", "newValue");
            propBag1.getBag().put("intProperty", "999");
            propBag1.getBag().put("longProperty", "999999");
            mysqlStore.update(propBag1);

        } catch (TransactionFailedException e) {
            Assert.fail();
        }
        PropertyBag<?> actualOuput = mysqlStore.get(EntityType.ENTITY_A, "5556");
        assertEquals("newValue", actualOuput.getBag().get("stringProperty"));
        assertEquals("999", actualOuput.getBag().get("intProperty"));
        assertEquals("999999", actualOuput.getBag().get("longProperty"));

    }

    /**
     * Test method for {@link
     * com.emc.caspian.ccs.datastore.mysql.MySqlStore#(com.emc.caspian.ccs.imagerepo.api.datamodel.
     * EntityType, java.util.Map)} .
     *
     * @throws MalformedURLException
     */
    @SuppressWarnings("unchecked")
    @Test
    public final void testGetEntityTypeFilter() throws MalformedURLException {
        PropertyBag<?>[] result = mysqlStore.get(EntityType.ENTITY_A, BinaryExpression.and(
                BinaryExpression.equalTo(new ParameterNameExpression("keyName"), new ConstantExpression("stringProperty")),
                BinaryExpression.equalTo(new ParameterNameExpression("keyValue"), new ConstantExpression("stringA"))), 0, null,
                null, null);
        Entity_A actualEntity_1 = ((PropertyBag<Entity_A>) result[0]).getEntityObject();
        Entity_A actualEntity_2 = ((PropertyBag<Entity_A>) result[1]).getEntityObject();

        Entity_A expectedEntity_1 = new Entity_A("4444", EntityType.IMAGE, "stringA", 1, 123l, Arrays.asList(new String[] { "s1",
                "s2" }), new URL[] { new URL("http://123"), new URL("http://456") }, new Object());

        Entity_A expectedEntity_2 = new Entity_A("4446", EntityType.IMAGE, "stringA", 6, 126l, Arrays.asList(new String[] { "s6",
                "s51" }), new URL[] { new URL("http://651"), new URL("http://552") }, new Object());

        if (actualEntity_1.getId().equals("4444")) {
            assertEquals(expectedEntity_1.getId(), actualEntity_1.getId());
            assertEquals(expectedEntity_1.getStringProperty(), actualEntity_1.getStringProperty());
            assertEquals(expectedEntity_2.getId(), actualEntity_2.getId());
            assertEquals(expectedEntity_2.getStringProperty(), actualEntity_2.getStringProperty());
        } else {
            assertEquals(expectedEntity_2.getId(), actualEntity_1.getId());
            assertEquals(expectedEntity_2.getStringProperty(), actualEntity_1.getStringProperty());
            assertEquals(expectedEntity_1.getId(), actualEntity_2.getId());
            assertEquals(expectedEntity_1.getStringProperty(), actualEntity_2.getStringProperty());
        }

    }

    @AfterClass
    public static void cleanup() {
        try (Handle h = mysqlStore.getDb().getConnection().open()) {
            deleteIfExists(h);
        }
    }
}
