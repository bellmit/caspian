/**
 *  Copyright (c) 2014 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */
package com.emc.caspian.ccs.imagerepo.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.imagerepo.api.datamodel.DockerImage;
import com.emc.caspian.ccs.imagerepo.api.datamodel.EntityType;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Image;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Member;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Task;
import com.emc.caspian.ccs.imagerepo.model.DockerImageLocation;
import com.emc.caspian.ccs.imagerepo.model.Images;
import com.emc.caspian.ccs.imagerepo.model.Location;

/**
 * Provides translation between internal model and glance model
 *
 * @author shrids
 *
 */
public final class ModelHelper {
    private static final Logger _log = LoggerFactory.getLogger(ModelHelper.class);

    public static final com.emc.caspian.ccs.imagerepo.model.Image encode(Image image) {
        com.emc.caspian.ccs.imagerepo.model.Image glanceV2Image = new com.emc.caspian.ccs.imagerepo.model.Image();
        glanceV2Image.setArchitecture(image.getArchitecture());
        glanceV2Image.setChecksum(image.getChecksum());
        if (image.getContainerFormat() != null) {
            glanceV2Image.setContainer_format(com.emc.caspian.ccs.imagerepo.model.Image.Container_format.fromValue(image
                    .getContainerFormat().toString()));
        }
        glanceV2Image.setCreated_at(image.getCreatedAt());
        glanceV2Image.setDirect_url(image.getDirectUrl());
        if (image.getDiskFormat() != null) {
            glanceV2Image.setDisk_format(com.emc.caspian.ccs.imagerepo.model.Image.Disk_format.fromValue(image.getDiskFormat()
                    .toString()));
        }
        glanceV2Image.setFile(image.getFile());
        glanceV2Image.setId(image.getId());
        glanceV2Image.setInstance_uuid(image.getInstanceUuid());
        glanceV2Image.setKernel_id(image.getKernelId());
        glanceV2Image.setLocations(encodeLocation(image.getLocations()));
        glanceV2Image.setMin_disk(image.getMinDisk());
        glanceV2Image.setMin_ram(image.getMinDisk());
        glanceV2Image.setName(image.getName());
        glanceV2Image.setOs_distro(image.getOsDistro());
        glanceV2Image.setOs_version(image.getOsVersion());
        glanceV2Image.setOwner(image.getOwner());
        glanceV2Image.setProtected(image.getProtected());
        glanceV2Image.setRamdisk_id(image.getRamdiskId());
        glanceV2Image.setSchema(image.getSchema());
        // glanceV2Image.setSelf(self); // to be updated in the reset resources
        glanceV2Image.setSize(image.getSize());
        if (image.getStatus() != null) {
            glanceV2Image.setStatus(com.emc.caspian.ccs.imagerepo.model.Image.Status.fromValue(image.getStatus().toString()));
        }
        glanceV2Image.setTags(image.getTags());
        glanceV2Image.setUpdated_at(image.getUpdatedAt());
        glanceV2Image.setVirtual_size(image.getVirtualSize());
        if (image.getVisibility() != null) {
            glanceV2Image.setVisibility(com.emc.caspian.ccs.imagerepo.model.Image.Visibility.fromValue(image.getVisibility()
                    .toString()));
        }
        return glanceV2Image;
    }

    public static final Image decode(com.emc.caspian.ccs.imagerepo.model.Image glanceV2Image) {
        Image image = new Image();
        image.setEntityType(EntityType.IMAGE);
        image.setId(glanceV2Image.getId());
        image.setArchitecture(glanceV2Image.getArchitecture());
        image.setChecksum(glanceV2Image.getChecksum());
        if (glanceV2Image.getContainer_format() != null) {
            image.setContainerFormat(glanceV2Image.getContainer_format().toString());
        }
        image.setCreatedAt(glanceV2Image.getCreated_at());
        image.setUpdatedAt(glanceV2Image.getUpdated_at());
        image.setDirectUrl(glanceV2Image.getDirect_url());
        if (glanceV2Image.getDisk_format() != null) {
            image.setDiskFormat(glanceV2Image.getDisk_format().toString());
        }
        image.setFile(glanceV2Image.getFile());
        image.setInstanceUuid(glanceV2Image.getInstance_uuid());
        image.setKernelId(glanceV2Image.getKernel_id());
        image.setLocations(decodeLocation(glanceV2Image.getLocations()));
        image.setMinDisk(glanceV2Image.getMin_disk());
        image.setMinRam(glanceV2Image.getMin_ram());
        image.setName(glanceV2Image.getName());
        image.setOsDistro(glanceV2Image.getOs_distro());
        image.setOwner(glanceV2Image.getOwner());
        image.setProtected(glanceV2Image.getProtected());
        image.setRamdiskId(glanceV2Image.getRamdisk_id());
        image.setSchema(glanceV2Image.getSchema());
        if (glanceV2Image.getSize() != null) {
            image.setSize(Long.parseLong(glanceV2Image.getSize().toString()));
        }
        if (glanceV2Image.getStatus() != null) {
            image.setStatus(glanceV2Image.getStatus().toString());
        }
        image.setTags(glanceV2Image.getTags());
        image.setVirtualSize(glanceV2Image.getVirtual_size());
        if (glanceV2Image.getVisibility() != null) {
            image.setVisibility(glanceV2Image.getVisibility().toString());
        }
        return image;
    }

