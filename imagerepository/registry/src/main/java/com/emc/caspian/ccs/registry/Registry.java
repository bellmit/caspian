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
package com.emc.caspian.ccs.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jersey.repackaged.com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspain.ccs.common.webfilters.KeystonePrincipal;
import com.emc.caspian.ccs.datastore.DataStore;
import com.emc.caspian.ccs.datastore.DataStoreFactory;
import com.emc.caspian.ccs.datastore.DataStoreType;
import com.emc.caspian.ccs.datastore.PropertyBag;
import com.emc.caspian.ccs.datastore.expressiontree.BinaryExpression;
import com.emc.caspian.ccs.datastore.expressiontree.ConstantExpression;
import com.emc.caspian.ccs.datastore.expressiontree.Expression;
import com.emc.caspian.ccs.datastore.expressiontree.ParameterNameExpression;
import com.emc.caspian.ccs.datastore.expressiontree.StringExpression;
import com.emc.caspian.ccs.datastore.mysql.MySqlStore;
import com.emc.caspian.ccs.imagerepo.api.datamodel.DockerImage;
import com.emc.caspian.ccs.imagerepo.api.datamodel.DockerRepoEntry;
import com.emc.caspian.ccs.imagerepo.api.datamodel.DockerRepository;
import com.emc.caspian.ccs.imagerepo.api.datamodel.EntityType;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Image;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Member;
import com.emc.caspian.ccs.imagerepo.api.datamodel.MetadataBase;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol.ImageRequest;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol.MemberRequest;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol.TasksRequest;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Task;
import com.emc.caspian.ccs.imagerepo.api.exceptionhandling.ExceptionToStatus;
import com.emc.caspian.fabric.config.Configuration;
import com.emc.caspian.fabric.lang.Sequence;
import com.emc.caspian.fabric.lang.Sequences;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

/**
 * @author shivesh,shivat
 */
public final class Registry {
    private static final Logger _log = LoggerFactory.getLogger(Registry.class);

    public static final Integer WORKER_THREADS = Configuration.make(Integer.class, "metadata.store.max_worker_threads", "10")
            .value();

    public static final ExecutorService pool = Executors.newFixedThreadPool(10);

    private static final DataStore ds = DataStoreFactory.getImageStore(DataStoreType.MYSQL);

    private static final ExceptionToStatus exceptionMapper = DataStoreFactory.getExceptionMapper(DataStoreType.MYSQL);

    // init method:
    // read configuration to determine the datastore type.
    // initialize data store object

    // methods for gets and sets of different entity types.
    // these methods will deal with model.

    public static Future<Image> getImageDetails(final Protocol.ImageRequest request) {
        Future<Image> imageFuture = pool.submit(new Callable<Image>() {
            @Override
            public Image call() {
                return (Image) ds.get(EntityType.IMAGE, request.getImageId()).getEntityObject();
            }
        });
        return imageFuture;
    }

    public static Future<Image> getImageDetailsByName(final Protocol.ImageRequest request) {
        Future<Image> imageFuture = pool.submit(new Callable<Image>() {
            @Override
            public Image call() {
            	//name is stored in ID
            	//TODO check if multi-image can have same name
            	final Expression expression = getExpression(request);
            	PropertyBag<? extends MetadataBase>[] listEntities= ds.get(EntityType.IMAGE, expression, 200, null, null, null);
            	if(listEntities.length > 1)
            		return null;
            	else
            		return (Image) listEntities[0].getEntityObject();
            }
            private Expression getExpression(final ImageRequest request) {
                BinaryExpression expression = null;

                if (StringUtils.isNotEmpty(request.getImageId())) {
                    BinaryExpression binaryExpression1 = BinaryExpression.equalTo(
                            new ParameterNameExpression(MySqlStore.Glancemeta.KeyName.getColumnName()),
                            new ConstantExpression("name")
                    );
                    BinaryExpression binaryExpression2 = BinaryExpression.equalTo(
                            new ParameterNameExpression(MySqlStore.Glancemeta.KeyValue.getColumnName()),
                            new StringExpression(request.getImageId())
                    );
                    final BinaryExpression name = BinaryExpression.and(binaryExpression1,
                                                                       binaryExpression2
                    );
                    expression = expression == null ? name : BinaryExpression.or(expression, name);
                }
                return expression;
            }
        });
        return imageFuture;
    }

