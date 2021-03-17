package org.vitrivr.cineast.core.remote_predictor_communication;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import org.vitrivr.cineast.core.config.AestheticPredictorConfig;

import java.util.HashMap;
import java.util.List;

import static org.jcodec.common.Assert.assertEquals;
import static org.jcodec.common.Assert.assertNotNull;

public enum RemotePredictorCommunication {

    INSTANCE;

    private HashMap<String, String> objectIDToUrlMap;

    RemotePredictorCommunication() {
        objectIDToUrlMap = new HashMap<>();
    }

    public static RemotePredictorCommunication getInstance() {
        return INSTANCE;
    }

    public void setObjectIDAbsolutePath(String objectID, String contentAbsolutePath) {
        objectIDToUrlMap.put(objectID, contentAbsolutePath);
    }


   /* public JSONObject getJsonResponseFromMLPredictor(String objectID, String featureToPredict, int shotStart, int shotEnd) {

        if (remotePredictorsConfig == null) {
            System.out.println("MlPredictorsConfig not set in cineast.json. Cannot extract to ml predictors.");
            return null;
        }

        if (!remotePredictorsConfig.containsKey(featureToPredict)) {
            System.out.println("MlPredictorsConfig does not contain key: " + featureToPredict + "Cannot extract to ml predictors");
            return null;
        }

        if (!objectIDToUrlMap.containsKey(objectID)) {
            System.out.println("Aborting extraction. ObjectID " + objectID + " has no stored absolute path");
            return null;
        }


        String contentPath = objectIDToUrlMap.get(objectID);
        try {
            if (shotEnd != 0)
                Unirest.setTimeouts(10000, 120000);
            else
                Unirest.setTimeouts(10000, 60000);

            HttpResponse<JsonNode> response = Unirest.post(remotePredictorsConfig.get(featureToPredict))
                    .header("Content-Type", "application/json")
                    .body("{\"contentPath\":\"" + contentPath + "\"," +
                            "\"startFrame\":\"" + shotStart + "\"," +
                            "\"endFrame\":\"" + shotEnd + "\"}")
                    .asJson();

            JsonNode responseBody = response.getBody();
            assertNotNull(responseBody);
            assertEquals(200, response.getStatus());

            JSONObject serverResponseJson = responseBody.getObject();
            objectIDToServerResponseMap.put(objectID, serverResponseJson);

            return serverResponseJson;

        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }


    }*/

    public JSONObject getJsonResponseFromAestheticFeaturePredictor(String objectID, String apiAddress, int shotStart, int shotEnd) {

        if (!objectIDToUrlMap.containsKey(objectID)) {
            System.out.println("Aborting extraction. ObjectID " + objectID + " has no stored absolute path");
            return null;
        }

        String contentPath = objectIDToUrlMap.get(objectID);
        try {
            if (shotEnd != 0)
                Unirest.setTimeouts(10000, 0);
            else
                Unirest.setTimeouts(10000, 0);

            HttpResponse<JsonNode> response = Unirest.post(apiAddress)
                    .header("Content-Type", "application/json")
                    .body("{\"contentPath\":\"" + contentPath + "\"," +
                            "\"startFrame\":\"" + shotStart + "\"," +
                            "\"endFrame\":\"" + shotEnd + "\"}")
                    .asJson();

            JsonNode responseBody = response.getBody();
            assertNotNull(responseBody);
            assertEquals(200, response.getStatus());

            return responseBody.getObject();

        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }

    }

}
