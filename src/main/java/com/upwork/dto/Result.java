package com.upwork.dto;

import java.io.Serializable;

/**
 * Created by vbabin on 11.11.2016.
 */
public class Result implements Serializable {
    private double result;

    public Result(double result) {
        this.result = result;
    }

    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }
}
