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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Image model.
 * @author shrids
 *
 */
public final class Image extends MetadataBase {

    public Image() {
        super();
    }

    public Image(String id) {
        super(id, EntityType.IMAGE);
    }

    /**
     * Status of the image (READ-ONLY)
     *
     */
    private Image.Status status;
    /**
     * List of strings related to the image
     *
     */
    private List<String> tags = new ArrayList<String>();
    /**
     * ID of image stored in Glance that should be used as the kernel when
     * booting an AMI-style image.
     *
     */
    private String kernelId;
    /**
     * Format of the container
     *
     */
    private Image.ContainerFormat containerFormat;
    /**
     * Amount of ram (in MB) required to boot image.
     *
     */
    private Integer minRam;
    /**
     * ID of image stored in Glance that should be used as the ramdisk when
     * booting an AMI-style image.
     *
     */
    private String ramdiskId;
    /**
     * A set of URLs to access the image file kept in external store
     *
     */
    private List<URL> locations = new ArrayList<URL>();
    /**
     * Scope of image accessibility
     *
     */
    private Image.Visibility visibility;
    /**
     * Date and time of the last image modification (READ-ONLY)
     *
     */
    private String updatedAt;
    /**
     * Owner of the image
     *
     */
    private java.lang.Object owner;
    /**
     * (READ-ONLY)
     *
     */
    private String file;
    /**
     * Amount of disk space (in GB) required to boot image.
     *
     */
    private Integer minDisk;
    /**
     * Virtual size of image in bytes (READ-ONLY)
     *
     */
    private java.lang.Object virtualSize;
    /**
     * Size of image file in bytes (READ-ONLY)
     *
     */
    private long size;
    /**
     * ID of instance used to create this image.
     *
     */
    private String instanceUuid;
    /**
     * Common name of operating system distribution as specified in
     * http://docs.openstack
     * .org/trunk/openstack-compute/admin/content/adding-images.html
     *
     */
    private String osDistro;
    /**
     * Descriptive name for the image
     *
     */
    private java.lang.Object name;
    /**
     * md5 hash of image contents. (READ-ONLY)
     *
     */
    private java.lang.Object checksum;
    /**
     * Date and time of image registration (READ-ONLY)
     *
     */
    private String createdAt;
    /**
     * Format of the disk
     *
     */
    private Image.DiskFormat diskFormat;
    /**
     * Operating system version as specified by the distributor
     *
     */
    private String osVersion;
    /**
     * If true, image will not be deletable.
     *
     */
    private Boolean _protected;
    /**
     * Operating system architecture as specified in
     * http://docs.openstack.org/trunk
     * /openstack-compute/admin/content/adding-images.html
     *
     */
    private String architecture;
    /**
     * URL to access the image file kept in external store (READ-ONLY)
     *
     */
    private String directUrl;
    /**
     * (READ-ONLY)
     *
     */
    private String self;
    /**
     * (READ-ONLY)
     *
     */
    private String schema;
    private Map<String, String> additionalProperties = new HashMap<String, String>();

    /**
     * Status of the image (READ-ONLY)
     *
     * @return The status
     */
    public String getStatus() {
        return (status==null)? null : status.toString();
    }

    /**
     * Status of the image (READ-ONLY)
     *
     * @param status
     *            The status
     */
    public void setStatus(Image.Status status) {
        this.status = status;
    }

    /**
     * Status of the image (READ-ONLY)
     *
     * @param status
     *            The status
     */
    public void setStatus(String status) {
        this.status = Image.Status.fromValue(status);
    }

    /**
     * List of strings related to the image
     *
     * @return The tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * List of strings related to the image
     *
     * @param tags
     *            The tags
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * ID of image stored in Glance that should be used as the kernel when
     * booting an AMI-style image.
     *
     * @return The kernel_id
     */
    public String getKernelId() {
        return kernelId;
    }

    /**
     * ID of image stored in Glance that should be used as the kernel when
     * booting an AMI-style image.
     *
     * @param kernel_id
     *            The kernel_id
     */
    public void setKernelId(String kernel_id) {
        this.kernelId = kernel_id;
    }

