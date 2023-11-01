package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class TenmoService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String API_BASE_URL = "http://localhost:8080/";
    private String authToken = null;

    public void setAuthToken(String authToken){
        this.authToken = authToken;
    }

    public Account retrieveAccountBalance() {
        Account retreivedAccount = null;

        try {
            ResponseEntity<Account> response = restTemplate.exchange(API_BASE_URL + "accounts", HttpMethod.GET, makeAuthEntity(), Account.class);
            retreivedAccount = response.getBody();
        }
        catch (Exception e) {
            BasicLogger.log(e.getMessage());
        }

        return retreivedAccount;
    }

    public User[] retrieveListOfUsers() {
        User[] users = null;

        try {
            ResponseEntity<User[]> response = restTemplate.exchange(API_BASE_URL + "users", HttpMethod.GET, makeAuthEntity(), User[].class);
            users = response.getBody();
        }
        catch (Exception e) {
            BasicLogger.log(e.getMessage());
        }

        return users;
    }

    //TODO - CREATE TRANSFER (bundle userId into object)


    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }


}
