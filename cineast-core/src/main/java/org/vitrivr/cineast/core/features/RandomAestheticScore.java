package org.vitrivr.cineast.core.features;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.entities.SimplePrimitiveTypeProviderFeatureDescriptor;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.dao.writer.PrimitiveTypeProviderFeatureDescriptorWriter;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.features.retriever.Retriever;

import java.util.*;
import java.util.function.Supplier;

public class RandomAestheticScore implements Extractor, Retriever {

    private static final Logger LOGGER = LogManager.getLogger();
    protected PrimitiveTypeProviderFeatureDescriptorWriter primitiveWriter;
    protected PersistencyWriter<?> phandler;

    private final Random random;
    private final float minScore = 3f;
    private final float maxScore = 7f;

    protected final String tableName = "features_RandomAestheticScore";

    private final String columnName = "feature";
    private final AttributeDefinition.AttributeType attributeType = AttributeDefinition.AttributeType.STRING;

    protected DBSelector selector = null;

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

            // float serverScore = getRandomAestheticScore();
            //  float serverScore2 = getRandomAestheticScore();
            String emotion1 = "surprise, disgust";
            String emotion2 = "fear";
            String emotion3 = "disgusted, surprised";
            String shotId = shot.getId();

            persist(shotId, PrimitiveTypeProvider.fromObject(emotion1));
            persist(shotId, PrimitiveTypeProvider.fromObject(emotion2));
            persist(shotId, PrimitiveTypeProvider.fromObject(emotion3));
            System.out.println("Aesthetic Score for shotId: " + shotId + " emotion: " + emotion1 + " writing with new Extractor");
            System.out.println("Aesthetic Score for shotId: " + shotId + " emotion: " + emotion2 + " writing with new Extractor");
        }

    }

    @Override
    public void init(DBSelectorSupplier selectorSupply) {
        this.selector = selectorSupply.get();
        this.selector.open(this.tableName);
    }

    @Override
    public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
        return null;
    }

    @Override
    public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) {
        return null;
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
