package org.vitrivr.cineast.core.features;

import org.json.JSONObject;
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

import java.util.function.Supplier;

public class ConfigurableAestheticFeatureExtractor implements Extractor {

    protected final String apiAddress;
    protected final String tableName;
    private final AttributeDefinition[] columnNameAndType;
    private final boolean videoSupport;

    protected PrimitiveTypeProviderFeatureDescriptorWriter primitiveWriter;
    protected PersistencyWriter<?> phandler;

    public ConfigurableAestheticFeatureExtractor(String tableName, AttributeDefinition[] columnNameAndType, String apiAddress, boolean videoSupport) {
        this.apiAddress = apiAddress;
        this.tableName = tableName;
        this.columnNameAndType = columnNameAndType;
        this.videoSupport = videoSupport;
    }

    @Override
    public void processSegment(SegmentContainer shot) {
        if (shot.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME)
            return;
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
                return;
            }

            Object[] predictedAestheticFeatures = getPredictedFeaturesFromJson(predictedJsonAestheticFeatures);
            if (predictedAestheticFeatures == null)
                return;
            persistMultipleValues(shotId, predictedAestheticFeatures);
        }
    }

    private JSONObject getPredictedAestheticFeatures(String objectID, int shotStart, int shotEnd) {
        JSONObject serverResponse;

        try {

            serverResponse = RemotePredictorCommunication.getInstance().getJsonResponseFromAestheticFeaturePredictor(objectID, apiAddress, shotStart, shotEnd);
            if (serverResponse == null) {
                System.out.println("Server Response is null. Aborting Extraction");
                return null;
            }

            return serverResponse;
        } catch (Exception e) {

            System.out.println("Exception occurred for " + tableName + " with shot: " + objectID);
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
}
