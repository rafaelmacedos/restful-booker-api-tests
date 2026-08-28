package com.booker.restful.tests;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.booker.restful.tags.Regression;
import com.booker.restful.tags.Smoke;

import com.booker.restful.data.BookingFactory;
import com.booker.restful.model.Booking;

@Regression
public class BookingRead extends BaseTest {

    @Test
    @Smoke
    void shouldGetBookingById() {
        var created = BookingFactory.validBookingWithPaidDeposit();
        int id = trackForCleanup(bookingService.create(created)
                .then().statusCode(200)
                .extract().path("bookingid"));

        // GET /booking/{id} returns the booking itself, not the {bookingid, booking} envelope of POST.
        Booking found = bookingService.getBookingById(id)
                .then().statusCode(200)
                .extract().as(Booking.class);

        assertThat(found).isEqualTo(created);
    }

    @Test
    void shouldReturnNotFoundForUnknownBookingId() {
        bookingService.getBookingById(99999999)
                .then().statusCode(404);
    }
}
