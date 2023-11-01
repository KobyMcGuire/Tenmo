package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

import java.util.List;

public interface TransferDao {

    Account retrieveAccountBalance(String username);

    boolean validateSendTransfer(Transfer transfer);

//    Transfer createTransfer(Transfer transfer);

    void updateAccountBalances(Transfer transfer);

    List<User> retrieveListOfUsers();



}
