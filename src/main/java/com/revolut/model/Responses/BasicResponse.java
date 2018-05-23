package com.revolut.model.Responses;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class BasicResponse {

    @NotNull
    String result;

    @NotNull
    int responseCode;
}
