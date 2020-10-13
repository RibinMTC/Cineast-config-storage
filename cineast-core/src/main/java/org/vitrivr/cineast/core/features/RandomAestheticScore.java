package org.vitrivr.cineast.core.features;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import java.util.*;
import java.util.function.Supplier;

public class RandomAestheticScore implements Extractor {

    private static final Logger LOGGER = LogManager.getLogger();
    protected PrimitiveTypeProviderFeatureDescriptorWriter primitiveWriter;
    protected PersistencyWriter<?> phandler;

    private final Random random;
    private final float minScore = 3f;
    private final float maxScore = 7f;

    protected final String tableName = "features_RandomAestheticScore";

    private final String columnName = "feature";
    private final AttributeDefinition.AttributeType attributeType = AttributeDefinition.AttributeType.FLOAT;

    public RandomAestheticScore() {
        random = new Random();
    }

    protected void persist(String shotId, PrimitiveTypeProvider fv) {
        SimplePrimitiveTypeProviderFeatureDescriptor descriptor = new SimplePrimitiveTypeProviderFeatureDescriptor(shotId, fv);

        this.primitiveWriter.write(descriptor);
    }

    private float getRandomAestheticScore() {
        return random.nextFloat() * (maxScore - minScore) + minScore;
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

        if (!phandler.idExists(shot.getId())) {

            float serverScore = getRandomAestheticScore();//MLPredictiorCommunication.getJsonResponseFromMLPredictor("/home/cribin/AestheticProject/resources/66.jpg");

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
