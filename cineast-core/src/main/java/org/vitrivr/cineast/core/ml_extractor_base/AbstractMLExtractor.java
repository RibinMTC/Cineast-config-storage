package org.vitrivr.cineast.core.ml_extractor_base;

import org.vitrivr.cineast.core.data.entities.SimplePrimitiveTypeProviderFeatureDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.dao.writer.PrimitiveTypeProviderFeatureDescriptorWriter;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.extractor.Extractor;

import java.util.function.Supplier;

public abstract class AbstractMLExtractor implements Extractor {

    protected final String tableName;
    private final String columnName = "feature";
    private final AttributeDefinition.AttributeType attributeType;

    protected PrimitiveTypeProviderFeatureDescriptorWriter primitiveWriter;
    protected PersistencyWriter<?> phandler;


    public AbstractMLExtractor(String tableName, AttributeDefinition.AttributeType attributeType) {
        this.tableName = tableName;
        this.attributeType = attributeType;
    }

    protected void persist(String shotId, PrimitiveTypeProvider fv) {
        SimplePrimitiveTypeProviderFeatureDescriptor descriptor = new SimplePrimitiveTypeProviderFeatureDescriptor(shotId, fv);

        this.primitiveWriter.write(descriptor);
    }


    @Override
    public void init(PersistencyWriterSupplier phandlerSupply, int batchSize) {
        this.phandler = phandlerSupply.get();
        this.primitiveWriter = new PrimitiveTypeProviderFeatureDescriptorWriter(this.phandler, this.tableName, batchSize);
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
