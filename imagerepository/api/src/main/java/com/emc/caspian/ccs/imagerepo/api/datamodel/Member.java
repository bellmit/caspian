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
package com.emc.caspian.ccs.imagerepo.api.datamodel;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;

public class Member extends MetadataBase
{
    /**
     * The status of this image member
     *
     */
    private Member.Status status;
    /**
     * An identifier for the image
     *
     */
    private String imageId;
    /**
     * An identifier for the image member (tenantId)
     *
     */
    private String memberId;

    private String schema;

    public Member() {
    	super();
    }
    
    public Member(final String id) {
        super(id, EntityType.MEMBER);
    }

    /**
     * The status of this image member
     *
     * @return
     *     The status
     */
    public String getStatus() {
        return (status==null)? null : status.toString();
    }

    /**
     * The status of this image member
     *
     * @param status
     *     The status
     */
    public void setStatus(Member.Status status) {
        this.status = status;
    }

    /**
     * Status of the image member 
     *
     * @param status
     *            The status
     */
    public void setStatus(String status) {
        this.status = Member.Status.fromValue(status);
    }
    
    /**
     * An identifier for the image
     *
     * @return
     *     The image_id
     */
    public String getImageId() {
        return imageId;
    }

    /**
     * An identifier for the image
     *
     * @param image_id
     *     The image_id
     */
    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    /**
     * An identifier for the image member (tenantId)
     *
     * @return
     *     The member_id
     */
    public String getMemberId() {
        return memberId;
    }

    /**
     * An identifier for the image member (tenantId)
     *
     * @param member_id
     *     The member_id
     */
    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    /**
     *
     * @return
     *     The schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     *
     * @param schema
     *     The schema
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }



    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(status).append(imageId).append(memberId).append(schema).toHashCode();
    }

    @Override
    public boolean equals(java.lang.Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Member) == false) {
            return false;
        }
        Member rhs = ((Member) other);
        return new EqualsBuilder().append(status, rhs.status).append(imageId, rhs.imageId).append(memberId, rhs.memberId).append(schema, rhs.schema).isEquals();
    }

    public static enum Status {

        PENDING("pending"),
        ACCEPTED("accepted"),
        REJECTED("rejected");
        private final String value;
        private static Map<String, Member.Status> constants = new HashMap<String, Member.Status>();

        static {
            for (Member.Status c: values()) {
                constants.put(c.value, c);
            }
        }

        private Status(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public static Member.Status fromValue(String value) {
            Member.Status constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}