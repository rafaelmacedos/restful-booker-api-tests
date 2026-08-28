package com.booker.restful.service;

import com.booker.restful.config.Specs;
import com.booker.restful.model.Booking;

import static io.restassured.RestAssured.given;
import io.restassured.response.Response;

public class BookingService {

    public Response create(Booking booking) {
        return given().spec(Specs.base())
                .body(booking)
                .when().post("/booking");
    }

    public Response getBookingById(int id) {
        return given().spec(Specs.base())
                .when().get("/booking/" + id);
    }

    public Response updateBookingById(int id, Booking booking) {
        return given().spec(Specs.auth())
                .body(booking)
                .when().put("/booking/" + id);
    }

    public Response updateWithoutAuthToken(int id, Booking booking) {
        return given().spec(Specs.base())
                .body(booking)
                .when().put("/booking/" + id);
    }

    public Response deleteBookingById(int id) {
        return given().spec(Specs.auth())
                .when().delete("/booking/" + id);
    }

    public Response deleteWithoutAuthToken(int id) {
        return given().spec(Specs.base())
                .when().delete("/booking/" + id);
    }
}
