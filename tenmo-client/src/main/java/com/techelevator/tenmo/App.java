package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.TenmoService;

import java.math.BigDecimal;
import java.util.List;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private final TenmoService tenmoService = new TenmoService();

    private AuthenticatedUser currentUser;

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }
    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        } else {
            tenmoService.setAuthToken(currentUser.getToken());
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

	private void viewCurrentBalance() {
		// TODO Auto-generated method stub
        Account account = tenmoService.retrieveAccountBalance();
        consoleService.printCurrentBalance(account.getBalance());
	}

	private void viewTransferHistory() {
		Transfer[] transfers = tenmoService.retrieveListOfTransfers(currentUser.getUser().getId());
        if (transfers == null){
            consoleService.printErrorMessage();
            return;
        }
        // pass list to console to print
        consoleService.printListOfTransfers(transfers, currentUser.getUser().getUsername());
        int userResponse = consoleService.promptForInt("Please enter transfer ID to view details (0 to cancel): ");
        if (userResponse == 0){
            return;
        }
        viewTransferById(userResponse);

	}
    private void viewTransferById(int transferId){
        Transfer transfer = tenmoService.retrieveTransferById(transferId);
        // pass transfer to console to print
        consoleService.printTransactionDetails(transfer);
    }

	private void viewPendingRequests() {
        Transfer[] transfers = tenmoService.retrieveListOfPendingTransfers(currentUser.getUser().getId());
        consoleService.printListOfPendingTransfers(transfers);
	}

	private void sendBucks() {
        // Print list of users
        User[] users = tenmoService.retrieveListOfUsers();
        consoleService.printListOfUsers(users, currentUser.getUser().getId());

        // Prompt user for recipient user Id
        int recipientUserId = consoleService.promptForInt("Enter ID of user you are sending to (0 to cancel): ");
        if (recipientUserId == 0) {
            return;
        }

        // Prompt user for amount
        BigDecimal amount = consoleService.promptForBigDecimal("Enter amount: ");

        // Bundle transfer before sending it through service method
        Transfer transfer = bundleTransfer(recipientUserId, amount, "Send");

        // Call service
        transfer = tenmoService.createTransfer(transfer);
	}

	private void requestBucks() {
        // Print list of users to pick from
        consoleService.printListOfUsers(tenmoService.retrieveListOfUsers(), currentUser.getUser().getId());

        // Grab user input for their chosen user
        int requesteeId = consoleService.promptForInt("Enter ID of user you are requesting from (0 to cancel): ");
        if (requesteeId == 0) {
            return;
        }

        // Take in the user input for the amount
        BigDecimal amount = consoleService.promptForBigDecimal("Enter Amount: ");

        // Bundle the transfer
        Transfer transfer = bundleTransfer(requesteeId, amount, "Request");

        // Call Tenmo Service to create the transfer
        tenmoService.createTransfer(transfer);
    }

    private Transfer bundleTransfer(int userChoiceId, BigDecimal amount, String type) {
        Transfer transfer = new Transfer();
        int currentUserId = currentUser.getUser().getId();

        if (type.equalsIgnoreCase("Send")) {
            transfer.setSenderId(currentUserId);
            transfer.setRecipientId(userChoiceId);
            transfer.setAmount(amount);
            transfer.setType(type);
            transfer.setStatus("Approved");
        }
        else if (type.equalsIgnoreCase("Request")) {
            transfer.setSenderId(userChoiceId);
            transfer.setRecipientId(currentUserId);
            transfer.setAmount(amount);
            transfer.setType(type);
            transfer.setStatus("Pending");
        }

        return transfer;
    }

}
