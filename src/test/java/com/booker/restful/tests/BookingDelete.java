package com.booker.restful.tests;

import org.junit.jupiter.api.Test;

import com.booker.restful.tags.Regression;
import com.booker.restful.tags.Smoke;

import com.booker.restful.data.BookingFactory;

@Regression
public class BookingDelete extends BaseTest {

    @Test
    @Smoke
    void shouldDeleteBooking() {
        // Not tracked for cleanup: deleting it is the test.
        int id = bookingService.create(BookingFactory.validBookingWithPaidDeposit())
                .then().statusCode(200)
                .extract().path("bookingid");

        bookingService.deleteBookingById(id).then().statusCode(201);

        bookingService.getBookingById(id).then().statusCode(404);
    }

    @Test
    void shouldNotDeleteWithoutAuthToken() {
        int id = trackForCleanup(bookingService.create(BookingFactory.validBookingWithPaidDeposit())
                .then().statusCode(200)
                .extract().path("bookingid"));

        bookingService.deleteWithoutAuthToken(id).then().statusCode(403);

        // The booking must survive the rejected delete.
        bookingService.getBookingById(id).then().statusCode(200);
    }
}
