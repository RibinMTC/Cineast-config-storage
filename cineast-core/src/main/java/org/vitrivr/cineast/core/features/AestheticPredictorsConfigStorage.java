package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.config.AestheticPredictorConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * This Singleton class is created to hold and pass the aesthetic predictors config data to the MainAestheticFeaturesPredictorInitializer and
 * CombinedAestheticMetricsRetriever, since the Config class cannot be accessed from the cineast-core package.
 */
public enum AestheticPredictorsConfigStorage {

    INSTANCE;

    private List<AestheticPredictorConfig> activeAestheticPredictorsConfig;
    private List<AestheticPredictorConfig> allAestheticPredictorsConfig;

    public static AestheticPredictorsConfigStorage getInstance() {
        return INSTANCE;
    }

    public void setAestheticPredictorsConfig(List<AestheticPredictorConfig> aestheticPredictorConfigs, List<Integer> activePredictorsConfig)
    {
        this.activeAestheticPredictorsConfig = new ArrayList<>();
        for (AestheticPredictorConfig config : aestheticPredictorConfigs)
        {
            if(activePredictorsConfig.contains(config.getPredictorId()))
                activeAestheticPredictorsConfig.add(config);
        }
        this.allAestheticPredictorsConfig = aestheticPredictorConfigs;
    }

    public List<AestheticPredictorConfig> getActiveAestheticPredictorsConfig() {
        return activeAestheticPredictorsConfig;
    }

    public List<AestheticPredictorConfig> getAllAestheticPredictorsConfig() {
        return allAestheticPredictorsConfig;
    }
}
