package com.emc.caspian.ccs.datastore.expressiontree;

import com.emc.caspian.ccs.imagerepo.api.datamodel.EntityType;
import com.emc.caspian.ccs.imagerepo.api.datamodel.MetadataBase;
import org.apache.commons.lang.builder.EqualsBuilder;

import java.net.URL;
import java.util.List;

/**
 * This custom entity is used for testing purposes .
 *
 * @author shrids
 *
 */
public class Entity_A extends MetadataBase
{

    public Entity_A() {
        super("", EntityType.ENTITY_A);
    }

    public Entity_A(String id, EntityType entitytType, String s, int i, long l, List<String> ss, URL[] urls, Object o) {
        super(id, EntityType.ENTITY_A);
        this.id = id;
        this.entityType = entitytType;
        this.stringProperty = s;
        this.intProperty = i;
        this.longProperty = l;
        this.stringList = ss;
        this.urls = urls;
    }

    private String stringProperty;
    private int intProperty;
    private long longProperty;
    private List<String> stringList;
    private URL[] urls;

    public String getStringProperty() {
        return stringProperty;
    }

    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
    }

    public int getIntProperty() {
        return intProperty;
    }

    public void setIntProperty(int intProperty) {
        this.intProperty = intProperty;
    }

    public List<String> getStringList() {
        return stringList;
    }

    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }

    public long getLongProperty() {
        return longProperty;
    }

    public void setLongProperty(long longProperty) {
        this.longProperty = longProperty;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if ((other instanceof Entity_A) == false)
            return false;
        Entity_A rhs = (Entity_A) other;
        return new EqualsBuilder().append(this.id, rhs.id).append(this.entityType, rhs.entityType)
                .append(this.intProperty, rhs.intProperty).append(this.longProperty, rhs.longProperty)
                .append(this.stringList, rhs.stringList).append(this.stringProperty, rhs.stringProperty).isEquals();// .append(this.urlList,
                                                                                                                    // rhs.urlList)
    }

    public URL[] getUrlList() {
        return urls;
    }

    public void setUrlList(URL[] urlList) {
        this.urls = urlList;
    }
}
