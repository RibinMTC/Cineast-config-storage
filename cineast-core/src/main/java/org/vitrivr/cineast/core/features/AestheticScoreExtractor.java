package org.vitrivr.cineast.core.features;

import org.json.JSONObject;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.ml_communication.MLPredictorCommunication;
import org.vitrivr.cineast.core.ml_extractor_base.AbstractMLExtractor;


public class AestheticScoreExtractor extends AbstractMLExtractor {

    private final String serverResponseAestheticScoreKey = "aesthetic score";

    private final String featureToExtract = "aesthetic_score";

    public AestheticScoreExtractor() {
        super("features_AestheticScore", AttributeDefinition.AttributeType.FLOAT);

    }

    @Override
    public void processSegment(SegmentContainer shot) {

        if (shot.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME)
            return;

        //Todo: Don't process if the shot is a video, since aesthetic score only works on images

        if (!phandler.idExists(shot.getId())) {
            float serverScore = getPredictedAestheticScore(shot.getSuperId());
            if (serverScore == -1)
                return;

            String shotId = shot.getId();

            persist(shotId, PrimitiveTypeProvider.fromObject(serverScore));
            System.out.println("Aesthetic Score for shotId: " + shotId + " score: " + serverScore);
        }

    }

    private float getPredictedAestheticScore(String objectID) {
        JSONObject serverResponse = MLPredictorCommunication.getInstance().getJsonResponseFromMLPredictor(objectID, featureToExtract);
        if (serverResponse == null) {
            System.out.println("Server Response is null. Aborting Extraction");
            return -1;
        }
        return (float) serverResponse.getDouble(serverResponseAestheticScoreKey);
    }
}