    /**
     * Format of the container
     *
     * @return The container_format
     */
    public String getContainerFormat() {
        return (containerFormat == null)? null: containerFormat.toString();
    }

    /**
     * Format of the container
     *
     * @param container_format
     *            The container_format
     */
    public void setContainerFormat(Image.ContainerFormat container_format) {
        this.containerFormat = container_format;
    }

    /**
     * Format of the container
     *
     * @param container_format
     *            The container_format
     */
    public void setContainerFormat(String container_format) {
        this.containerFormat = Image.ContainerFormat.fromValue(container_format);
    }
    /**
     * Amount of ram (in MB) required to boot image.
     *
     * @return The min_ram
     */
    public Integer getMinRam() {
        return minRam;
    }

    /**
     * Amount of ram (in MB) required to boot image.
     *
     * @param min_ram
     *            The min_ram
     */
    public void setMinRam(Integer min_ram) {
        this.minRam = min_ram;
    }

    /**
     * ID of image stored in Glance that should be used as the ramdisk when
     * booting an AMI-style image.
     *
     * @return The ramdisk_id
     */
    public String getRamdiskId() {
        return ramdiskId;
    }

    /**
     * ID of image stored in Glance that should be used as the ramdisk when
     * booting an AMI-style image.
     *
     * @param ramdisk_id
     *            The ramdisk_id
     */
    public void setRamdiskId(String ramdisk_id) {
        this.ramdiskId = ramdisk_id;
    }

    /**
     * A set of URLs to access the image file kept in external store
     *
     * @return The locations
     */
    public List<URL> getLocations() {
        return locations;
    }

    /**
     * A set of URLs to access the image file kept in external store
     *
     * @param locations
     *            The locations
     */
    public void setLocations(List<URL> locations) {
        this.locations = locations;
    }

    /**
     * Scope of image accessibility
     *
     * @return The visibility
     */
    public String getVisibility() {
        return (visibility==null)? null: visibility.toString();
    }

    /**
     * Scope of image accessibility
     *
     * @param visibility
     *            The visibility
     */
    public void setVisibility(Image.Visibility visibility) {
        this.visibility = visibility;
    }

    /**
     * Scope of image accessibility
     *
     * @param visibility
     *            The visibility
     */
    public void setVisibility(String visibility) {
        this.visibility = Image.Visibility.fromValue(visibility);
    }

    /**
     * Date and time of the last image modification (READ-ONLY)
     *
     * @return The updated_at
     */
    @Override
    public String getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Date and time of the last image modification (READ-ONLY)
     *
     * @param updated_at
     *            The updated_at
     */
    @Override
    public void setUpdatedAt(String updated_at) {
        this.updatedAt = updated_at;
    }

    /**
     * Owner of the image
     *
     * @return The owner
     */
    public java.lang.Object getOwner() {
        return owner;
    }

    /**
     * Owner of the image
     *
     * @param owner
     *            The owner
     */
    public void setOwner(java.lang.Object owner) {
        this.owner = owner;
    }

    /**
     * (READ-ONLY)
     *
     * @return The file
     */
    public String getFile() {
        return file;
    }

    /**
     * (READ-ONLY)
     *
     * @param file
     *            The file
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * Amount of disk space (in GB) required to boot image.
     *
     * @return The min_disk
     */
    public Integer getMinDisk() {
        return minDisk;
    }

    /**
     * Amount of disk space (in GB) required to boot image.
     *
     * @param min_disk
     *            The min_disk
     */
    public void setMinDisk(Integer min_disk) {
        this.minDisk = min_disk;
    }

    /**
     * Virtual size of image in bytes (READ-ONLY)
     *
     * @return The virtual_size
     */
    public java.lang.Object getVirtualSize() {
        return virtualSize;
    }

    /**
     * Virtual size of image in bytes (READ-ONLY)
     *
     * @param virtual_size
     *            The virtual_size
     */
    public void setVirtualSize(java.lang.Object virtual_size) {
        this.virtualSize = virtual_size;
    }

