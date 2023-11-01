package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao{

    private final JdbcTemplate jdbcTemplate;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Account retrieveAccountBalance(String username) {
        Account account = null;
        String sql = "SELECT balance FROM account " +
                "JOIN tenmo_user ON account.user_id = tenmo_user.user_id " +
                "WHERE username ILIKE ?";
        try {
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, username);
            if (result.next()){
                account = mapRowToAccount(result);
            }
        } catch (Exception e){
            throw new DaoException("Unable to reach database or account was not found", e);
        }
        return account;
    }

    @Override
    public boolean validateSendTransfer(Transfer transfer) {
        BigDecimal balance = new BigDecimal("0");
        String sql = "SELECT balance FROM account WHERE user_id = ?";
        try {
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, transfer.getSenderId());
            if (result.next()) {
                balance = result.getBigDecimal("balance");
            }
            if (balance.compareTo(transfer.getAmount()) >= 0){
                return true;
            }
        } catch (Exception e) {
            throw new DaoException("Was not able to get account balance", e);
        }
        return false;
    }

    @Override
    public Transfer createTransfer(Transfer transfer) {

        String accountSQL = "SELECT account_id FROM account WHERE user_id = ?";
        String transferTypeSQL = "SELECT transfer_type_id FROM transfer_type WHERE transfer_type_desc ILIKE ?";
        String transferStatusSQL = "SELECT transfer_status_id FROM transfer_status WHERE transfer_status_desc ILIKE ?";
        String createSQL = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING transfer_id";

        int senderAccountID = 0;
        int recipientAccountID = 0;
        int transferTypeID = 0;
        int transferStatusID = 0;
        try {
            SqlRowSet result = jdbcTemplate.queryForRowSet(accountSQL, transfer.getSenderId());
            if (result.next()) {
                senderAccountID = result.getInt("account_id");
                if (senderAccountID == 0) { throw new DaoException("From Account Id did not get set");}
            }
            result = jdbcTemplate.queryForRowSet(accountSQL, transfer.getRecipientId());
            if (result.next()) {
                recipientAccountID = result.getInt("account_id");
                if (recipientAccountID == 0) { throw new DaoException("To Account Id did not get set");}
            }
            result = jdbcTemplate.queryForRowSet(transferTypeSQL, transfer.getType());
            if (result.next()){
                transferTypeID = result.getInt("transfer_type_id");
                if (transferTypeID == 0) { throw new DaoException("Transfer Type Id did not get set");}
            }
            result = jdbcTemplate.queryForRowSet(transferStatusSQL, transfer.getStatus());
            if (result.next()){
                transferStatusID = result.getInt("transfer_status_id");
                if (transferStatusID == 0) { throw new DaoException("Transfer Status Id did not get set");}
            }

            int transferID = jdbcTemplate.queryForObject(createSQL, int.class, transferTypeID, transferStatusID, senderAccountID, recipientAccountID, transfer.getAmount());
            transfer.setTransferId(transferID);
        } catch (Exception e) {
            throw new DaoException("There was an error.", e);
        }
        return transfer;
    }

    @Override
    public void updateAccountBalances(Transfer transfer) {

    }

    @Override
    public List<User> retrieveListOfUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username " +
                     "FROM tenmo_user";

        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            while(results.next()) {
                users.add(mapRowToUser(results));
            }
        }
        catch (Exception e) {
            throw new DaoException("There was an error fetching the list of users.", e);
        }

        return users;
    }

    private Account mapRowToAccount(SqlRowSet rowSet){
        Account account = new Account();
        account.setBalance(rowSet.getBigDecimal("balance"));
        return account;
    }

    private User mapRowToUser(SqlRowSet rowSet) {
        User user = new User();
        user.setId(rowSet.getInt("user_id"));
        user.setUsername(rowSet.getString("username"));
        return user;
    }
}
