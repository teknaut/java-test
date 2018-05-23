package com.revolut.services;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.revolut.model.Account;
import com.revolut.model.Responses.BasicResponse;
import com.revolut.model.Transfer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccountService {

    private final Map<Long, Account> accounts = new ConcurrentHashMap<>();

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    /**
     * add new account with a balance
     * @param files
     * @return
     */
    public BasicResponse createAccount(Map<String, String> files) {
        BasicResponse response = new BasicResponse();
        try {
            Account account = gson.fromJson(files.get("postData"), Account.class);
            if(null != accounts.get(account.getAccountId())) throw new IllegalStateException();
            accounts.put(account.getAccountId(), account);
            response.setResult("success");
            response.setResponseCode(200);
        }
        catch(IllegalStateException iex){
            String error = "creation failed an account with this id already exists";
            log.error(error);
            response.setResult(error);
            response.setResponseCode(400);
        }
        catch(Exception ex){
            String error = "cannot create account";
            log.error(error);
            response.setResult(error);
            response.setResponseCode(400);
        }
        return response;
    }

    /**
     * list created accounts
     * @return
     */
    public List<Long> listAccounts() {
        return accounts.keySet()
                .stream()
                .collect(Collectors.toList());
    }

    /**
     * Get balance for given account
     * @param accountid
     * @return
     */
    public BasicResponse getBalance(Long accountid) {
        BasicResponse response = new BasicResponse();
        if (null == accounts.get(accountid)) {
            response.setResult("N0 account for this id");
            response.setResponseCode(400);
        }
        response.setResult(String.valueOf(accounts.get(accountid).getBalance()));
        response.setResponseCode(200);
        return response;
    }


    /**
     *  transfer from one account to another
     * @return
     */
    public BasicResponse doTransfer(Map<String, String> files) {
        BasicResponse response = new BasicResponse();
        Transfer transfer = gson.fromJson(files.get("postData"), Transfer.class);
        Account accountTo = accounts.get(transfer.getAccountTo());
        Account accountFrom = accounts.get(transfer.getAccountFrom());
        if (validateTransfer(accountFrom, transfer.getBalanceToTransfer())) {
            accountTo.setBalance(accountTo.getBalance() + transfer.getBalanceToTransfer());
            accountFrom.setBalance(accountFrom.getBalance() - transfer.getBalanceToTransfer());
            response.setResult("success");
        } else {
            String error = "insufficient funds";
            log.error(error + "in transfer from " + accountFrom + " to " + accountTo);
            response.setResult(error);
            response.setResponseCode(400);
        }
        return response;
    }

    /**
     * check the account we are transferring from can cover the balance of
     * the transfer and that we are passed a positive value for the transfer
     *
     * @param accountFrom
     * @param balanceToTransfer
     * @return
     */
    public Boolean validateTransfer(Account accountFrom, Long balanceToTransfer) {
        return (balanceToTransfer > 0 && accountFrom.getBalance() - balanceToTransfer > 0);
    }

}
