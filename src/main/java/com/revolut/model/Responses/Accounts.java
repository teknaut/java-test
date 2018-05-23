package com.revolut.model.Responses;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Accounts {
    private List<Long> accounts;
 }
