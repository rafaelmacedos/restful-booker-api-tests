package com.booker.restful.tests;

import org.junit.jupiter.api.Test;

import com.booker.restful.tags.Regression;
import com.booker.restful.tags.Smoke;

import com.booker.restful.service.HealthCheckService;

@Regression
public class HealthCheck extends BaseTest {

    private final HealthCheckService healthCheckService = new HealthCheckService();

    @Test
    @Smoke
    void shouldReturnCreatedOnPing() {
        healthCheckService.ping().then().statusCode(201);
    }
}
