package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
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
        return false;
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