    public static Future<DockerImage> getDockerImageDetails(final Protocol.DockerImageRequest request) {
        Future<DockerImage> imageFuture = pool.submit(new Callable<DockerImage>()
                                          {
                                              @Override
                                              public DockerImage call() {
                                                  return (DockerImage) ds.get(EntityType.DOCKER_IMAGE, request.getImageId()).getEntityObject();
                                              }
                                          });
        return imageFuture;
    }

    public static Future<DockerRepository> getDockerRespositoryDetails(final Protocol.DockerRepositoryRequest request) {
        Future<DockerRepository> repoFuture = pool.submit(new Callable<DockerRepository>()
                                              {
                                                  @Override
                                                  public DockerRepository call() {
                                                      return (DockerRepository) ds.get(EntityType.DOCKER_REPOSITORY, request.getRepositoryName()).getEntityObject();
                                                  }
                                              });
        return repoFuture;
    }

    public static Future<DockerRepoEntry> getDockerRepoEntryDetails(final Protocol.DockerRepoEntryRequest request) {
        Future<DockerRepoEntry> repoEntryFuture = pool.submit(new Callable<DockerRepoEntry>()
                                                  {
                                                      @Override
                                                      public DockerRepoEntry call() {
                                                          return (DockerRepoEntry) ds.get(EntityType.DOCKER_REPO_ENTRY, request.getRepositoryEntry().getId()).getEntityObject();
                                                      }
                                                  });
        return repoEntryFuture;
    }