    /**
     * Size of image file in bytes (READ-ONLY)
     *
     * @return The size
     */
    public long getSize() {
        return size;
    }

    /**
     * Size of image file in bytes (READ-ONLY)
     *
     * @param size
     *            The size
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * ID of instance used to create this image.
     *
     * @return The instance_uuid
     */
    public String getInstanceUuid() {
        return instanceUuid;
    }

    /**
     * ID of instance used to create this image.
     *
     * @param instance_uuid
     *            The instance_uuid
     */
    public void setInstanceUuid(String instance_uuid) {
        this.instanceUuid = instance_uuid;
    }

    /**
     * Common name of operating system distribution as specified in
     * http://docs.openstack
     * .org/trunk/openstack-compute/admin/content/adding-images.html
     *
     * @return The os_distro
     */
    public String getOsDistro() {
        return osDistro;
    }

    /**
     * Common name of operating system distribution as specified in
     * http://docs.openstack
     * .org/trunk/openstack-compute/admin/content/adding-images.html
     *
     * @param os_distro
     *            The os_distro
     */
    public void setOsDistro(String os_distro) {
        this.osDistro = os_distro;
    }

    /**
     * Descriptive name for the image
     *
     * @return The name
     */
    public java.lang.Object getName() {
        return name;
    }

    /**
     * Descriptive name for the image
     *
     * @param name
     *            The name
     */
    public void setName(java.lang.Object name) {
        this.name = name;
    }

    /**
     * md5 hash of image contents. (READ-ONLY)
     *
     * @return The checksum
     */
    public java.lang.Object getChecksum() {
        return checksum;
    }

    /**
     * md5 hash of image contents. (READ-ONLY)
     *
     * @param checksum
     *            The checksum
     */
    public void setChecksum(java.lang.Object checksum) {
        this.checksum = checksum;
    }

    /**
     * Date and time of image registration (READ-ONLY)
     *
     * @return The created_at
     */
    @Override
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Date and time of image registration (READ-ONLY)
     *
     * @param created_at
     *            The created_at
     */
    @Override
    public void setCreatedAt(String created_at) {
        this.createdAt = created_at;
    }

    /**
     * Format of the disk
     *
     * @return The disk_format
     */
    public String getDiskFormat() {
        return (diskFormat == null) ? null: diskFormat.toString();
    }

    /**
     * Format of the disk
     *
     * @param disk_format
     *            The disk_format
     */
    public void setDiskFormat(Image.DiskFormat disk_format) {
        this.diskFormat = disk_format;
    }

    /**
     * Format of the disk
     *
     * @param disk_format
     *            The disk_format
     */
    public void setDiskFormat(String disk_format) {
        this.diskFormat = Image.DiskFormat.fromValue(disk_format);
    }

    /**
     * Operating system version as specified by the distributor
     *
     * @return The os_version
     */
    public String getOsVersion() {
        return osVersion;
    }

    /**
     * Operating system version as specified by the distributor
     *
     * @param os_version
     *            The os_version
     */
    public void setOsVersion(String os_version) {
        this.osVersion = os_version;
    }

    /**
     * If true, image will not be deletable.
     *
     * @return The _protected
     */
    public Boolean getProtected() {
        return _protected;
    }

    /**
     * If true, image will not be deletable.
     *
     * @param _protected
     *            The protected
     */
    public void setProtected(Boolean _protected) {
        this._protected = _protected;
    }

    /**
     * Operating system architecture as specified in
     * http://docs.openstack.org/trunk
     * /openstack-compute/admin/content/adding-images.html
     *
     * @return The architecture
     */
    public String getArchitecture() {
        return architecture;
    }

    /**
     * Operating system architecture as specified in
     * http://docs.openstack.org/trunk
     * /openstack-compute/admin/content/adding-images.html
     *
     * @param architecture
     *            The architecture
     */
    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    /**
     * URL to access the image file kept in external store (READ-ONLY)
     *
     * @return The direct_url
     */
    public String getDirectUrl() {
        return directUrl;
    }

