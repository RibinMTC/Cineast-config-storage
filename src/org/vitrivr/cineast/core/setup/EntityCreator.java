package org.vitrivr.cineast.core.setup;

import java.util.HashMap;
import java.util.HashSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.db.dao.TagHandler;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.setup.AttributeDefinition.AttributeType;

public interface EntityCreator extends AutoCloseable {
    /**
     * Logger instance used for logging.
     */
    Logger LOGGER = LogManager.getLogger();

    /**
     * Name of the 'clean' flag.
     */
    public static final String SETUP_FLAG_CLEAN = "clean";

    /**
     * Performs the setup of the persistent layer by executing all the necessary entity
     * creation steps in a sequence.
     * <p>
     * The options map supports the following flags:
     * <p>
     * - clean: Drops all entities before creating new ones.
     *
     * @param options Options that can be provided for setup.
     * @return boolean Indicating success or failure of the setup.
     */
    default boolean setup(HashMap<String, String> options) {

        if (options.containsKey(SETUP_FLAG_CLEAN)) {
            LOGGER.info("Dropping all entities");
            this.dropAllEntities();
        }

        LOGGER.info("Setting up basic entities...");

        this.createMultiMediaObjectsEntity();
        this.createMetadataEntity();
        this.createSegmentMetadataEntity();
        this.createSegmentEntity();
        this.createTagEntity();

        LOGGER.info("...done");


        LOGGER.info("Collecting retriever classes...");

        HashSet<Retriever> retrievers = new HashSet<>();
        for (String category : Config.sharedConfig().getRetriever().getRetrieverCategories()) {
            retrievers.addAll(Config.sharedConfig().getRetriever().getRetrieversByCategory(category).keySet());
        }

        LOGGER.info("...done");

        for (Retriever r : retrievers) {
            LOGGER.info("Setting up " + r.getClass().getSimpleName());
            r.initalizePersistentLayer(() -> this);
        }

        System.out.println("Setup complete!");

        return true;
    }


    /**
     * Drops all entities currently required by Cineast.
     */
    default void dropAllEntities() {

        LOGGER.info("Dropping basic entities...");

        this.dropMultiMediaObjectsEntity();
        this.dropMetadataEntity();
        this.dropSegmentEntity();
        this.dropTagEntity();

        LOGGER.info("...done");

        HashSet<Retriever> retrievers = new HashSet<>();
        for (String category : Config.sharedConfig().getRetriever().getRetrieverCategories()) {
            retrievers.addAll(Config.sharedConfig().getRetriever().getRetrieversByCategory(category).keySet());
        }

        for (Retriever r : retrievers) {
            LOGGER.info("Dropping " + r.getClass().getSimpleName());
            r.dropPersistentLayer(() -> this);
        }
    }

    /**
     * Initialises the main entity holding information about multimedia objects
     */
    boolean createMultiMediaObjectsEntity();

    /**
     * Initialises the entity responsible for holding metadata information about multimedia objects.
     */
    boolean createMetadataEntity();

    /**
     * Initialises the entity responsible for holding metadata information about multimedia segments.
     */
    boolean createSegmentMetadataEntity();

    /**
     * Initialises the entity responsible for holding the mapping between human readable tags and their descriptions to the internally used ids
     */
    default boolean createTagEntity() {
        return this.createIdEntity(TagHandler.ENTITY, new AttributeDefinition("name", AttributeType.STRING), new AttributeDefinition("description", AttributeType.STRING));
    }


    /**
     * Initializes the entity responsible for holding information about segments of a multimedia object
     */
    boolean createSegmentEntity();

    /**
     * Drops the main entity holding information about multimedia objects
     */
    default boolean dropMultiMediaObjectsEntity() {
        if (this.dropEntity(MultimediaObjectDescriptor.ENTITY)) {
            LOGGER.info("Successfully dropped multimedia object entity.");
            return true;
        } else {
            LOGGER.error("Error occurred while dropping multimedia object entity");
            return false;
        }
    }

    /**
     * Drops the entity responsible for holding information about segments of a multimedia object
     */
    default boolean dropSegmentEntity() {
        if (this.dropEntity(SegmentDescriptor.ENTITY)) {
            LOGGER.info("Successfully dropped segment entity.");
            return true;
        } else {
            LOGGER.error("Error occurred while dropping segment entity");
            return false;
        }
    }

    /**
     * Drops the entity responsible for holding metadata information about multimedia objects.
     */
    default boolean dropMetadataEntity() {
        if (this.dropEntity(MultimediaMetadataDescriptor.ENTITY)) {
            LOGGER.info("Successfully dropped metadata entity.");
            return true;
        } else {
            LOGGER.error("Error occurred while dropping metadata entity");
            return false;
        }
    }

    /**
     * Drops the entity responsible for holding metadata information about multimedia objects.
     */
    default boolean dropTagEntity() {
        if (this.dropEntity(TagHandler.ENTITY)) {
            LOGGER.info("Successfully dropped tag entity.");
            return true;
        } else {
            LOGGER.error("Error occurred while dropping tag entity");
            return false;
        }
    }

    /**
     * Creates and initializes an entity for a feature module with default parameters
     *
     * @param featurename the name of the feature module
     * @param unique      true if the feature module produces at most one vector per segment
     */
    default boolean createFeatureEntity(String featurename, boolean unique) {
        return createFeatureEntity(featurename, unique, "feature");
    }

    boolean createFeatureEntity(String featurename, boolean unique, String... featureNames);

    boolean createFeatureEntity(String featurename, boolean unique, AttributeDefinition... attributes);

    /**
     * Creates and initializes an entity with the provided name and the provided attributes. The new entity will have an additional
     * field prepended, called "id", which is of type "string" and has an index.
     *
     * @param entityName Name of the new entity.
     * @param attributes List of {@link AttributeDefinition} objects specifying the new entities attributes.
     * @return True on success, false otherwise.
     */
    boolean createIdEntity(String entityName, AttributeDefinition... attributes);

    /**
     * Creates and initializes an entity with the provided name and the provided attributes.
     *
     * @param entityName Name of the new entity.
     * @param attributes List of {@link AttributeDefinition} objects specifying the new entities attributes.
     * @return True on success, false otherwise.
     */
    boolean createEntity(String entityName, AttributeDefinition... attributes);

    /**
     * @param entityName
     * @return
     */
    boolean existsEntity(String entityName);

    /**
     * drops an entity, returns <code>true</code> if the entity was successfully dropped, <code>false</code> otherwise
     *
     * @param entityName the entity to drop
     */
    boolean dropEntity(String entityName);

    @Override
    void close();
}