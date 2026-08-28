package com.booker.restful.tests;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.booker.restful.tags.Regression;

import com.booker.restful.data.BookingFactory;
import com.booker.restful.model.Booking;

@Regression
public class BookingUpdate extends BaseTest {

    @Test
    void shouldUpdateBookingWithAuthToken() {
        int id = trackForCleanup(bookingService.create(BookingFactory.validBookingWithPaidDeposit())
                .then().statusCode(200)
                .extract().path("bookingid"));

        var updated = BookingFactory.validBookingWithUnpaidDeposit();

        Booking res = bookingService.updateBookingById(id, updated)
                .then().statusCode(200)
                .extract().as(Booking.class);

        assertThat(res).isEqualTo(updated);
    }

    @Test
    void shouldNotUpdateWithoutAuthToken() {
        var original = BookingFactory.validBookingWithPaidDeposit();
        int id = trackForCleanup(bookingService.create(original)
                .then().statusCode(200)
                .extract().path("bookingid"));

        bookingService.updateWithoutAuthToken(id, BookingFactory.validBookingWithUnpaidDeposit())
                .then().statusCode(403);

        // The booking must be untouched by the rejected update.
        Booking found = bookingService.getBookingById(id)
                .then().statusCode(200)
                .extract().as(Booking.class);

        assertThat(found).isEqualTo(original);
    }

    @Test
    void shouldPersistUpdatedBooking() {
        int id = trackForCleanup(bookingService.create(BookingFactory.validBookingWithPaidDeposit())
                .then().statusCode(200)
                .extract().path("bookingid"));

        var updated = BookingFactory.validBookingWithCustomDates("2027-03-01", "2027-03-08");
        bookingService.updateBookingById(id, updated).then().statusCode(200);

        // Re-reads instead of trusting the PUT response body, so the assertion covers persistence.
        Booking found = bookingService.getBookingById(id)
                .then().statusCode(200)
                .extract().as(Booking.class);

        assertThat(found).isEqualTo(updated);
    }
}