    public static Future<Iterable<Image>> getImages(final Protocol.ImagesRequest request) {
        // make the sql query for all the images in a loop unless we have an api to query for all
        // images. need to
        // include that!
        Future<Iterable<Image>> imageListFuture =
                pool.submit(new Callable<Iterable<Image>>()
                            {
                                @Override
                                public Iterable<Image> call() {
                                    final Expression expression = getExpression(request);
                                    final List<PropertyBag<? extends MetadataBase>> listEntities = Arrays.asList(ds.get(EntityType.IMAGE,
                                                                                                                        expression,
                                                                                                                        request.getLimit(),
                                                                                                                        request.getMarker(),
                                                                                                                        request.getSortKey(),
                                                                                                                        request.getSortDir()
                                                                                                                 ));
                                    final List<Image> transform = Lists.newArrayList(Iterables.transform(listEntities,
                                                                                                   new Function<PropertyBag<?>, Image>()
                                                                                                   {
                                                                                                       @Override
                                                                                                       public Image apply(final PropertyBag<?> input) {
                                                                                                           return ((PropertyBag<Image>) input).getEntityObject();
                                                                                                       }
                                                                                                   }
                                                                               )
                                    );
                                    return transform;
                                }

                                private Expression getExpression(final Protocol.ImagesRequest request) {
                                    BinaryExpression expression = null;

                                    if (StringUtils.isNotEmpty(request.getName())) {
                                        BinaryExpression binaryExpression1 = BinaryExpression.equalTo(
                                                new ParameterNameExpression(MySqlStore.Glancemeta.KeyName.getColumnName()),
                                                new ConstantExpression("name")
                                        );
                                        BinaryExpression binaryExpression2 = BinaryExpression.equalTo(
                                                new ParameterNameExpression(MySqlStore.Glancemeta.KeyValue.getColumnName()),
                                                new StringExpression(request.getName())
                                        );
                                        final BinaryExpression name = BinaryExpression.and(binaryExpression1,
                                                                                           binaryExpression2
                                        );
                                        expression = expression == null ? name : BinaryExpression.or(expression, name);
                                    }

                                    if (StringUtils.isNotEmpty(request.getOwner())) {
                                        BinaryExpression binaryExpression1 = BinaryExpression.equalTo(
                                                new ParameterNameExpression(MySqlStore.Glancemeta.KeyName.getColumnName()),
                                                new ConstantExpression("owner")
                                        );
                                        BinaryExpression binaryExpression2 = BinaryExpression.equalTo(
                                                new ParameterNameExpression(MySqlStore.Glancemeta.KeyValue.getColumnName()),
                                                new ConstantExpression(request.getOwner())
                                        );
                                        final BinaryExpression owner = BinaryExpression.and(binaryExpression1,
                                                                                            binaryExpression2
                                        );
                                        expression = expression == null ? owner : BinaryExpression.or(expression, owner);
                                    }

                                    if (StringUtils.isNotEmpty(request.getTag())) {
                                        BinaryExpression binaryExpression1 = BinaryExpression.equalTo(
                                                new ConstantExpression(MySqlStore.Glancemeta.KeyName.getColumnName()),
                                                new ConstantExpression("tag")
                                        );
                                        BinaryExpression binaryExpression2 = BinaryExpression.equalTo(
                                                new ParameterNameExpression(MySqlStore.Glancemeta.KeyValue.getColumnName()),
                                                new ConstantExpression(request.getTag())
                                        );
                                        final BinaryExpression tag = BinaryExpression.and(binaryExpression1,
                                                                                          binaryExpression2
                                        );
                                        expression = expression == null ? tag : BinaryExpression.or(expression, tag);
                                    }

                                    if (StringUtils.isNotEmpty(request.getVisibility())) {
                                        BinaryExpression binaryExpression1 = BinaryExpression.equalTo(
                                                new ParameterNameExpression(MySqlStore.Glancemeta.KeyName.getColumnName()),
                                                new ConstantExpression("visibility")
                                        );
                                        BinaryExpression binaryExpression2 = BinaryExpression.equalTo(
                                                new ParameterNameExpression(MySqlStore.Glancemeta.KeyValue.getColumnName()),
                                                new ConstantExpression(request.getVisibility())
                                        );
                                        final BinaryExpression visibility = BinaryExpression.and(binaryExpression1,
                                                                                                 binaryExpression2
                                        );
                                        expression = expression == null ? visibility : BinaryExpression.or(expression, visibility);
                                    }

                                    if (StringUtils.isNotEmpty(request.getMemberStatus())) {
                                        BinaryExpression binaryExpression1 = BinaryExpression.equalTo(
                                                new ParameterNameExpression(MySqlStore.Glancemeta.KeyName.getColumnName()),
                                                new ConstantExpression("memberStatus")
                                        );
                                        BinaryExpression binaryExpression2 = BinaryExpression.equalTo(
                                                new ParameterNameExpression(MySqlStore.Glancemeta.KeyValue.getColumnName()),
                                                new ConstantExpression(request.getMemberStatus())
                                        );
                                        final BinaryExpression memberStatus = BinaryExpression.and(binaryExpression1,
                                                                                                   binaryExpression2
                                        );
                                        expression = expression == null ? memberStatus : BinaryExpression.or(expression, memberStatus);
                                    }

                                    if (request.isSizeMinSet()) {
                                        BinaryExpression binaryExpression1 = BinaryExpression.equalTo(
                                                new ParameterNameExpression(MySqlStore.Glancemeta.KeyName.getColumnName()),
                                                new ConstantExpression("size_min")
                                        );

                                        BinaryExpression binaryExpression2 = BinaryExpression.greaterThan(
                                                new ParameterNameExpression(MySqlStore.Glancemeta.KeyValue.getColumnName()),
                                                new ConstantExpression("" + request.getSizeMin())
                                        );

                                        final BinaryExpression sizeMin = BinaryExpression.and(binaryExpression1,
                                                                                              binaryExpression2
                                        );

                                        expression = expression == null ? sizeMin : BinaryExpression.or(expression, sizeMin);
                                    }

                                    if (request.isSizeMaxSet()) {
                                        BinaryExpression binaryExpression1 = BinaryExpression.equalTo(
                                                new ParameterNameExpression(MySqlStore.Glancemeta.KeyName.getColumnName()),
                                                new ConstantExpression("size_max")
                                        );

                                        BinaryExpression binaryExpression2 = BinaryExpression.lessThan(
                                                new ParameterNameExpression(MySqlStore.Glancemeta.KeyValue.getColumnName()),
                                                new ConstantExpression("" + request.getSizeMax())
                                        );

                                        final BinaryExpression sizeMax = BinaryExpression.and(binaryExpression1,
                                                                                              binaryExpression2
                                        );

                                        expression = expression == null ? sizeMax : BinaryExpression.or(expression, sizeMax);
                                    }

                                    if (request.isStatusSet()) {
                                        BinaryExpression binaryExpression1 = BinaryExpression.equalTo(
                                                new ParameterNameExpression(MySqlStore.Glancemeta.KeyName.getColumnName()),
                                                new ConstantExpression("status")
                                        );

                                        BinaryExpression binaryExpression2 = BinaryExpression.equalTo(
                                                new ParameterNameExpression(MySqlStore.Glancemeta.KeyValue.getColumnName()),
                                                new ConstantExpression(Image.Status.values()[request.getStatus()].name())
                                        );

                                        final BinaryExpression status = BinaryExpression.and(binaryExpression1,
                                                                                             binaryExpression2
                                        );
                                        expression = expression == null ? status : BinaryExpression.or(expression, status);
                                    }

                                    return expression;
                                }
                            }
                );

        return imageListFuture;
    }

