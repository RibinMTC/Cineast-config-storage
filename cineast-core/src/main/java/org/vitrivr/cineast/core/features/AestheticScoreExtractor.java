package org.vitrivr.cineast.core.features;

import com.mashape.unirest.http.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.vitrivr.cineast.core.data.entities.SimplePrimitiveTypeProviderFeatureDescriptor;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.dao.writer.PrimitiveTypeProviderFeatureDescriptorWriter;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.ml_communication.MLPredictorCommunication;

import java.util.function.Supplier;

public class AestheticScoreExtractor implements Extractor {

    private static final Logger LOGGER = LogManager.getLogger();
    protected PrimitiveTypeProviderFeatureDescriptorWriter primitiveWriter;
    protected PersistencyWriter<?> phandler;

    protected final String tableName = "features_AestheticScore";

    private final String columnName = "feature";
    private final AttributeDefinition.AttributeType attributeType = AttributeDefinition.AttributeType.FLOAT;

    private final String serverResponseAestheticScoreKey = "aesthetic score";

    public AestheticScoreExtractor() {

    }

    protected void persist(String shotId, PrimitiveTypeProvider fv) {
        SimplePrimitiveTypeProviderFeatureDescriptor descriptor = new SimplePrimitiveTypeProviderFeatureDescriptor(shotId, fv);

        this.primitiveWriter.write(descriptor);
    }

    private float getPredictedAestheticScore(String objectID) {
        JSONObject serverResponse = MLPredictorCommunication.getInstance().getJsonResponseFromMLPredictor(objectID);
        if (serverResponse == null) {
            System.out.println("Server Response is null. Aborting Extraction");
            return -1;
        }
        return (float) serverResponse.getDouble(serverResponseAestheticScoreKey);
    }

    @Override
    public void init(PersistencyWriterSupplier phandlerSupply, int batchSize) {
        this.phandler = phandlerSupply.get();
        this.primitiveWriter = new PrimitiveTypeProviderFeatureDescriptorWriter(this.phandler, this.tableName, batchSize);
    }

    @Override
    public void processSegment(SegmentContainer shot) {

        if (shot.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME)
            return;

        //Todo: Don't process if the shot is a video, since aesthetic score only works on images

        if (!phandler.idExists(shot.getId())) {
            float serverScore = getPredictedAestheticScore(shot.getSuperId());
            if (serverScore == -1)
                return;

            String shotId = shot.getId();

            persist(shotId, PrimitiveTypeProvider.fromObject(serverScore));
            System.out.println("Aesthetic Score for shotId: " + shotId + " score: " + serverScore + " writing with new Extractor");
        }

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
        supply.get().createFeatureEntity(this.tableName, true, new AttributeDefinition(columnName, attributeType));
    }

    @Override
    public void dropPersistentLayer(Supplier<EntityCreator> supply) {
        supply.get().dropEntity(this.tableName);
    }
}
