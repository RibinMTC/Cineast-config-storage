package org.vitrivr.cineast.core.features;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.vitrivr.cineast.core.config.AestheticPredictorConfig;
import org.vitrivr.cineast.core.data.entities.SimplePrimitiveTypeProviderFeatureDescriptor;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.db.dao.writer.PrimitiveTypeProviderFeatureDescriptorWriter;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.remote_predictor_communication.RemotePredictorCommunication;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ConfigurableAestheticFeatureExtractor implements Extractor {

    private static final Logger LOGGER = LogManager.getLogger();
    protected final String apiAddress;
    protected final String tableName;
    private final AttributeDefinition[] columnNameAndType;
    private final boolean videoSupport;
    private final boolean multipleValuesPerPrediction;
    private final String valuesSeparator = ",";

    protected PrimitiveTypeProviderFeatureDescriptorWriter primitiveWriter;
    protected PersistencyWriter<?> phandler;

    public ConfigurableAestheticFeatureExtractor(AestheticPredictorConfig aestheticPredictorConfig) {
        this.apiAddress = aestheticPredictorConfig.getApiAddress();
        this.tableName = aestheticPredictorConfig.getTableName();
        this.columnNameAndType = getColumnNameAndType(aestheticPredictorConfig.getColumnNameAndType());
        this.videoSupport = aestheticPredictorConfig.getVideoSupport();
        this.multipleValuesPerPrediction = aestheticPredictorConfig.getMultipleValuesPerPrediction();
        if (multipleValuesPerPrediction && columnNameAndType.length != 1) {
            throw new RuntimeException("Cineast Config Error: Ensure that the table: " + this.tableName + " contains only one column, when multipleValuesPerPrediction is true.");
        }
    }

    private AttributeDefinition[] getColumnNameAndType(HashMap<String, String> columnNameAndTypeString) {
        int numOfColumns = columnNameAndTypeString.size();
        AttributeDefinition[] columnNameAndType = new AttributeDefinition[numOfColumns];
        int counter = 0;
        for (Map.Entry<String, String> entry : columnNameAndTypeString.entrySet()) {
            String attributeTypeUpperCase = entry.getValue().toUpperCase();
            columnNameAndType[counter] = new AttributeDefinition(entry.getKey(), AttributeDefinition.AttributeType.valueOf(attributeTypeUpperCase));
            counter++;
        }

        return columnNameAndType;

    }

    @Override
    public void processSegment(SegmentContainer shot) {
        if (shot.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME) {
            logError("Given Segment is empty.");
            return;
        }
        String shotId = shot.getId();
        int shotStart = shot.getStart();
        int shotEnd = shot.getEnd();

        if (!videoSupport && shotEnd != 0) {
            System.out.println("The feature : " + tableName + " only supports images. Skipping extraction for : " + shotId);
            return;
        }
        if (!phandler.idExists(shotId)) {

            String shotSuperId = shot.getSuperId();
            JSONObject predictedJsonAestheticFeatures = getPredictedAestheticFeatures(shotSuperId, shotStart, shotEnd);
            if (predictedJsonAestheticFeatures == null) {
                System.exit(1);
                return;
            }

            if (multipleValuesPerPrediction) {
                Object[] predictedAestheticFeatures = getPredictedFeatureWithMultipleValuessFromJson(predictedJsonAestheticFeatures);
                if (predictedAestheticFeatures == null) {
                    System.exit(1);
                    return;
                }
                persistMultipleValuesForOneColumn(shotId, predictedAestheticFeatures);
            } else {
                Object[] predictedAestheticFeatures = getPredictedFeaturesFromJson(predictedJsonAestheticFeatures);
                if (predictedAestheticFeatures == null) {
                    System.exit(1);
                    return;
                }
                persistMultipleValues(shotId, predictedAestheticFeatures);
            }
        }
    }

    private JSONObject getPredictedAestheticFeatures(String objectID, int shotStart, int shotEnd) {
        JSONObject serverResponse;

        try {

            serverResponse = RemotePredictorCommunication.getInstance().getJsonResponseFromAestheticFeaturePredictor(objectID, apiAddress, shotStart, shotEnd);
            if (serverResponse == null) {
                logError("Remote predictor response is null for shot: " + objectID + ". Aborting Extraction");
               // System.out.println("Server Response is null. Aborting Extraction");
                return null;
            }

            return serverResponse;
        } catch (Exception e) {

            logError("Exception occurred during prediction for shot: " + objectID + ". Aborting Extraction");
            //System.out.println("Exception occurred for " + tableName + " with shot: " + objectID);
            e.printStackTrace();
            return null;
        }
    }

    private Object[] getPredictedFeaturesFromJson(JSONObject serverJsonResponse) {
        int numOfFeatures = columnNameAndType.length;

        try {
            Object[] predictedFeatures = new Object[numOfFeatures];
            for (int i = 0; i < numOfFeatures; i++) {
                AttributeDefinition currentColNameAndType = columnNameAndType[i];
                predictedFeatures[i] = serverJsonResponse.get(currentColNameAndType.getName());
            }

            return predictedFeatures;
        } catch (Exception e) {
            logError("Exception occurred during parsing of json response. Aborting Extraction");
            e.printStackTrace();
            return null;
        }

    }

    private Object[] getPredictedFeatureWithMultipleValuessFromJson(JSONObject serverJsonResponse) {

        try {
            AttributeDefinition currentColNameAndType = columnNameAndType[0];
            Object currentResponse = serverJsonResponse.get(currentColNameAndType.getName());
            Object[] predictedValueObjects;
            if (currentResponse instanceof JSONArray) {
                JSONArray predictedValues = (JSONArray) currentResponse;
                int numOfValues = predictedValues.length();
                predictedValueObjects = new Object[numOfValues];
                for (int i = 0; i < numOfValues; i++)
                    predictedValueObjects[i] = predictedValues.get(i);
            } else {
                predictedValueObjects = new Object[]{currentResponse};
            }

            return predictedValueObjects;

        } catch (Exception e) {
            logError("Exception occurred during parsing of json response. Aborting Extraction");
            e.printStackTrace();
            return null;
        }

    }

    private void persistMultipleValues(String shotId, Object... valuesToPersist) {

        int numOfValuesToPersist = valuesToPersist.length;
        Object[] objectsToPersist = new Object[numOfValuesToPersist + 1];
        objectsToPersist[0] = shotId;
        for (int i = 0; i < numOfValuesToPersist; i++)
            objectsToPersist[i + 1] = PrimitiveTypeProvider.fromObject(valuesToPersist[i]);

        persist(objectsToPersist);
    }

    private void persistMultipleValuesForOneColumn(String shotId, Object... valuesToPersist) {
        for (Object o : valuesToPersist)
            persist(shotId, PrimitiveTypeProvider.fromObject(o));
    }


    protected void persist(String shotId, PrimitiveTypeProvider fv) {
        SimplePrimitiveTypeProviderFeatureDescriptor descriptor = new SimplePrimitiveTypeProviderFeatureDescriptor(shotId, fv);

        this.primitiveWriter.write(descriptor);
    }

    protected void persist(Object[] objectsToPersist) {

        PersistentTuple tuple = this.phandler.generateTuple(objectsToPersist);
        this.phandler.persist(tuple);
    }

    @Override
    public void init(PersistencyWriterSupplier phandlerSupply, int batchSize) {
        this.phandler = phandlerSupply.get();
        this.phandler.open(this.tableName);
        this.phandler.setFieldNames(getColumnNames());
        this.primitiveWriter = new PrimitiveTypeProviderFeatureDescriptorWriter(this.phandler, this.tableName, batchSize);
    }

    private String[] getColumnNames() {
        int numOfColumnsWithoutId = columnNameAndType.length;
        String[] columnNames = new String[numOfColumnsWithoutId + 1];
        columnNames[0] = "id";
        for (int i = 0; i < numOfColumnsWithoutId; i++) {
            columnNames[i + 1] = columnNameAndType[i].getName();
        }
        return columnNames;
    }

    @Override
    public void finish() {
        if (this.primitiveWriter != null) {
            this.primitiveWriter.close();
            this.primitiveWriter = null;
        }

        if (this.phandler != null) {
            this.phandler.close();
            this.phandler = null;
        }
    }

    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
        int numOfColumns = columnNameAndType.length;
        if (numOfColumns <= 0) {
            System.out.println("Number of columns and types is 0. Cannot create a table for : " + tableName);
            return;
        }
        supply.get().createIdEntity(tableName, columnNameAndType);
    }

    @Override
    public void dropPersistentLayer(Supplier<EntityCreator> supply) {
        supply.get().dropEntity(this.tableName);
    }

    private void logError(String message)
    {
        LOGGER.error("EXTRACTION-ERROR for table: " + tableName);
        LOGGER.error(message);
    }
}
