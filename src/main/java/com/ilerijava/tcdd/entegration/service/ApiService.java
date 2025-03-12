package com.ilerijava.tcdd.entegration.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class ApiService {
    private static final String BASE_URL = "https://api-yebsp.tcddtasimacilik.gov.tr/";
    private static final String AUTH_HEADER = "Basic " + Base64.getEncoder().encodeToString("tcdd:tcdd123".getBytes());
    private final HttpClient httpClient;

    public ApiService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public String sendPostRequest(String endpoint, String jsonBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", AUTH_HEADER)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String checkAvailability(String departureStation, String arrivalStation, String date)
            throws IOException, InterruptedException {
        String jsonBody = String.format("""
                {
                    "binisIstasyonu": "%s",
                    "inisIstasyonu": "%s",
                    "binisIstasyonId": null,
                    "inisIstasyonId": null,
                    "gidisTarih": "%s",
                    "yolcuSayisi": 1
                }""", departureStation, arrivalStation, date);

        return sendPostRequest("sefer/seferSorgula", jsonBody);
    }
}