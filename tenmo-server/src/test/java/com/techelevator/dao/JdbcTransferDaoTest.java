package com.techelevator.dao;
import com.techelevator.tenmo.dao.JdbcTransferDao;
import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import org.junit.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class JdbcTransferDaoTest extends BaseDaoTests {

    /*
    INSERT INTO account (user_id,balance) VALUES (1001,1000.00); -- 2001
INSERT INTO account (user_id,balance) VALUES (1002,1000.00); -- 2002
INSERT INTO account (user_id,balance) VALUES (1003,1000.00); -- 2003

INSERT INTO transfer (transfer_type_id,transfer_status_id,account_from,account_to,amount)
    VALUES (2,2,2001,2002,100.00) -- 3001
INSERT INTO transfer (transfer_type_id,transfer_status_id,account_from,account_to,amount)
    VALUES (1,1,2001,2002,200.00) -- 3002 // Request/Pending (test for Approved)
INSERT INTO transfer (transfer_type_id,transfer_status_id,account_from,account_to,amount)
    VALUES (1,1,2002,2003,200.00) -- 3003 // Request/Pending (test for Rejected)
     */
    private static final Account ACCOUNT_2001 = new Account(BigDecimal.valueOf(1000.00));
    private static final Account ACCOUNT_2002 = new Account(BigDecimal.valueOf(2000.00));
    private static final Account ACCOUNT_2003 = new Account(BigDecimal.valueOf(3000.00));

    private static final Transfer TRANSFER_3001 = new Transfer(3001, 1001, 1002, BigDecimal.valueOf(100.00), "Send", "Approved", "user1", "user2");
    private static final Transfer TRANSFER_3002 = new Transfer(3002, 1001, 1002, BigDecimal.valueOf(200.00), "Send", "Approved", "user1", "user2");
    private static final Transfer TRANSFER_3003 = new Transfer(3003, 1002, 1003, BigDecimal.valueOf(200.00), "Send", "Approved", "user2", "user3");
    private static final Transfer TRANSFER_3004 = new Transfer(3004, 1001, 1002, BigDecimal.valueOf(1000000.00), "Send", "Approved", "user1", "user2");

    private TransferDao sut;

    @Before
    public void setup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        sut = new JdbcTransferDao(jdbcTemplate);
    }


//    Account retrieveAccountBalance(String username) {
//
//
//
//    }
        // retrieves correct account balance for username passed in
        // returns null account if username is invalid

    @Test
    public void retrieveAccountBalance_returns_correct_balance() {
        final BigDecimal expectedValue = new BigDecimal(1000.00).setScale(2);
        BigDecimal balance =  sut.retrieveAccountBalance("user1").getBalance().setScale(2);

        Assert.assertEquals(expectedValue, balance);
    }

    //boolean validateTransfer(Transfer transfer);
        // returns true of transfer amount is less than or equal to "sender's" balance
        // returns false if transfer amount is greater than "sender's" balance
        // handle if transfer object is somehow invalid?

    @Test
    public void validateTransfer_returns_correct_boolean() {
        // Happy path
        boolean isAbleToTransfer = sut.validateTransfer(TRANSFER_3001);
        Assert.assertTrue(isAbleToTransfer);

        // Not enough money, should fail
        isAbleToTransfer = sut.validateTransfer(TRANSFER_3004);
        Assert.assertFalse(isAbleToTransfer);


    }

    //Transfer createTransfer(Transfer transfer);
        // creates a transfer in the transfer table
        // check that created city isn't null
        // check that returned transfer id is > 0
        // when getting a transfer by the new id, the transfer matches the original created transfer

    //void updateAccountBalances(Transfer transfer);
        // pull an account from test database
        // update balance (updatedBalance)
        // put updated transfer(balance) into database, confirm rowsAffected is > 0
        // pull balance by transferId and check that it equals updatedBalance


    //List<User> retrieveListOfUsers();
        // get list of users
            // confirm length of list is equal to expected
            // confirm that users at index 0 match? (would need to add an ORDER BY to SQL)


    //List<Transfer> retrieveListOfTransfers(int userId);
        // get list of transfers passing a test data user id in
        // confirm that length of list is expected
        // confirm that transfers at index 0 match (would need to add an ORDER BY to SQL)


    //List<Transfer> retrieveListOfPendingTransfers(int userId);
        // get list of transfers passing a test data user id in
            // confirm that length of list is expected
            // confirm that two transfers match OR? confirm that for each transfer in list, the status equals "Pending"
        // ?confirm that an empty list is returned when an invalid id is sent in?

    //Transfer retrieveTransferById(int transferId);
        // get transfer by id and make sure it matches local matching transfer
        // confirm that transfer is null when passed an invalid transferId


    //int updateTransferStatus(Transfer transfer);
        // pull a transfer from the database by transferId
        // update the status on the transfer (and confirm return is not null?)
        // get transfer by original transfer id and see if it matched the updatedTransfer

        // check that Approved and Rejected both work

        // confirm int rows affected == 1
        // ?confirm int rows affected == 0 if transfer object is null?


    



}
