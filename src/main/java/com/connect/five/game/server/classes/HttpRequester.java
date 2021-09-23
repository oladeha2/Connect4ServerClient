package com.connect.five.game.server.classes;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpRequester {

    Gson gson = new Gson();

    public GameState makePostRequest(String uri){
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(uri))
                    .POST(HttpRequest.BodyPublishers.ofString(""))
                    .build();
            return getResponse(request);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public GameState makeGetRequest(String uri) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(uri))
                    .GET()
                    .build();
            return getResponse(request);
        } catch (URISyntaxException e){
            return null;
        }
    }

    private GameState getResponse(HttpRequest request) {
        try {
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 405 ? null : gson.fromJson(response.body(), GameState.class);
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

}
