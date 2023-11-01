package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

public interface TransferDao {

    Account retrieveAccountBalance(String username);

}
