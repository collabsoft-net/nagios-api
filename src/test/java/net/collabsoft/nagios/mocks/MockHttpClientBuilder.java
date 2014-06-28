package net.collabsoft.nagios.mocks;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class MockHttpClientBuilder extends HttpClientBuilder {

    // ----------------------------------------------------------------------------------------------- Constructor

    private final HttpClient client;

    public MockHttpClientBuilder(HttpClient client) {
        this.client = client;
    }

    @Override
    public CloseableHttpClient build() {
        return (CloseableHttpClient)client;
    }

    // ----------------------------------------------------------------------------------------------- Getters & Setters


    // ----------------------------------------------------------------------------------------------- Public methods


    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

}
