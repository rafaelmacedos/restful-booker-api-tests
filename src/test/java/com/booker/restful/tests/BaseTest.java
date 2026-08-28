package com.booker.restful.tests;

import java.util.ArrayList;
import java.util.List;

import com.booker.restful.service.BookingService;
import com.booker.restful.service.HealthCheckService;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

/**
 * Shared setup for every test class. @BeforeAll must be static because JUnit runs it
 * before any instance of the test class exists.
 */
public abstract class BaseTest {

    private static final int PING_OK = 201;

    protected final BookingService bookingService = new BookingService();

    private final List<Integer> createdBookingIds = new ArrayList<>();

    @BeforeAll
    static void healthCheck() {
        Response response = new HealthCheckService().ping();

        if (response.statusCode() != PING_OK) {
            throw new IllegalStateException(
                    "API is not available. Expected " + PING_OK + " from /ping but got " + response.statusCode());
        }
    }

    /** Registers a booking id so it gets deleted once the test finishes. Returns the id for chaining. */
    protected int trackForCleanup(int bookingId) {
        createdBookingIds.add(bookingId);
        return bookingId;
    }

    /**
     * Deletes whatever the test created. The service call is left unvalidated on purpose:
     * a failing cleanup must not turn a passing test red or hide its real result.
     */
    @AfterEach
    void deleteCreatedBookings() {
        createdBookingIds.forEach(bookingService::deleteBookingById);
        createdBookingIds.clear();
    }
}
