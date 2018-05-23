package com.revolut.model;

import javax.validation.constraints.NotNull;

public class Transfer {

    @NotNull
    private Long accountFrom;

    @NotNull
    private Long accountTo;

    @NotNull
    private Long balanceToTransfer;

    public Long getAccountFrom() {
        return accountFrom;
    }

    public void setAccountFrom(Long accountFrom) {
        this.accountFrom = accountFrom;
    }

    public Long getAccountTo() {
        return accountTo;
    }

    public void setAccountTo(Long accountTo) {
        this.accountTo = accountTo;
    }

    public Long getBalanceToTransfer() {
        return balanceToTransfer;
    }

    public void setBalanceToTransfer(Long balanceToTransfer) {
        this.balanceToTransfer = balanceToTransfer;
    }
}
