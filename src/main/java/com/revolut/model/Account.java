package com.revolut.model;

import javax.validation.constraints.NotNull;

public class Account {

    @NotNull
    private Long accountId;

    /**
     * The balance is represented in the smallest currency unit
     * for example pence in the UK
     */
    @NotNull
    private Long balance;


    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }
}
