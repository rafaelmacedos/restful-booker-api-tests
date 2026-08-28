package com.booker.restful.service;

import com.booker.restful.config.Specs;

import static io.restassured.RestAssured.given;
import io.restassured.response.Response;

public class HealthCheckService {
    public Response ping() {
        return given().spec(Specs.base())
                .when().get("/ping");
    }
}
