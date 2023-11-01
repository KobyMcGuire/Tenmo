package com.techelevator.tenmo.services;

import org.springframework.web.client.RestTemplate;

public class TenmoService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String API_BASE_URL = "http://localhost:8080/";
    private String authToken = null;

    public void setAuthToken(String authToken){
        this.authToken = authToken;
    }


}