    /**
     * URL to access the image file kept in external store (READ-ONLY)
     *
     * @param direct_url
     *            The direct_url
     */
    public void setDirectUrl(String direct_url) {
        this.directUrl = direct_url;
    }

    /**
     * (READ-ONLY)
     *
     * @return The self
     */
    public String getSelf() {
        return self;
    }

    /**
     * (READ-ONLY)
     *
     * @param self
     *            The self
     */
    public void setSelf(String self) {
        this.self = self;
    }

    /**
     * (READ-ONLY)
     *
     * @return The schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * (READ-ONLY)
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

    public Map<String, String> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, String value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(status).append(tags).append(kernelId).append(containerFormat).append(minRam)
                .append(ramdiskId).append(locations).append(visibility).append(updatedAt).append(owner).append(file)
                .append(minDisk).append(virtualSize).append(size).append(instanceUuid).append(osDistro).append(name)
                .append(checksum).append(createdAt).append(diskFormat).append(osVersion).append(_protected)
                .append(architecture).append(directUrl).append(self).append(schema).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(java.lang.Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Image) == false) {
            return false;
        }
        Image rhs = ((Image) other);
        return new EqualsBuilder().append(status, rhs.status).append(tags, rhs.tags).append(kernelId, rhs.kernelId)
                .append(containerFormat, rhs.containerFormat).append(minRam, rhs.minRam).append(ramdiskId, rhs.ramdiskId)
                .append(locations, rhs.locations).append(visibility, rhs.visibility).append(updatedAt, rhs.updatedAt)
                .append(owner, rhs.owner).append(file, rhs.file).append(minDisk, rhs.minDisk)
                .append(virtualSize, rhs.virtualSize).append(size, rhs.size).append(instanceUuid, rhs.instanceUuid)
                .append(osDistro, rhs.osDistro).append(name, rhs.name).append(checksum, rhs.checksum)
                .append(createdAt, rhs.createdAt).append(diskFormat, rhs.diskFormat).append(osVersion, rhs.osVersion)
                .append(_protected, rhs._protected).append(architecture, rhs.architecture).append(directUrl, rhs.directUrl)
                .append(self, rhs.self).append(schema, rhs.schema).append(additionalProperties, rhs.additionalProperties)
                .isEquals();
    }

    public static enum ContainerFormat {

        AMI("ami"), ARI("ari"), AKI("aki"), BARE("bare"), OVF("ovf"), OVA("ova");
        private final String value;
        private static Map<String, Image.ContainerFormat> constants = new HashMap<String, Image.ContainerFormat>();

        static {
            for (Image.ContainerFormat c : values()) {
                constants.put(c.value, c);
            }
        }

        private ContainerFormat(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public static Image.ContainerFormat fromValue(String value) {
            Image.ContainerFormat constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public static enum DiskFormat {

        AMI("ami"), ARI("ari"), AKI("aki"), VHD("vhd"), VMDK("vmdk"), RAW("raw"), QCOW_2("qcow2"), VDI("vdi"), ISO("iso");
        private final String value;
        private static Map<String, Image.DiskFormat> constants = new HashMap<String, Image.DiskFormat>();

        static {
            for (Image.DiskFormat c : values()) {
                constants.put(c.value, c);
            }
        }

        private DiskFormat(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public static Image.DiskFormat fromValue(String value) {
            Image.DiskFormat constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public static enum Status {

        QUEUED("queued"), SAVING("saving"), ACTIVE("active"), KILLED("killed"), DELETED("deleted"), PENDING_DELETE(
                "pending_delete");
        private final String value;
        private static Map<String, Image.Status> constants = new HashMap<String, Image.Status>();

        static {
            for (Image.Status c : values()) {
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

        public static Image.Status fromValue(String value) {
            Image.Status constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public static enum Visibility {

        PUBLIC("public"), PRIVATE("private");
        private final String value;
        private static Map<String, Image.Visibility> constants = new HashMap<String, Image.Visibility>();

        static {
            for (Image.Visibility c : values()) {
                constants.put(c.value, c);
            }
        }

        private Visibility(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public static Image.Visibility fromValue(String value) {
            Image.Visibility constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
