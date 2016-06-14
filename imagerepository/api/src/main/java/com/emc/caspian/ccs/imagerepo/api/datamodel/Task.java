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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Task extends MetadataBase {

    /**
     * The current status of this task
     *
     */
    private Task.Status status;

    private java.lang.Object expiresAt;

    /**
     * The result of current task, JSON blob
     *
     */
    private java.lang.Object result;
    /**
     * An identifier for the owner of this task
     *
     */
    private String owner;
    /**
     * The parameters required by task, JSON blob
     *
     */
    private java.lang.Object input;
    /**
     * Human-readable informative message only included when appropriate (usually on failure)
     *
     */
    private String message;
    /**
     * The type of task represented by this content
     *
     */
    private Task.Type type;

    private String schema;

    public Task() {
        super();
        this.entityType = EntityType.TASK;
    }

    public Task(final String id) {
        super(id, EntityType.TASK);
    }

    /**
     * The current status of this task
     *
     * @return The status
     */
    public String getStatus() {
        return (status == null) ? null : status.toString();
    }

    /**
     * The current status of this task
     *
     * @param status
     *            The status
     */
    public void setStatus(Task.Status status) {
        this.status = status;
    }

    public void setStatus(String status) {
        this.status = Task.Status.fromValue(status);
    }

    /**
     * Datetime when this resource would be subject to removal
     *
     * @return The expiresAt
     */
    public java.lang.Object getExpiresAt() {
        return expiresAt;
    }

    /**
     * Datetime when this resource would be subject to removal
     *
     * @param expires_at
     *            The expiresAt
     */
    public void setExpiresAt(java.lang.Object expires_at) {
        this.expiresAt = expires_at;
    }

    /**
     * The result of current task, JSON blob
     *
     * @return The result
     */
    public java.lang.Object getResult() {
        return result;
    }

    /**
     * The result of current task, JSON blob
     *
     * @param result
     *            The result
     */
    public void setResult(java.lang.Object result) {
        this.result = result;
    }

    /**
     * An identifier for the owner of this task
     *
     * @return The owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * An identifier for the owner of this task
     *
     * @param owner
     *            The owner
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * The parameters required by task, JSON blob
     *
     * @return The input
     */
    public java.lang.Object getInput() {
        return input;
    }

    /**
     * The parameters required by task, JSON blob
     *
     * @param input
     *            The input
     */
    public void setInput(java.lang.Object input) {
        this.input = input;
    }

    /**
     * Human-readable informative message only included when appropriate (usually on failure)
     *
     * @return The message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Human-readable informative message only included when appropriate (usually on failure)
     *
     * @param message
     *            The message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * The type of task represented by this content
     *
     * @return The type
     */
    public String getType() {
        return (type == null) ? null : type.toString();
    }

    /**
     * The type of task represented by this content
     *
     * @param type
     *            The type
     */
    public void setType(Task.Type type) {
        this.type = type;
    }

    public void setType(String type) {
        this.type = Task.Type.fromValue(type);
    }

    /**
     *
     * @return The schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     *
     * @param schema
     *            The schema
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
        return new HashCodeBuilder().append(status).append(createdAt).append(updatedAt).append(expiresAt).append(result)
                .append(owner).append(input).append(message).append(type).append(id).append(schema).toHashCode();
    }

    @Override
    public boolean equals(java.lang.Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Task) == false) {
            return false;
        }
        Task rhs = ((Task) other);
        return new EqualsBuilder().append(status, rhs.status).append(createdAt, rhs.createdAt)
                .append(updatedAt, rhs.updatedAt).append(expiresAt, rhs.expiresAt).append(result, rhs.result)
                .append(owner, rhs.owner).append(input, rhs.input).append(message, rhs.message).append(type, rhs.type)
                .append(id, rhs.id).append(schema, rhs.schema).isEquals();
    }

    public static enum Status {

        PENDING("pending"), PROCESSING("processing"), SUCCESS("success"), FAILURE("failure");
        private final String value;
        private static Map<String, Task.Status> constants = new HashMap<String, Task.Status>();

        static {
            for (Task.Status c : values()) {
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

        public static Task.Status fromValue(String value) {
            Task.Status constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public static enum Type {

        IMPORT("import");
        private final String value;
        private static Map<String, Task.Type> constants = new HashMap<String, Task.Type>();

        static {
            for (Task.Type c : values()) {
                constants.put(c.value, c);
            }
        }

        private Type(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public static Task.Type fromValue(String value) {
            Task.Type constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
