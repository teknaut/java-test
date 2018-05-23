package com.revolut.model;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class Account {

    @NotNull
    private Long accountId;

    /**
     * The balance is represented in the smallest currency unit
     * for example pence in the UK
     */
    @NotNull
    private Long balance;
}
