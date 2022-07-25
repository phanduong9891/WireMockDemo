package com.wiremock.demo;


import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.common.Json;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.swing.tree.RowMapper;
import java.util.ArrayList;
import java.util.List;
import org.json.*;

import static org.assertj.core.api.Assertions.*;

public class WireMockRealTest {

    //use wiremock server, need to import
    private WireMockServer wireMockServer;
    private RestTemplate restTemplate;

    private String apiLink() {
        return String.format("http://localhost:%d/api/room", wireMockServer.port());
    }


    @BeforeEach
    void wireMockServerSetUp() {
        this.restTemplate = new RestTemplate();
        this.wireMockServer = new WireMockServer(options()
                .dynamicPort());
        wireMockServer.start();
        configureFor(this.wireMockServer.port());
    }
    //search room -> "number": 1, "type": "big"


    @Test
    void testGetAll() {

        Room room1 = new Room(1,"big");
        Room room2 = new Room(2,"small");
        List<Room> roomList = new ArrayList<>();
        roomList.add(room1);
        roomList.add(room2);

        Gson gson = new Gson();
        String json = gson.toJson(roomList);

        givenThat(get(urlEqualTo("/api/room/1"))
                .willReturn(aResponse()
                        .withBody(json)));

        String link = apiLink() + "/1";
        HttpEntity<String> getContent = new HttpEntity<>(json);

        //this is a request to the link
        ResponseEntity<String> response1 = restTemplate.exchange(
                link,
                HttpMethod.GET,
                getContent,
                String.class
        );
     assertThat(response1.getBody()).isEqualTo(json);

    }


    @Test
    void testFindById() {
        //before use wiremock, need to import wiremock
        //stub a response for a get request to the below url
        givenThat(get(urlEqualTo("/api/room/1"))
                .willReturn(aResponse()
                        .withBody("{\"number\": 1, \"type\": \"big\"}")));

        String link = apiLink() + "/1";

        //this is a request to the link
        ResponseEntity<String> response = restTemplate.getForEntity(link, String.class);

        assertThat(response.getBody()).isEqualTo("{\"number\": 1, \"type\": \"big\"}");

    }

    @Test
    void testPostApi() {
        //stub a post response with an input body
        givenThat(post(urlEqualTo("/api/room/add"))
                .withRequestBody(equalToJson("{\"number\": 2, \"type\": \"small\"}"))
                .willReturn(aResponse()
                        .withStatus(200)));

        String link = apiLink() + "/add";
        HttpEntity<String> postContent = new HttpEntity<>(
                "{\"number\": 2, \"type\": \"small\"}"
        );

        //create a post request
        ResponseEntity<String> response = restTemplate.postForEntity(link, postContent, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testPutApi() {
        //stub an update request, will receive an input body
        givenThat(put(urlEqualTo("/api/room/update"))
                .withRequestBody(equalToJson("{\"number\": 2, \"type\": \"small\"}"))
                .willReturn(aResponse()
                        .withStatus(200)));


        String link = apiLink() + "/update";
        HttpEntity<String> putContent = new HttpEntity<>(
                "{\"number\": 2, \"type\": \"small\"}"
        );

        //create a put request
        ResponseEntity<String> response = restTemplate.exchange(link,
                HttpMethod.PUT,
                putContent,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

  @Test
  void testDeleteApi(){

        givenThat(delete(urlEqualTo("/api/room/delete/1"))
                .willReturn(noContent()));

        String link = apiLink() + "/delete/1";
        HttpEntity<String> entity = new HttpEntity<>("{\"number\": 2, \"type\": \"small\"}");

        //create a delete request
      ResponseEntity<String> response = restTemplate.exchange(
              link,
              HttpMethod.DELETE,
              entity,
              String.class
      );

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

    @AfterEach
    void stopWireMockServer() {
        wireMockServer.stop();
    }

}

