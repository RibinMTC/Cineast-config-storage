package org.vitrivr.cineast.core.ml_communication;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

import java.util.HashMap;

import static org.jcodec.common.Assert.assertEquals;
import static org.jcodec.common.Assert.assertNotNull;

public enum MLPredictorCommunication {

    INSTANCE;

    private HashMap<String, String> objectIDToUrlMap;
    private HashMap<String, JSONObject> objectIDToServerResponseMap;
    private String baseContentUrlPath;

    MLPredictorCommunication() {
        objectIDToUrlMap = new HashMap<>();
        objectIDToServerResponseMap = new HashMap<>();
    }

    public static MLPredictorCommunication getInstance() {
        return INSTANCE;
    }

   /* public void setBaseUrlPath(String baseContentUrlPath)
    {
        this.baseContentUrlPath = baseContentUrlPath;
    }*/

    public void setObjectIDAbsolutePath(String objectID, String contentAbsolutePath)
    {
        objectIDToUrlMap.put(objectID, contentAbsolutePath);
    }

    public JSONObject getJsonResponseFromMLPredictor(String objectID, String featureToExtract) {

        if(objectIDToServerResponseMap.containsKey(objectID))
            return objectIDToServerResponseMap.get(objectID);


        if(!objectIDToUrlMap.containsKey(objectID))
        {
            System.out.println("Aborting extraction. ObjectID " + objectID + " has no stored absolute path");
            return null;
        }


        String contentPath = objectIDToUrlMap.get(objectID);//baseContentUrlPath + "/" + objectID;
        try {
            HttpResponse<JsonNode> response = Unirest.post("http://localhost:5000/predict/" + featureToExtract)
                    .header("Content-Type", "application/json")
                    .body("{\"contentPath\":\"" + contentPath + "\"}")
                    .asJson();

            JsonNode responseBody = response.getBody();
            assertNotNull(responseBody);
            assertEquals(200, response.getStatus());

            JSONObject serverResponseJson = responseBody.getObject();
            objectIDToServerResponseMap.put(objectID, serverResponseJson);

            return serverResponseJson;

        } catch (UnirestException e) {
            e.printStackTrace();
        }

        return null;
    }

}
