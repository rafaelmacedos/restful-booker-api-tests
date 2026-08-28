package com.booker.restful.tests;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.booker.restful.tags.KnownIssue;
import com.booker.restful.tags.Regression;
import com.booker.restful.tags.Smoke;

import com.booker.restful.data.BookingFactory;
import com.booker.restful.model.BookingResponse;

@Regression
public class BookingCreate extends BaseTest {

    @Test
    @Smoke
    void shouldCreateValidBookingWithPaidDeposit() {
        var booking = BookingFactory.validBookingWithPaidDeposit();

        BookingResponse res = bookingService.create(booking)
                .then().statusCode(200)
                .extract().as(BookingResponse.class);
        trackForCleanup(res.bookingid());

        assertThat(res.bookingid()).isPositive();
        assertThat(res.booking().firstname()).isEqualTo(booking.firstname());
        assertThat(res.booking().depositpaid()).isTrue();
    }

    @Test
    void shouldCreateValidBookingWithUnpaidDeposit() {
        var booking = BookingFactory.validBookingWithUnpaidDeposit();

        BookingResponse res = bookingService.create(booking)
                .then().statusCode(200)
                .extract().as(BookingResponse.class);
        trackForCleanup(res.bookingid());

        assertThat(res.bookingid()).isPositive();
        assertThat(res.booking().firstname()).isEqualTo(booking.firstname());
        assertThat(res.booking().depositpaid()).isFalse();
    }

    @Test
    void shouldCreateBookingWithCustomDates() {
        var booking = BookingFactory.validBookingWithCustomDates("2027-01-05", "2027-01-12");

        BookingResponse res = bookingService.create(booking)
                .then().statusCode(200)
                .extract().as(BookingResponse.class);
        trackForCleanup(res.bookingid());

        assertThat(res.booking().bookingdates().checkin()).isEqualTo("2027-01-05");
        assertThat(res.booking().bookingdates().checkout()).isEqualTo("2027-01-12");
    }

    @Test
    void shouldCreateBookingWithEmptyAdditionalNeeds() {
        var booking = BookingFactory.validBookingWithEmptyAdditionalNeeds();

        BookingResponse res = bookingService.create(booking)
                .then().statusCode(200)
                .extract().as(BookingResponse.class);
        trackForCleanup(res.bookingid());

        assertThat(res.booking().additionalneeds()).isEmpty();
    }

    @Test
    @KnownIssue
    @Disabled("API defect: a payload with null required fields returns 500 instead of 400. "
            + "A 5xx for malformed client input is a server-side error leaking out, not a validation response.")
    void shouldRejectBookingWithMissingFields() {
        bookingService.create(BookingFactory.invalidBookingWithMissingFields())
                .then().statusCode(400);
    }

    @Test
    @KnownIssue
    @Disabled("Validation not implemented: the API accepts checkout before checkin and returns 200. "
            + "The restful-booker docs do not specify this rule, so 400 here is the expected behaviour we argue for.")
    void shouldRejectBookingWithCheckoutBeforeCheckin() {
        bookingService.create(BookingFactory.invalidBookingWithInvalidDates())
                .then().statusCode(400);
    }

    @Test
    @KnownIssue
    @Disabled("Validation not implemented: the API accepts a negative totalprice and returns 200. "
            + "The restful-booker docs do not specify this rule, so 400 here is the expected behaviour we argue for.")
    void shouldRejectBookingWithNegativePrice() {
        bookingService.create(BookingFactory.invalidBookingWithNegativePrice())
                .then().statusCode(400);
    }

}
