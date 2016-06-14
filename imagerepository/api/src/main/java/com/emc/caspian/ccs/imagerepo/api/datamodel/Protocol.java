package com.emc.caspian.ccs.imagerepo.api.datamodel;

/**
 * Copyright (c) 2014 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */

import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public final class Protocol {
    private static final Logger _log = LoggerFactory.getLogger(Protocol.class);

    public enum Status {
        OK(200), NO_RESPONSE(1), CREATED(2), ERROR_INTERNAL(100), ERROR_BAD_REQUEST(101), ERROR_UNAUTHORIZED(102),

        ERROR_NOT_FOUND(103), ERROR_DELETE_FAILED(104),
        NO_CONTENT(3),BAD_REQUEST(400),FORBIDDEN(403),CONFLICT(409),
        ERROR_NO_IMAGE(404), INCORRECT_VISIBILITY(403), MEMBER_EXISTS(409), ERROR_NO_MEMBER(404),
        NOT_IMPLEMENTED(800);
        private final int _value;

        private Status(final int value) {
            _value = value;
        }

        public int value() {
            return _value;
        }
    }

    public static enum RequestType {
        // all apis will have a type represented here
        GET_IMAGES_V2, GET_IMAGE_V2,

        GET_DOCKER_REPO_IMAGES, CREATE_DOCKER_REPO, CREATE_DOCKER_METADATA, GET_DOCKER_IMAGE_MEMBERS, GET_REPO_ENTRIES,

        CREATE_IMAGE_ID_V2, PATCH_IMAGES_V2, CREATE_IMAGE_V2,

        CREATE_TASK_V2, GET_TASK_V2, GET_TASKS_V2,
        
        ADD_MEMBER_V2, GET_MEMBER_V2, UPDATE_MEMBER_V2, DELETE_MEMBER_V2, GET_MEMBERS_V2
    }

    public abstract static class Request {
        // Requesttype
        RequestType requestType;

        protected Request(final RequestType requestType) {
            this.requestType = requestType;
        }

        public RequestType getRequestType() {
            return requestType;
        }
    }

    public static class ImagesRequest extends Request {
        private int limit;
        private String marker;
        private String name;
        private String visibility;
        private String memberStatus;
        private String owner;
        private int status;
        private int sizeMin;
        private int sizeMax;
        private String sortKey;
        private String sortDir;
        private String tag;
        private boolean isStatusSet;
        private boolean isSizeMinSet;
        private boolean isSizeMaxSet;

        public ImagesRequest() {
            super(RequestType.GET_IMAGES_V2);
        }

        public String getTag() {
            return tag;
        }

        public ImagesRequest setTag(final String tag) {
            this.tag = tag;
            return this;
        }

        public String getSortDir() {
            return sortDir;
        }

        public ImagesRequest setSortDir(final String sortDir) {
            this.sortDir = sortDir;
            return this;
        }

        public String getSortKey() {
            return sortKey;
        }

        public ImagesRequest setSortKey(final String sortKey) {
            this.sortKey = sortKey;
            return this;
        }

        public int getSizeMax() {
            return sizeMax;
        }

        public ImagesRequest setSizeMax(final int sizeMax) {
            this.sizeMax = sizeMax;
            this.isSizeMaxSet = true;
            return this;
        }

        public int getSizeMin() {
            return sizeMin;
        }

        public ImagesRequest setSizeMin(final int sizeMin) {
            this.sizeMin = sizeMin;
            this.isSizeMinSet = true;
            return this;
        }

        public int getStatus() {
            return status;
        }

        public ImagesRequest setStatus(final int status) {
            this.status = status;
            this.isStatusSet = true;
            return this;
        }

        public String getOwner() {
            return owner;
        }

        public ImagesRequest setOwner(final String owner) {
            this.owner = owner;
            return this;
        }

        public String getMemberStatus() {
            return memberStatus;
        }

        public ImagesRequest setMemberStatus(final String memberStatus) {
            this.memberStatus = memberStatus;
            return this;
        }

        public String getVisibility() {
            return visibility;
        }

        public ImagesRequest setVisibility(final String visibility) {
            this.visibility = visibility;
            return this;
        }

        public String getName() {
            return name;
        }

        public ImagesRequest setName(final String name) {
            this.name = name;
            return this;
        }

        public String getMarker() {
            return marker;
        }

        public ImagesRequest setMarker(final String marker) {
            this.marker = marker;
            return this;
        }

        public int getLimit() {
            return limit;
        }

        public ImagesRequest setLimit(final int limit) {
            this.limit = limit;
            return this;
        }

        public boolean isSizeMinSet() {
            return this.isSizeMinSet;
        }

        public boolean isSizeMaxSet() {
            return this.isSizeMaxSet;
        }

        public boolean isStatusSet() {
            return this.isStatusSet;
        }
    }

    public static class Response {
        private Status status = Status.OK;

        public Status getStatus() {
            return status;
        }

        public void setStatus(final Status status) {
            this.status = status;
        }
    }

    public static class ImagesResponse extends Response {
        private Iterable<Image> images;

        public Iterable<Image> getImages() {
            return images;
        }

        public void setImages(final Iterable<Image> images) {
            this.images = images;
        }
    }

    public static class ImageRequest extends Request {
        public ImageRequest() {
            super(RequestType.GET_IMAGE_V2);
        }

        public String getImageId() {
            return imageId;
        }

        public ImageRequest setImageId(final String imageId) {
            this.imageId = imageId;
            return this;
        }

        private String imageId;
    }

    public static class ImageResponse extends Response {
        private Image imageDetails;

        public Image getImage() {
            return imageDetails;
        }

        public void setImage(final Image image) {
            this.imageDetails = image;
        }
    }

    public static class DockerImageResponse extends Response {
        private DockerImage imageDetails;

        public DockerImage getImage() {
            return imageDetails;
        }

        public DockerImageResponse setImage(final DockerImage image) {
            this.imageDetails = image;
            return this;
        }
    }

    public static class DockerRepositoryRequest extends Request {
        private String repositoryName;

        private DockerRepository repository;

        public DockerRepositoryRequest() {
            super(RequestType.CREATE_DOCKER_REPO);
        }

        public String getRepositoryName() {
            return repositoryName;
        }

        public DockerRepositoryRequest setRepositoryName(String repositoryName) {
            this.repositoryName = repositoryName;
            return this;
        }

        /**
         * @return the repository
         */
        public DockerRepository getRepository() {
            return repository;
        }

        /**
         * @param repository
         *            the repository to set
         */
        public DockerRepositoryRequest setRepository(DockerRepository repository) {
            this.repository = repository;
            return this;
        }
    }

    public static class DockerRepositoryResponse extends Response {
        private DockerRepository repository;

        /**
         * @return the repository
         */
        public DockerRepository getRepository() {
            return repository;
        }

        /**
         * @param repository
         *            the repository to set
         */
        public DockerRepositoryResponse setRepository(DockerRepository repository) {
            this.repository = repository;
            return this;
        }

        private Set<String> members;

        public Set<String> getMembers() {
            return members;
        }

        public void setMembers(Set<String> members) {
            this.members = members;
        }
    }

    public static class DockerResponse extends Response {
        private Collection<String> jsonList;
        private Map<String,String> jsonMap;
        private String jsonString;
        /**
         * @return the jsonList
         */
        public Collection<String> getJsonList() {
            return jsonList;
        }
        /**
         * @param jsonList the jsonList to set
         */
        public void setJsonList(Collection<String> jsonList) {
            this.jsonList = jsonList;
        }
        /**
         * @return the jsonMap
         */
        public Map<String, String> getJsonMap() {
            return jsonMap;
        }
        /**
         * @param jsonMap the jsonMap to set
         */
        public void setJsonMap(Map<String, String> jsonMap) {
            this.jsonMap = jsonMap;
        }
        /**
         * @return the jsonString
         */
        public String getJsonString() {
            return jsonString;
        }
        /**
         * @param jsonString the jsonString to set
         */
        public void setJsonString(String jsonString) {
            this.jsonString = jsonString;
        }
    }

    public static class DockerRepoEntryRequest extends Request {
        private DockerRepoEntry repositoryEntry;

        public DockerRepoEntryRequest() {
            super(RequestType.GET_REPO_ENTRIES);
        }
        
        public DockerRepoEntryRequest(String repositoryName, String tagName) {
            super(RequestType.GET_REPO_ENTRIES);
            repositoryEntry = new DockerRepoEntry(repositoryName + "_" + tagName);
        }

        /**
         * @return the repositoryEntry
         */
        public DockerRepoEntry getRepositoryEntry() {
            return repositoryEntry;
        }

        /**
         * @param repositoryEntry the repositoryEntry to set
         */
        public DockerRepoEntryRequest setRepositoryEntry(DockerRepoEntry repositoryEntry) {
            this.repositoryEntry = repositoryEntry;
            return this;
        }
    }

    public static class DockerImageRequest extends Request {
        private DockerImage image;

        private String imageId;

        public DockerImageRequest() {
            super(RequestType.CREATE_DOCKER_METADATA);
        }

        /**
         * @return the image
         */
        public DockerImage getImage() {
            return image;
        }

        /**
         * @param image
         *            the image to set
         */
        public DockerImageRequest setImage(DockerImage image) {
            this.image = image;
            return this;
        }

        /**
         * @return the imageId
         */
        public String getImageId() {
            return imageId;
        }

        /**
         * @param imageId
         *            the imageId to set
         */
        public DockerImageRequest setImageId(String imageId) {
            this.imageId = imageId;
            return this;
        }
    }
    
    public static class ImageRequestBase extends Request {
        private Image image;

        public ImageRequestBase(final RequestType requestType) {
            super(requestType);
        }

        public ImageRequestBase setImage(final Image image) {
            this.image = image;
            return this;
        }

        public Image getImage() {
            return image;
        }
    }
    
    public static class CreateImageRequest extends ImageRequestBase {
        public CreateImageRequest() {
            super(RequestType.CREATE_IMAGE_V2);
        }
    }

    public static class UpdateImageRequest extends ImageRequestBase {
        public UpdateImageRequest() {
            super(RequestType.PATCH_IMAGES_V2);
        }
    }

    public static class ImageFileResponse extends Response {
        public InputStream getImageFile() {
            return imageFile;
        }

        public void setImageFile(final InputStream imageFile) {
            this.imageFile = imageFile;
        }

        private InputStream imageFile;
    }

    public static class CreateTaskRequest extends Request {
        private Task task;

        public CreateTaskRequest() {
            super(RequestType.CREATE_TASK_V2);
        }

        public CreateTaskRequest setTask(final Task task) {
            this.task = task;
            return this;
        }

        public Task getTask() {
            return task;
        }
    }

    public static class TaskRequest extends Request {
        private String taskId;

        public TaskRequest() {
            super(RequestType.GET_TASK_V2);
        }

        public final String getTaskId() {
            return taskId;
        }

        public final TaskRequest setTaskId(String taskId) {
            this.taskId = taskId;
            return this;
        }
    }

    public static class TaskResponse extends Response {
        private Task task;
        private URI location;

        public Task getTask() {
            return task;
        }

        public TaskResponse setTask(final Task task) {
            this.task = task;
            return this;
        }

        /**
         * @return the location
         */
        public final URI getLocation() {
            return location;
        }

        /**
         * @param location
         *            the location to set
         */
        public final TaskResponse setLocation(URI location) {
            this.location = location;
            return this;
        }
    }

    public static class TasksRequest extends Request {
        private String type;
        private String status;
        private String sortDir;
        private String sortKey;

        public TasksRequest() {
            super(RequestType.GET_TASKS_V2);
        }

        public final String getType() {
            return type;
        }

        public final TasksRequest setType(String type) {
            this.type = type;
            return this;
        }

        public final String getStatus() {
            return status;
        }

        public final TasksRequest setStatus(String status) {
            this.status = status;
            return this;
        }

        public final String getSortDir() {
            return sortDir;
        }

        public final TasksRequest setSortDir(String sortDir) {
            this.sortDir = sortDir;
            return this;
        }

        public final String getSortKey() {
            return sortKey;
        }

        public final TasksRequest setSortKey(String sortKey) {
            this.sortKey = sortKey;
            return this;
        }
    }

    public static class TasksResponse extends Response {
        private Iterable<Task> tasks;

        public Iterable<Task> getTasks() {
            return tasks;
        }

        public void setTasks(final Iterable<Task> tasks) {
            this.tasks = tasks;
        }
    }
    
    public static class MemberRequest extends Request
    {
    	private String Id;
    	private String memberId;
    	private String imageId;
    	private Member member;
    
    	public MemberRequest(RequestType type) {
            super(type);
        }
    	
    	public MemberRequest(RequestType type, String Id) {
    		super(type);
    		this.Id = Id;     
        }
    	
    	public MemberRequest(RequestType type, String Id, String imageId) {
    		super(type);
    		this.Id = Id;     
    		this.imageId = imageId;
        }
    	
    	public MemberRequest(RequestType type, String Id, String imageId, String memberId) {
    		super(type);
    		this.Id = Id;     
    		this.imageId = imageId;
    		this.memberId = memberId;
        }
    	
        public String getId() {
            return Id;
        }

        public MemberRequest setId(final String Id) {
            this.Id = Id;
            return this;
        }
        
    	public String getMemberId() {
            return memberId;
        }
    	
    	public void setMemberId(String memberId) {
    		this.memberId = memberId;
    	}
    	
    	public String getImageId() {
            return imageId;
        }
    	
    	public void setImageId(String imageId) {
    		this.imageId = imageId;
    	}
    	
    	public Member getMember() {
            return member;
        }
    	
    	public void setMember(Member member) {
    		this.member = member;
    	}
    }
        
    public static class MemberResponse extends Response
    {
        private Member member;
        private Iterable<Member> members;

        public Iterable<Member> getMembers() {
            return members;
        }

        public void setMembers(final Iterable<Member> members) {
            this.members = members;
        }
        
        public Member getMember() {
            return member;
        }

        public void setMember(final Member member) {
            this.member = member;
        }
    }
}