    public static Future<Image> createImage(final Protocol.CreateImageRequest request) {
      	 //get the sql queries -> setID setURI setStatus(active)
           //execute the queries
      	 //uploadImage
      	Future<Image> imageFuture = pool.submit(new Callable<Image>()
                  {
                      @Override
                      public Image call() {
                          ds.insert(new PropertyBag<Image>(request.getImage()));
   						return request.getImage();
                      }
                  }
   		);
   		return imageFuture;
    }

    public static Future<Image> updateImageDetails(final Protocol.UpdateImageRequest request) {
    	Future<Image> imageFuture = pool.submit(new Callable<Image>()
                {
                    @Override
                    public Image call() {
                    	ds.update(new PropertyBag<Image>(request.getImage()));
						return request.getImage();
                    }
                }
		);
		return imageFuture;
    }

    public static void deleteImage(final ImageRequest request) {
		Future<Image> imageFuture = pool.submit(new Callable<Image>()
                {
                    @Override
                    public Image call() {
                    	ds.delete(EntityType.IMAGE, request.getImageId());
						return null;
                    }
                }
		);
		return;
	}

    public static Future<Protocol.DockerImageResponse> updateDockerImageDetails(final Protocol.DockerImageRequest request) {
        Future<Protocol.DockerImageResponse> imageUpdateFuture =
                pool.submit(new Callable<Protocol.DockerImageResponse>()
                {
                    @Override
                    public Protocol.DockerImageResponse call() {
                        Protocol.DockerImageResponse response = new Protocol.DockerImageResponse();
                        PropertyBag<DockerImage> propBag = new PropertyBag<DockerImage>(request.getImage());
                        ds.update(propBag);
                        response.setStatus(Protocol.Status.OK);
                        response.setImage(request.getImage());
                        return response;
                    }
                }
                );
        return imageUpdateFuture;
    }

    public static Future<Protocol.DockerRepositoryResponse> updateDockerRepository(final Protocol.DockerRepositoryRequest request) {
        Future<Protocol.DockerRepositoryResponse> repositoryUpdateFuture = pool.submit(new Callable<Protocol.DockerRepositoryResponse>()
                {
                     @Override
                     public Protocol.DockerRepositoryResponse call() {
                         Protocol.DockerRepositoryResponse response = new Protocol.DockerRepositoryResponse();
                         DockerRepository repositoryEntity = request.getRepository();
                         PropertyBag<DockerRepository> propBag = new PropertyBag<DockerRepository>(repositoryEntity);
                         ds.update(propBag);

                         //TODO updating REPOSITORY and adding of new REPO_ENTRY must happen in single transaction
                         //requires adding new api in datastore
                         //Flattening the DockerRepository.repositoryMap and persisting it as REPONAME_TAGNAME : IMAGEID
                         final List<PropertyBag<?>> propBags = flattenRepoMap(repositoryEntity);
                         for (Iterator iterator = propBags.iterator(); iterator.hasNext();) {
                            PropertyBag<?> propertyBag = (PropertyBag<?>) iterator.next();
                            ds.update(propertyBag);
                        }
                         response.setStatus(Protocol.Status.OK);
                         response.setRepository(request.getRepository());
                         return response;
                     }
                }
                );
        return repositoryUpdateFuture;
    }

