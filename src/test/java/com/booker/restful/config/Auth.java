package com.booker.restful.config;

import java.util.Map;

import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

public final class Auth {

    private static String token;

    public static synchronized String token() {
        if (token == null) token = login();
        return token;
    }

    private static String login() {
        return given()
                .baseUri(Config.baseUrl())
                .contentType(ContentType.JSON)
                .body(Map.of("username", Config.authLogin(),
                             "password", Config.authPassword()))
        .when()
                .post("/auth")
        .then()
                .statusCode(200)
                .extract().path("token");
    }
}