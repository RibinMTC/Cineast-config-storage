package org.vitrivr.cineast.core.hecate_image_metrics;

import java.util.HashMap;
import java.util.Map;

public enum HecateImageMetric {
    Brightness(0),
    Sharpness(1),
    Asymmetry(2);

    private final int value;
    private static final Map<Integer, HecateImageMetric> map = new HashMap<>();

    HecateImageMetric(int value) {
        this.value = value;
    }

    static {
        for (HecateImageMetric hecateImageMetric : HecateImageMetric.values()) {
            map.put(hecateImageMetric.value, hecateImageMetric);
        }
    }

    public static HecateImageMetric valueOf(int metricType) {
        return map.get(metricType);
    }

    public int getValue() {
        return value;
    }
}
