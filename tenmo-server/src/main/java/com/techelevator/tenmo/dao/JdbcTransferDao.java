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
    public boolean validateTransfer(Transfer transfer) {
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

          // Transaction ??
//        // Begin Transaction SQL
//        String beingTransactionSql = "BEGIN TRANSACTION";
//        // Rollback SQL
//        String rollbackSql = "ROLLBACK";
//        // Commit SQL
//        String commitSql = "COMMIT";

        //  Grabbing balance SQL
        String grabBalanceSql = "SELECT balance " +
                     "FROM account " +
                     "WHERE user_id = ?";

        // Updating account balance SQL
        String updateBalanceSql = "UPDATE account SET balance = ? WHERE user_id = ?";


        try {
            BigDecimal senderBalance = new BigDecimal(0);
            BigDecimal recipientBalance = new BigDecimal(0);

            // Sender
            SqlRowSet result = jdbcTemplate.queryForRowSet(grabBalanceSql, transfer.getSenderId());
            if (result.next()) {
                senderBalance = result.getBigDecimal("balance");
                senderBalance = senderBalance.subtract(transfer.getAmount());
                int rowsAffected = jdbcTemplate.update(updateBalanceSql, senderBalance, transfer.getSenderId());

                // Exception handling
                if (rowsAffected == 0) {
                    throw new DaoException("No rows were updated for the sender.");
                }
            }

            // Recipient
            result = jdbcTemplate.queryForRowSet(grabBalanceSql, transfer.getRecipientId());
            if (result.next()) {
                recipientBalance = result.getBigDecimal("balance");
                recipientBalance = recipientBalance.add(transfer.getAmount());
                int rowsAffected = jdbcTemplate.update(updateBalanceSql, recipientBalance, transfer.getRecipientId());

                // Exception handling
                if (rowsAffected == 0) {
                    throw new DaoException("No rows were updated for the recipient.");
                }
            }
        }
        catch (Exception e) {
            throw new DaoException("There was an error with updating the balances.", e);
        }

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

    @Override
    public List<Transfer> retrieveListOfTransfers(int userId) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT t.transfer_id, tt.transfer_type_desc, ts.transfer_status_desc, t.amount, " +
                "tuf.username AS sender_username, tut.username AS recipient_username, " +
                "af.user_id AS sender_id, at.user_id AS recipient_id " +
                "FROM transfer AS t " +
                "JOIN transfer_type AS tt ON t.transfer_type_id = tt.transfer_type_id " +
                "JOIN transfer_status AS ts ON t.transfer_status_id = ts.transfer_status_id " +
                "JOIN account AS af ON t.account_from = af.account_id " +
                "JOIN account AS at ON t.account_to = at.account_id " +
                "JOIN tenmo_user AS tuf ON af.user_id = tuf.user_id " +
                "JOIN tenmo_user AS tut ON at.user_id = tut.user_id " +
                "WHERE af.user_id = ? OR at.user_id = ?";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId, userId);
            while (results.next()){
                transfers.add(mapRowToTransfer(results));
            }
        } catch (Exception e ) {
            throw new DaoException("There was an error getting transfers.", e);
        }
        return transfers;
    }

    public List<Transfer> retrieveListOfPendingTransfers(int userId) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT t.transfer_id, tt.transfer_type_desc, ts.transfer_status_desc, t.amount, " +
                "tuf.username AS sender_username, tut.username AS recipient_username, " +
                "af.user_id AS sender_id, at.user_id AS recipient_id " +
                "FROM transfer AS t " +
                "JOIN transfer_type AS tt ON t.transfer_type_id = tt.transfer_type_id " +
                "JOIN transfer_status AS ts ON t.transfer_status_id = ts.transfer_status_id " +
                "JOIN account AS af ON t.account_from = af.account_id " +
                "JOIN account AS at ON t.account_to = at.account_id " +
                "JOIN tenmo_user AS tuf ON af.user_id = tuf.user_id " +
                "JOIN tenmo_user AS tut ON at.user_id = tut.user_id " +
                "WHERE af.user_id = ? AND ts.transfer_status_desc ILIKE 'Pending'";

        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
            while (results.next()) {
                transfers.add(mapRowToTransfer(results));
            }
        }
        catch (Exception e) {
            throw  new DaoException("There was a problem with fetching the pending transactions.", e);
        }

        return transfers;
    }

    public Transfer retrieveTransferById(int transferId){
        Transfer transfer = null;
        String sql = "SELECT t.transfer_id, tt.transfer_type_desc, ts.transfer_status_desc, t.amount, " +
                "tuf.username AS sender_username, tut.username AS recipient_username, " +
                "af.user_id AS sender_id, at.user_id AS recipient_id " +
                "FROM transfer AS t " +
                "JOIN transfer_type AS tt ON t.transfer_type_id = tt.transfer_type_id " +
                "JOIN transfer_status AS ts ON t.transfer_status_id = ts.transfer_status_id " +
                "JOIN account AS af ON t.account_from = af.account_id " +
                "JOIN account AS at ON t.account_to = at.account_id " +
                "JOIN tenmo_user AS tuf ON af.user_id = tuf.user_id " +
                "JOIN tenmo_user AS tut ON at.user_id = tut.user_id " +
                "WHERE t.transfer_id = ?";
        try {
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, transferId);
            if (result.next()) {
                transfer = mapRowToTransfer(result);
            }
        } catch (Exception e) {
            throw new DaoException("There was an error locating specific transfer.", e);
        }
        return transfer;
    }

    public int updateTransfer(Transfer transfer){
        int rowsAffected = 0;
        String sql = "UPDATE transfer " +
                "SET transfer_status_id = (SELECT transfer_status_id FROM transfer_status WHERE transfer_status_desc ILIKE ?) " +
                "WHERE transfer_id = ?";
        try {
            rowsAffected = jdbcTemplate.update(sql, transfer.getStatus(), transfer.getTransferId());
            if (rowsAffected == 0) {
                throw new DaoException("No rows were updated.");
            }
        } catch (Exception e) {
            throw new DaoException("There was an error updating the transfer.", e);
        }
        return rowsAffected;
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

    private Transfer mapRowToTransfer(SqlRowSet rowSet){
        Transfer transfer = new Transfer();
        transfer.setTransferId(rowSet.getInt("transfer_id"));
        transfer.setSenderUsername(rowSet.getString("sender_username"));
        transfer.setSenderId(rowSet.getInt("sender_id"));
        transfer.setRecipientId(rowSet.getInt("recipient_id"));
        transfer.setRecipientUsername(rowSet.getString("recipient_username"));
        transfer.setAmount(rowSet.getBigDecimal("amount"));
        transfer.setType(rowSet.getString("transfer_type_desc"));
        transfer.setStatus(rowSet.getString("transfer_status_desc"));

        return transfer;
    }
}
