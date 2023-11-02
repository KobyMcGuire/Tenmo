package com.techelevator.dao;
import com.techelevator.tenmo.dao.JdbcTransferDao;
import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.dao.TransferDao;
import org.junit.*;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcTransferDaoTest extends BaseDaoTests {

    private TransferDao sut;

    @Before
    public void setup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        sut = new JdbcTransferDao(jdbcTemplate);
    }
    /*
    Account retrieveAccountBalance(String username);

    boolean validateTransfer(Transfer transfer);

    Transfer createTransfer(Transfer transfer);

    void updateAccountBalances(Transfer transfer);

    List<User> retrieveListOfUsers();

    List<Transfer> retrieveListOfTransfers(int userId);

    List<Transfer> retrieveListOfPendingTransfers(int userId);

    Transfer retrieveTransferById(int transferId);

    int updateTransferStatus(Transfer transfer);
     */

    @Test




}
