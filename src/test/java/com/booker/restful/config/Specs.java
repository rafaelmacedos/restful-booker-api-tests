package com.booker.restful.config;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public final class Specs {

    private static final RequestSpecification BASE = new RequestSpecBuilder()
            .setBaseUri(Config.baseUrl())
            .setContentType(ContentType.JSON)
            .build();

    /** sem token */
    public static RequestSpecification base() {
        return BASE;
    }

    /** token valido, via login */
    public static RequestSpecification auth() {
        return auth(Auth.token());
    }

    /** token arbitrario, para cenarios negativos */
    public static RequestSpecification auth(String token) {
        return new RequestSpecBuilder()
                .addRequestSpecification(BASE)
                .addCookie("token", token)
                .build();
    }
}