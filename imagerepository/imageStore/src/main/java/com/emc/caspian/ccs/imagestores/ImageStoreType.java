package com.emc.caspian.ccs.imagestores;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shivesh on 2/11/15.
 */
public enum ImageStoreType {
    FileSystem("file"), ObjectService("ecs"), Swift("swift"), Http("http");

    private String scheme; //represent scheme in URI e.g: http:// swift:// ecs://
    private static Map<String, ImageStoreType> constants = new HashMap<String, ImageStoreType>();

    static {
        for (ImageStoreType c : values()) {
            constants.put(c.scheme, c);
        }
    }

    private ImageStoreType(String scheme) {
        this.scheme = scheme;
    }

    public String getScheme() {
        return this.scheme;
    }

    public static ImageStoreType fromString(String scheme) {

        ImageStoreType constant = constants.get(scheme);
        if (constant == null) {
            throw new IllegalArgumentException(scheme);
        } else {
            return constant;
        }
    }
}
