package com.upwork.dto;

import java.io.Serializable;

/**
 * Created by vbabin on 11.11.2016.
 */
public class Result implements Serializable {
    private float result;

    public Result(float result) {
        this.result = result;
    }

    public float getResult() {
        return result;
    }

    public void setResult(float result) {
        this.result = result;
    }
}
