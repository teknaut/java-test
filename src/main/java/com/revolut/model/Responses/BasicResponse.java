package com.revolut.model.Responses;

import javax.validation.constraints.NotNull;

public class BasicResponse {

    @NotNull
    String result;

    @NotNull
    int responseCode;


    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
}
