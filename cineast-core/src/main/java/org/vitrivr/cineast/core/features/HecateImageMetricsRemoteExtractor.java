package org.vitrivr.cineast.core.features;

import org.json.JSONObject;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.hecate_image_metrics.HecateImageMetric;
import org.vitrivr.cineast.core.remote_extractor_base.BaseRemoteFeatureExtractor;
import org.vitrivr.cineast.core.remote_predictor_communication.RemotePredictorCommunication;

import java.util.Arrays;


public class HecateImageMetricsRemoteExtractor extends BaseRemoteFeatureExtractor {

    private final String[] imageMetricNames;
    private final int numOfMetrics;

    public HecateImageMetricsRemoteExtractor() {
        //Todo: Remove passing huge inline argument to the constructor
        super("features_HecateImageMetrics", "hecate image metrics", Arrays.stream(getNames(HecateImageMetric.class)).map(imageMetricName -> new AttributeDefinition(imageMetricName, AttributeDefinition.AttributeType.FLOAT)).toArray(AttributeDefinition[]::new));
        imageMetricNames = getNames(HecateImageMetric.class);
        numOfMetrics = imageMetricNames.length;
    }

    public static String[] getNames(Class<? extends Enum<?>> e) {
        return Arrays.toString(e.getEnumConstants()).replaceAll("^.|.$", "").split(", ");
    }

    private AttributeDefinition[] getAttributeDefinitions() {
        String[] imageMetricNames = getNames(HecateImageMetric.class);
        return Arrays.stream(imageMetricNames).map(imageMetricName -> new AttributeDefinition(imageMetricName, AttributeDefinition.AttributeType.FLOAT)).toArray(AttributeDefinition[]::new);
    }

    @Override
    public void processSegment(SegmentContainer shot) {

        if (shot.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME)
            return;

        int shotStart = shot.getStart();
        int shotEnd = shot.getEnd();
        if (!phandler.idExists(shot.getId())) {

            String shotId = shot.getId();
            float[] imageMetrics = getPredictedImageMetrics(shot.getSuperId(), shotStart, shotEnd);
            if(imageMetrics == null)
            {
                System.out.println("Predicted Hecate Image Metric for shotId: " + shotId + "was null. Skipping extraction for this shot.");
                return;
            }

            persistMultipleValues(shotId, imageMetrics);
        }
    }

    private float[] getPredictedImageMetrics(String objectID, int shotStart, int shotEnd) {
        JSONObject serverResponse = null;
        float[] predictedImageMetrics = new float[numOfMetrics];
        try {

            serverResponse = RemotePredictorCommunication.getInstance().getJsonResponseFromMLPredictor(objectID, featureToPredict, shotStart, shotEnd);
            if (serverResponse == null) {
                System.out.println("Server Response is null. Aborting Extraction");
                return null;
            }

            for(int i=0; i < numOfMetrics; i++)
            {
                predictedImageMetrics[i] = (float) serverResponse.getDouble(imageMetricNames[i]);
            }
            return predictedImageMetrics;
        } catch (Exception e) {
            if (serverResponse != null)
                System.out.println("Exception occurred for aesthetic score detection shot: " + objectID + ". Received server response: " + serverResponse);
            return null;
        }
    }

    private float getNormalizedRandomNumber() {
        return (float) Math.random() * 10f;
    }

    private void persistMultipleValues(String shotId, float... valuesToPersist) {

        int numOfValuesToPersist = valuesToPersist.length;
        Object[] objectsToPersist = new Object[numOfValuesToPersist + 1];
        objectsToPersist[0] = shotId;
        for (int i = 0; i < numOfValuesToPersist; i++)
            objectsToPersist[i + 1] = PrimitiveTypeProvider.fromObject(valuesToPersist[i]);

        persist(objectsToPersist);
    }
}
