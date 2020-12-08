package org.vitrivr.cineast.core.features;

import org.json.JSONObject;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;;
import org.vitrivr.cineast.core.remote_extractor_base.BaseRemoteFeatureExtractor;
import org.vitrivr.cineast.core.remote_predictor_communication.RemotePredictorCommunication;

/**
 * Old way of creating aesthetic extractors. Now the functionality is moved to the ConfigurableAestheticFeatureExtractor
 */

/*
public class FacialEmotionExtractor extends BaseRemoteFeatureExtractor {

    public FacialEmotionExtractor() {
        super("features_facialEmotion", "facial emotion", new AttributeDefinition[]{new AttributeDefinition("feature", AttributeDefinition.AttributeType.STRING)});
    }

    @Override
    public void processSegment(SegmentContainer shot) {
        if (shot.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME)
            return;

        if(shot.getEnd() != 0) {
            System.out.println("Facial Emotion does not currently support videos. Aborting extraction");
            return;
        }

        if (!phandler.idExists(shot.getId())) {
            String facialEmotion = getPredictedFacialEmotion(shot.getSuperId());
            if (facialEmotion == null)
                return;

            String shotId = shot.getId();

            persist(shotId, PrimitiveTypeProvider.fromObject(facialEmotion));
            System.out.println("Facial emotion for shotId: " + shotId + " emotion: " + facialEmotion);
        }

    }

    private String getPredictedFacialEmotion(String objectID) {
        JSONObject serverResponse = null;
        try {
            serverResponse = RemotePredictorCommunication.getInstance().getJsonResponseFromMLPredictor(objectID, featureToPredict, 0, 0);
            if (serverResponse == null) {
                System.out.println("Server Response is null. No " + featureToPredict + "extracted for shot:" + objectID);
                return null;
            }
            return serverResponse.getString(featureToPredict);
        }
        catch(Exception exception)
        {
            if(serverResponse != null)
                System.out.println("Exception occurred for facial emotion detection for shot: " + objectID + ". Received server response: " + serverResponse);
            return null;
        }
    }
}
*/