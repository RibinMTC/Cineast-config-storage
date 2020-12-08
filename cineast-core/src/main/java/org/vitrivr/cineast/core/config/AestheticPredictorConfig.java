package org.vitrivr.cineast.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class AestheticPredictorConfig {

    private String tableName;
    private String apiAddress;
    private Boolean videoSupport;
    private HashMap<String, String> columnNameAndType;

    @JsonCreator
    public AestheticPredictorConfig(){}

    @JsonProperty
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @JsonProperty
    public String getApiAddress() {
        return apiAddress;
    }

    public void setApiAddress(String apiAddress) {
        this.apiAddress = apiAddress;
    }

    @JsonProperty
    public Boolean getVideoSupport() {
        return videoSupport;
    }

    public void setVideoSupport(Boolean videoSupport) {
        this.videoSupport = videoSupport;
    }

    @JsonProperty
    public HashMap<String, String> getColumnNameAndType() {
        return columnNameAndType;
    }

    public void setColumnNameAndType(HashMap<String, String> columnNameAndType) {
        this.columnNameAndType = columnNameAndType;
    }
}