    public static Future<Protocol.DockerImageResponse> createDockerImage(final Protocol.DockerImageRequest request) {

        //persisting only those attributes which will be used in serving other requests
        _log.debug("Put image metadata json received for image {}", request.getImage().getId());

        final DockerImage dockerImage = request.getImage();

        final PropertyBag<DockerImage> propBag = new PropertyBag<DockerImage>(dockerImage);
        Future<Protocol.DockerImageResponse> imageFuture = pool.submit(new Callable<Protocol.DockerImageResponse>()
                                                           {
                                                                @Override
                                                                public Protocol.DockerImageResponse call() throws DuplicateEntityException {
                                                                    Protocol.DockerImageResponse response = new Protocol.DockerImageResponse();
                                                                    //TODO must handle get and insert as atomic operation
                                                                    DockerImage dockerImage = (DockerImage) ds.get(EntityType.DOCKER_IMAGE, request.getImageId()).getEntityObject();

                                                                    if(dockerImage.getId() != null) {
                                                                       throw new DuplicateEntityException(); 
                                                                    }
                                                                    //TODO if binary data was uploaded before even uploading json, should be calling ds.update()
                                                                    ds.insert(propBag);
                                                                    response.setStatus(Protocol.Status.OK);
                                                                    return response.setImage(dockerImage);
                                                                }
                                                            }
        );
        return imageFuture;
    }

    //TODO comments
    public static Future<Protocol.DockerRepositoryResponse> createRepository(Protocol.DockerRepositoryRequest request) {

        //persisting imageId, tags, repositoryName
        _log.debug("Put repository {} json request received", request.getRepository().getName());

        final DockerRepository repositoryEntity = request.getRepository();
        //Flattening the DockerRepository.repositoryMap and persisting it as REPONAME_TAGNAME : IMAGEID
        final List<PropertyBag<?>> propBags = flattenRepoMap(repositoryEntity);

        Future<Protocol.DockerRepositoryResponse> imageFuture = pool.submit(new Callable<Protocol.DockerRepositoryResponse>()
                                                                {
                                                                    @Override
                                                                    public Protocol.DockerRepositoryResponse call() {
                                                                        Protocol.DockerRepositoryResponse response = new Protocol.DockerRepositoryResponse();
                                                                        ds.insert(propBags);
                                                                        response.setStatus(Protocol.Status.OK);
                                                                        response.setRepository(repositoryEntity);
                                                                        return response;
                                                                    }
                                                                }
        );
        return imageFuture;
    }

    private static List<PropertyBag<?>> flattenRepoMap(DockerRepository repository) {

        String repositoryName = repository.getName();
        Map<String,String> repoMap = repository.getRepositoryMap();

        List<PropertyBag<?>> repoEntryList = new ArrayList<PropertyBag<?>>();
        DockerRepoEntry repoEntry;

        for (Map.Entry<String,String> e : repoMap.entrySet()) {
            String tagName = e.getKey();
            String imageId = e.getValue();
            repoEntry = new DockerRepoEntry(repositoryName + "_" + tagName);
            repoEntry.setRepositoryName(repositoryName);
            repoEntry.setTag(tagName);
            repoEntry.setImageGUID(imageId);
            repoEntryList.add(new PropertyBag<DockerRepoEntry>(repoEntry));
        }
        return repoEntryList;
    }

    public static Future<Protocol.Response> addImageToRepositoryFile(Protocol.DockerRepositoryRequest request) {
        //TODO This method must make DB entries of imageId to tags mapping for an already existing repository
        return null;
    }

    public static Sequence<String> getImageAncestry(final Protocol.DockerImageRequest request) {
        Sequence<String> ancestry = getImageAncestry(request.getImageId());
        if (ancestry.length() == 1){
            //List will have just the imageId passed and this implies the given imageId doesn't exist in DB
            throw new IllegalArgumentException("image id");
        }
        return ancestry;
    }

    /**
     * Helper method that picks an image metadata file and reads the parent and recursively calls to find all
     * ancestors and returns the list
     *
     * @param imageId
     *
     * @return ancestry tree sequence
     */
    private static Sequence<String> getImageAncestry(final String imageId) {
        final Sequences.SequenceBuilder<String> ancestry = new Sequences.SequenceBuilder<String>();
        ancestry.add(imageId);

        DockerImage dockerImage = (DockerImage) ds.get(EntityType.DOCKER_IMAGE, imageId).getEntityObject();
        final String parent = dockerImage.getParentId();

        if (!Strings.isNullOrEmpty(parent))
            ancestry.add(getImageAncestry(parent));

        return ancestry.build();
    }

    public static boolean checkUserQuota(final KeystonePrincipal principal, final long size) {
        return false;
    }

    public static ExceptionToStatus fetchExceptionMapper() {
        return exceptionMapper;
    }

