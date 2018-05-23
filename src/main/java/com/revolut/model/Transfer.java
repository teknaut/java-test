package com.revolut.model;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class Transfer {

    @NotNull
    private Long accountFrom;

    @NotNull
    private Long accountTo;

    @NotNull
    private Long balanceToTransfer;
}
