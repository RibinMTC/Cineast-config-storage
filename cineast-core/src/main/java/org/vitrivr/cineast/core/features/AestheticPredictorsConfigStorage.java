package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.config.AestheticPredictorConfig;

import java.util.List;

/**
 * This Singleton class is created to hold and pass the aesthetic predictors config data to the MainAestheticFeaturesPredictorInitializer and
 * CombinedAestheticMetricsRetriever, since the Config class cannot be accessed from the cineast-core package.
 */
public enum AestheticPredictorsConfigStorage {

    INSTANCE;

    private List<AestheticPredictorConfig> aestheticPredictorsConfig;

    public static AestheticPredictorsConfigStorage getInstance() {
        return INSTANCE;
    }

    public void setAestheticPredictorsConfig(List<AestheticPredictorConfig> aestheticPredictorConfigs)
    {
        this.aestheticPredictorsConfig = aestheticPredictorConfigs;
    }

    public List<AestheticPredictorConfig> getAestheticPredictorsConfig() {
        return aestheticPredictorsConfig;
    }
}
