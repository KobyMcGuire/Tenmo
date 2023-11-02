package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
public class TenmoController {
    @Autowired
    private TransferDao dao;


    @RequestMapping(path = "accounts", method = RequestMethod.GET)
    public Account retrieveAccountBalance(Principal principal){
        Account account = dao.retrieveAccountBalance(principal.getName());
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account was not found for that Username");
        }
        return account;
    }

    @RequestMapping(path = "users", method = RequestMethod.GET)
    public List<User> retrieveListOfUsers() {
        List<User> users = dao.retrieveListOfUsers();

        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users were found");
        }

        return users;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path="/transfers", method = RequestMethod.POST)
    public Transfer createTransfer(@Valid @RequestBody Transfer transfer, Principal principal) {

        if (transfer.getRecipientId() == transfer.getSenderId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You tried to send money to yourself :(.");
        }

        // validate that the sender has enough money
        boolean canTransfer = dao.validateSendTransfer(transfer);

        // call dao to insert in to transfer table no matter what the transfer type is
        if (!canTransfer) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough money in the account to send.");
        }

        transfer = dao.createTransfer(transfer);

        // if the type is "send" update accounts' balances immediately
        if (transfer.getType().equalsIgnoreCase("Send")) {
            dao.updateAccountBalances(transfer);
        }


        return transfer;
    }

    @RequestMapping(path = "transfers", method = RequestMethod.GET)
    public List<Transfer> retrieveListOfTransfers(@RequestParam int userId){
         List<Transfer> transfers =  dao.retrieveListOfTransfers(userId);
         if (transfers.isEmpty()) {
             throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to get list of transfers.");
         }
         return transfers;
    }

    @RequestMapping(path = "transfers/{id}", method = RequestMethod.GET)
    public Transfer retrieveTransferById(@PathVariable("id") int transferId){
        Transfer transfer = null;
        transfer = dao.retrieveTransferById(transferId);
        if (transfer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to locate specific transfer.");
        }
        return transfer;
    }

}
