package org.vitrivr.cineast.core.features;


import java.util.Arrays;

public class HecateImageMetricsRangeBooleanRetriever extends RangeBooleanRetriever {
    public HecateImageMetricsRangeBooleanRetriever() {
        super("features_hecateimagemetrics", Arrays.asList("asymmetry", "sharpness","brightness"));
    }
}
