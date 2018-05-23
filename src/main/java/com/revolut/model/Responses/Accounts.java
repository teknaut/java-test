package com.revolut.model.Responses;

import java.util.List;

public class Accounts {
    private List<Long> accounts;

    public Accounts(List<Long> accounts) {
        this.accounts = accounts;
    }

    public List<Long> getAccounts() {
        return accounts;
    }

}