    public static final com.emc.caspian.ccs.imagerepo.model.Images encode(List<Image> images) {
        List<com.emc.caspian.ccs.imagerepo.model.Image> glanceV2ImageList = new ArrayList<com.emc.caspian.ccs.imagerepo.model.Image>();
        for (Image image : images) {
            glanceV2ImageList.add(encode(image));
        }
        com.emc.caspian.ccs.imagerepo.model.Images glanceV2Images = new Images();
        glanceV2Images.setImages(glanceV2ImageList);
        return glanceV2Images;
    }

    public static final List<Image> decode(com.emc.caspian.ccs.imagerepo.model.Images glanceV2Images) {
        List<Image> images = new ArrayList<Image>();
        for (com.emc.caspian.ccs.imagerepo.model.Image image : glanceV2Images.getImages()) {
            images.add(decode(image));
        }
        return images;
    }

    private static final List<com.emc.caspian.ccs.imagerepo.model.Location> encodeLocation(List<URL> locations) {
        List<com.emc.caspian.ccs.imagerepo.model.Location> glanceV2Location = new ArrayList<com.emc.caspian.ccs.imagerepo.model.Location>(
                0);
        for (URL location : locations) {
            Location loc = new Location();
            loc.setUrl(location.toString());
            glanceV2Location.add(loc);
        }
        return glanceV2Location;

    }

    private static final List<URL> decodeLocation(List<com.emc.caspian.ccs.imagerepo.model.Location> glanceV2Location) {
        List<URL> locations = new ArrayList<URL>(0);
        for (com.emc.caspian.ccs.imagerepo.model.Location location : glanceV2Location) {
            try {
                locations.add(new URL(location.getUrl()));
            } catch (MalformedURLException exp) {
                _log.error("Error during converting location from glanceV2 model", exp);
            }
        }
        return locations;

    }

    public static final com.emc.caspian.ccs.imagerepo.model.DockerImage encode(DockerImage image) {
        com.emc.caspian.ccs.imagerepo.model.DockerImage dockerImage = new com.emc.caspian.ccs.imagerepo.model.DockerImage();
        dockerImage.setId(image.getId());
        dockerImage.setDockerImageLocation(encodeDockerImageLocation(image.getLocation()));
        dockerImage.setSize(image.getVirtualSize());
        return dockerImage;
    }

    //TODO reuse Image.Location json attribute inplace of DockerImageLocation
    private static final com.emc.caspian.ccs.imagerepo.model.DockerImageLocation encodeDockerImageLocation(URL location) {
        com.emc.caspian.ccs.imagerepo.model.DockerImageLocation dockerLayerLocation = new com.emc.caspian.ccs.imagerepo.model.DockerImageLocation();
        DockerImageLocation loc = new DockerImageLocation();
        loc.setUrl(location.toString());
        dockerLayerLocation = loc;
        return dockerLayerLocation;

    }

    public static final Task decode(com.emc.caspian.ccs.imagerepo.model.Task task) {
        Task taskData = new Task();
        taskData.setEntityType(EntityType.TASK);
        taskData.setId(task.getId());
        taskData.setCreatedAt(task.getCreated_at());
        taskData.setExpiresAt(task.getExpires_at());
        taskData.setInput(task.getInput());
        taskData.setMessage(task.getMessage());
        taskData.setOwner(task.getOwner());
        taskData.setResult(task.getResult());
        taskData.setSchema(task.getSchema());
        if (task.getStatus() != null) {
            taskData.setStatus(task.getStatus().toString());
        }
        if (task.getType() != null) {
            taskData.setType(task.getType().toString());
        }
        taskData.setUpdatedAt(task.getUpdated_at());

        return taskData;
    }

    public static final com.emc.caspian.ccs.imagerepo.model.Task encode(Task task) {

        com.emc.caspian.ccs.imagerepo.model.Task taskData = new com.emc.caspian.ccs.imagerepo.model.Task();
        taskData.setId(task.getId());
        taskData.setCreated_at(task.getCreatedAt());
        taskData.setExpires_at(task.getExpiresAt());
        taskData.setInput(task.getInput());
        taskData.setMessage(task.getMessage());
        taskData.setOwner(task.getOwner());
        taskData.setResult(task.getResult());
        taskData.setSchema(task.getSchema());
        if (task.getStatus() != null) {
            taskData.setStatus(com.emc.caspian.ccs.imagerepo.model.Task.Status.fromValue(task.getStatus()));
        }
        if (task.getType() != null) {
            taskData.setType(com.emc.caspian.ccs.imagerepo.model.Task.Type.fromValue(task.getType()));
        }
        taskData.setUpdated_at(task.getUpdatedAt());
        return taskData;
    }
    
    public static final com.emc.caspian.ccs.imagerepo.model.Member encodeMember(Member member) {
        com.emc.caspian.ccs.imagerepo.model.Member glanceV2Member = new com.emc.caspian.ccs.imagerepo.model.Member();

        glanceV2Member.setStatus(com.emc.caspian.ccs.imagerepo.model.Member.Status.fromValue(member.getStatus()));
        glanceV2Member.setCreated_at(member.getCreatedAt());
        glanceV2Member.setUpdated_at(member.getUpdatedAt());
        glanceV2Member.setImage_id(member.getImageId());
        glanceV2Member.setMember_id(member.getMemberId());
        glanceV2Member.setSchema(member.getSchema());
        return glanceV2Member;
    }
}
