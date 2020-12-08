package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.config.AestheticPredictorConfig;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.extractor.Extractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Class is responsible for initializing all aesthetic predictors from the cineast.json file.
 * Class is an extractor, which simply calls all initialized extractor methods
 */
public class MainAestheticFeaturesPredictorInitializer implements Extractor {

    private final List<ConfigurableAestheticFeatureExtractor> activeExtractors;

    public MainAestheticFeaturesPredictorInitializer() {

        List<AestheticPredictorConfig> aestheticPredictorConfigs = AestheticPredictorsConfigStorage.getInstance().getAestheticPredictorsConfig();
        activeExtractors = new ArrayList<>();
        for (AestheticPredictorConfig aestheticPredictorConfig :
                aestheticPredictorConfigs) {
            AttributeDefinition[] columnNameAndType = getColumnNameAndType(aestheticPredictorConfig.getColumnNameAndType());
            ConfigurableAestheticFeatureExtractor extractor = new ConfigurableAestheticFeatureExtractor(aestheticPredictorConfig.getTableName(),
                    columnNameAndType, aestheticPredictorConfig.getApiAddress(), aestheticPredictorConfig.getVideoSupport());
            activeExtractors.add(extractor);
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
    public void init(PersistencyWriterSupplier phandlerSupply, int batchSize) {
        for (ConfigurableAestheticFeatureExtractor extractor :
                activeExtractors) {
            extractor.init(phandlerSupply, batchSize);
        }
    }

    @Override
    public void processSegment(SegmentContainer shot) {
        for (ConfigurableAestheticFeatureExtractor extractor :
                activeExtractors) {
            extractor.processSegment(shot);
        }
    }

    @Override
    public void finish() {
        for (ConfigurableAestheticFeatureExtractor extractor :
                activeExtractors) {
            extractor.finish();
        }
    }

    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
        for (ConfigurableAestheticFeatureExtractor extractor :
                activeExtractors) {
            extractor.initalizePersistentLayer(supply);
        }
    }

    @Override
    public void dropPersistentLayer(Supplier<EntityCreator> supply) {
        for (ConfigurableAestheticFeatureExtractor extractor :
                activeExtractors) {
            extractor.dropPersistentLayer(supply);
        }
    }
}
