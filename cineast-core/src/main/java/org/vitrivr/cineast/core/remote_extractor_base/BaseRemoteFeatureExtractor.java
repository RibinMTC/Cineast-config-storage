package org.vitrivr.cineast.core.remote_extractor_base;

import org.vitrivr.cineast.core.data.entities.SimplePrimitiveTypeProviderFeatureDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.db.dao.writer.PrimitiveTypeProviderFeatureDescriptorWriter;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.extractor.Extractor;

import java.util.function.Supplier;


public abstract class BaseRemoteFeatureExtractor implements Extractor {

    protected final String featureToPredict;
    protected final String tableName;
    private final AttributeDefinition[] columnNameAndType;

    protected PrimitiveTypeProviderFeatureDescriptorWriter primitiveWriter;
    protected PersistencyWriter<?> phandler;

    public BaseRemoteFeatureExtractor(String tableName, String featureToPredict, AttributeDefinition[] columnNameAndType) {
        this.tableName = tableName;
        this.featureToPredict = featureToPredict;
        this.columnNameAndType = columnNameAndType;
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
        if (numOfColumns == 1 && columnNameAndType[0].getName().equals("feature"))
            supply.get().createFeatureEntity(tableName, true, columnNameAndType);
        else if (numOfColumns > 1)
            supply.get().createIdEntity(tableName, columnNameAndType);


    }

    @Override
    public void dropPersistentLayer(Supplier<EntityCreator> supply) {
        supply.get().dropEntity(this.tableName);
    }
}
