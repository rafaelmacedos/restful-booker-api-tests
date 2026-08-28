package com.booker.restful.config;

import static org.hamcrest.Matchers.lessThan;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public final class Specs {

    private static final RequestSpecification BASE = new RequestSpecBuilder()
            .setBaseUri(Config.baseUrl())
            .setContentType(ContentType.JSON)
            .build();

    private static final ResponseSpecification TIMED = new ResponseSpecBuilder()
            .expectResponseTime(lessThan(Config.maxResponseTimeMs()))
            .build();

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);
        RestAssured.responseSpecification = TIMED;

        if (Config.httpLog()) {
            RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        }
    }

    public static RequestSpecification base() {
        return BASE;
    }

    public static RequestSpecification auth() {
        return auth(Auth.token());
    }

    public static RequestSpecification auth(String token) {
        return new RequestSpecBuilder()
                .addRequestSpecification(BASE)
                .addCookie("token", token)
                .build();
    }
}