    public static Future<Protocol.TaskResponse> getTaskDetails(final Protocol.TaskRequest request) {
        Future<Protocol.TaskResponse> taskFuture = pool.submit(new Callable<Protocol.TaskResponse>() {
            @Override
            public Protocol.TaskResponse call() {
                Task result = (Task) ds.get(EntityType.TASK, request.getTaskId()).getEntityObject();
                Protocol.TaskResponse response = new Protocol.TaskResponse().setTask(result);
                response.setStatus(Protocol.Status.OK);
                return response;

            }
        });
        return taskFuture;
    }

    public static Future<Void> updateTask(final Task taskData) {
        return pool.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                ds.update(new PropertyBag<Task>(taskData));
                return null;
            }
        });
    }

    public static Future<Void> insertTask(final Task taskData) {
        return pool.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                ds.insert(new PropertyBag<Task>(taskData));
                return null;
            }
        });
    }

    public static Future<Void> createImage(final Image image) {
        return pool.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                ds.insert(new PropertyBag<Image>(image));
                return null;
            }
        });
    }

    public static Future<Iterable<Task>> getTasks(final TasksRequest request) {
        Future<Iterable<Task>> imageListFuture = pool.submit(new Callable<Iterable<Task>>() {
            @Override
            public Iterable<Task> call() {
                final Expression expression = getExpression(request);
                // fetch task from db.
                final List<PropertyBag<? extends MetadataBase>> listEntities = Arrays.asList(ds.get(EntityType.TASK, expression,
                        0, null, request.getSortKey(), request.getSortDir()));

                final List<Task> transform = Lists.newArrayList(Iterables.transform(listEntities,
                        new Function<PropertyBag<?>, Task>() {
                            @SuppressWarnings("unchecked")
                            @Override
                            public Task apply(final PropertyBag<?> input) {
                                return ((PropertyBag<Task>) input).getEntityObject();
                            }
                        }));
                return transform;
            }

            private Expression getExpression(final Protocol.TasksRequest request) {
                BinaryExpression expression = null;

                if (StringUtils.isNotEmpty(request.getType())) {
                    BinaryExpression binaryExpression1 = BinaryExpression.equalTo(new ParameterNameExpression(
                            MySqlStore.Glancemeta.KeyName.getColumnName()), new ConstantExpression("type"));
                    BinaryExpression binaryExpression2 = BinaryExpression.equalTo(new ParameterNameExpression(
                            MySqlStore.Glancemeta.KeyValue.getColumnName()), new ConstantExpression(request.getType()));
                    final BinaryExpression type = BinaryExpression.and(binaryExpression1, binaryExpression2);
                    expression = expression == null ? type : BinaryExpression.or(expression, type);
                }

                if (StringUtils.isNotEmpty(request.getStatus())) {
                    BinaryExpression binaryExpression1 = BinaryExpression.equalTo(new ParameterNameExpression(
                            MySqlStore.Glancemeta.KeyName.getColumnName()), new ConstantExpression("status"));
                    BinaryExpression binaryExpression2 = BinaryExpression.equalTo(new ParameterNameExpression(
                            MySqlStore.Glancemeta.KeyValue.getColumnName()), new ConstantExpression(request.getStatus()));
                    final BinaryExpression status = BinaryExpression.and(binaryExpression1, binaryExpression2);
                    expression = expression == null ? status : BinaryExpression.or(expression, status);
                }

                //Filter entity type task
                BinaryExpression entityFilterExpression = BinaryExpression.equalTo(new ParameterNameExpression(
                        MySqlStore.Glancemeta.EntityType.getColumnName()), new ConstantExpression(EntityType.TASK.toString()));

                expression = (expression == null)? entityFilterExpression : BinaryExpression.and(entityFilterExpression, expression);
                return expression;
            }
        });

        return imageListFuture;
    }

    public static Future<Iterable<DockerRepoEntry>> getDockerRepoEntryDetailsForImageId(final Protocol.DockerImageRequest request) {
        Future<Iterable<DockerRepoEntry>> repoListEntryFuture = pool.submit(new Callable<Iterable<DockerRepoEntry>>() {
            @Override
            public Iterable<DockerRepoEntry> call() {
                BinaryExpression expression = null;

                if (StringUtils.isNotEmpty(request.getImageId())) {
                    BinaryExpression binaryExpression1 = BinaryExpression.equalTo(
                            new ParameterNameExpression(MySqlStore.Glancemeta.KeyName.getColumnName()),
                            new ConstantExpression("imageGUID")
                    );
                    BinaryExpression binaryExpression2 = BinaryExpression.equalTo(
                            new ParameterNameExpression(MySqlStore.Glancemeta.KeyValue.getColumnName()),
                            new ConstantExpression(request.getImageId())
                    );
                    expression = BinaryExpression.and(binaryExpression1, binaryExpression2);
                }
                final List<PropertyBag<? extends MetadataBase>> listDockerRepoEntries = Arrays.asList( ds.get(EntityType.DOCKER_REPO_ENTRY, expression, 0, null, null, null));

                final List<DockerRepoEntry> transform = Lists.newArrayList(Iterables.transform(listDockerRepoEntries,
                        new Function<PropertyBag<?>, DockerRepoEntry>() {
                            @SuppressWarnings("unchecked")
                            @Override
                            public DockerRepoEntry apply(final PropertyBag<?> input) {
                                return ((PropertyBag<DockerRepoEntry>) input).getEntityObject();
                            }
                        }));
                return transform;
            }
        });
        return repoListEntryFuture;
    }
    
    // Returns the id of member if exists
    public static Future<String> getExistingMember(final MemberRequest request) {
        Future<String> member_id = pool.submit(new Callable<String>()
        {
        	 	   @Override
                   public String call() {
                     return (String) ds.getPropertyValue(EntityType.MEMBER, request.getId(), "id" );
    			   
        	 	   }
        }
        );
        return member_id;            
    }
    
    public static Future<Member> addMember(final MemberRequest request) {
   	     Future<Member> memberFuture = pool.submit(new Callable<Member>()
         {
                   @Override
                   public Member call() {
                      ds.insert(new PropertyBag<Member>(request.getMember()));
				      return request.getMember();
                   }
         }
		 );
		 return memberFuture;
   }
    
    public static Future<Member> getMember(final MemberRequest request) {
       	Future<Member> memberFuture = pool.submit(new Callable<Member>()
                   {
                       @Override
                       public Member call() {
                          return (Member) ds.get(EntityType.MEMBER, request.getId()).getEntityObject();
                       }
                   }
    		);
    		return memberFuture;
       }                  
    
    public static Future<Member> deleteMember(final MemberRequest request) {
		Future<Member> memberFuture = pool.submit(new Callable<Member>()
                {
                    @Override
                    public Member call() {
                    	ds.delete(EntityType.MEMBER, request.getId());
						return request.getMember();
                    }
                }
		);
		return memberFuture;		
	}
    
    public static Future<Member> updateMember(final Member member) {
    	Future<Member> memberFuture = pool.submit(new Callable<Member>()
                {
                    @Override
                    public Member call() {
                    	ds.update(new PropertyBag<Member>(member));
                    	return member;
                    }
                }
		);
		return memberFuture;
    }
    
    public static Future<Iterable<Member>> getMembers(final Protocol.MemberRequest request) {
    	final String imageId = request.getImageId();
        
    	Future<Iterable<Member>> members = pool.submit(new Callable<Iterable<Member>>() 
    			{
            		@Override
            		public Iterable<Member> call() {
            			 BinaryExpression expression1 = BinaryExpression.equalTo(
                                 new ParameterNameExpression(MySqlStore.Glancemeta.KeyName.getColumnName()),
                                 new ConstantExpression("imageId"));
                                 
                         BinaryExpression expression2 = BinaryExpression.equalTo(
                                 new ParameterNameExpression(MySqlStore.Glancemeta.KeyValue.getColumnName()),
                                 new ConstantExpression(imageId));
                         
                         BinaryExpression expression = BinaryExpression.and(expression1, expression2);
            			
            			 final List<PropertyBag<? extends MetadataBase>> listEntities = Arrays.asList(ds.get(EntityType.MEMBER, expression, 0, null, null, null));

            			 final List<Member> memberList = Lists.newArrayList(Iterables.transform(listEntities, new Function<PropertyBag<?>, Member>() {
                              @Override
                              public Member apply(final PropertyBag<?> input) {
                                 return ((PropertyBag<Member>) input).getEntityObject();
                              }
                         }));
                         return memberList;
            		}
            });
            return members;
    }
}
