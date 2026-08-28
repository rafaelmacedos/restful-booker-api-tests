package com.booker.restful.data;

import com.booker.restful.model.Booking;
import com.booker.restful.model.BookingDates;

import net.datafaker.Faker;

public final class BookingFactory {
    static Faker faker = new Faker();

    public static Booking validBookingWithPaidDeposit() {
        return new Booking(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.number().numberBetween(100, 200),
                true,
                new BookingDates("2026-10-10", "2026-10-15"),
                "Breakfast");
    }

    public static Booking validBookingWithUnpaidDeposit() {
        return new Booking(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.number().numberBetween(100, 200),
                false,
                new BookingDates("2026-10-10", "2026-10-15"),
                "Breakfast");
    }

    public static Booking validBookingWithCustomDates(String checkin, String checkout) {
        return new Booking(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.number().numberBetween(100, 200),
                true,
                new BookingDates(checkin, checkout),
                "Breakfast");
    }

    public static Booking invalidBookingWithMissingFields() {
        return new Booking(
                null,
                null,
                0,
                false,
                null,
                null);
    }

    public static Booking invalidBookingWithInvalidDates() {
        return new Booking(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.number().numberBetween(100, 200),
                true,
                new BookingDates("2026-10-15", "2026-10-10"), // Invalid dates (checkout before checkin)
                "Breakfast");
    }

    public static Booking invalidBookingWithNegativePrice() {
        return new Booking(
                faker.name().firstName(),
                faker.name().lastName(),
                -100, // Invalid negative price
                true,
                new BookingDates("2026-10-10", "2026-10-15"),
                "Breakfast");
    }

    public static Booking validBookingWithEmptyAdditionalNeeds() {
        return new Booking(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.number().numberBetween(100, 200),
                true,
                new BookingDates("2026-10-10", "2026-10-15"),
                ""); // Empty additionalneeds is accepted by the API and is a legitimate payload
    }
}