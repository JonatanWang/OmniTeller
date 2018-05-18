package com.example.martin.omniteller.Models;

/**
 * Created by Martin on 2017-12-17.
 */

public class Recognition {


    private String id;
    private String label;
    private float confidence;

    public Recognition(String id, String label, float confidence) {
        this.id = id;
        this.label = label;
        this.confidence = confidence;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }


}
