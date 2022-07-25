package com.wiremock.demo;


import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.web.client.HttpClientErrorException;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;



public class WireMockConfiguarationTest {

    private WireMockServer wireMockServer;
    private RestTemplate restTemplate;

    @BeforeEach
    void configureSystemUnderTest(){
        this.restTemplate = new RestTemplate();
        this.wireMockServer = new WireMockServer(options()
                .dynamicPort());
        this.wireMockServer.start();
        configureFor("localhost", this.wireMockServer.port());
    }


    /**
     * Request matching: about matching the url
     */
    @Test
    @DisplayName("Should compare the actual request method with the expected method")
    void shouldCompareActualMethodWithExpectedRequestMethod(){
        givenThat(post(urlEqualTo("/api/message?id=1"))
                .withRequestBody(equalToJson("{ \"message\": \"Hello World\"}"))
                .willReturn(aResponse().withStatus(200)
                        ));

        String apiMethodUrl = buildApiMethodUrl(1);
        HttpEntity<String> postContent = new HttpEntity<>(
                "{ \"message\": \"Hello World\"}"
        );
        ResponseEntity<String> response = restTemplate.postForEntity(apiMethodUrl,
                postContent,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    private String buildApiMethodUrl(Integer id){
        return String.format("http://localhost:%d/api/message?id=%d",
                this.wireMockServer.port(),id);
    }

    @AfterEach
    void stopWireMockServer(){
        this.wireMockServer.stop();
    }
}
