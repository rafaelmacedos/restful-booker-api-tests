# Templates

Copy, rename, fill in. Import order follows the suite: static imports, JUnit, then project
packages (`tags`, `data`, `model`, `service`).

## New test class

```java
package com.booker.restful.tests;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.booker.restful.tags.Regression;
import com.booker.restful.tags.Smoke;

import com.booker.restful.data.BookingFactory;
import com.booker.restful.model.Booking;

@Regression
public class BookingSearch extends BaseTest {

    @Test
    @Smoke
    void shouldFindBookingByLastname() {
        // ...
    }
}
```

## Happy path with cleanup

The POST response is the `{bookingid, booking}` envelope, so it deserializes into
`BookingResponse` — not `Booking`.

```java
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
}
```

## Scenario that needs an existing booking

Extract only `bookingid` and wrap the call in `trackForCleanup`, which returns the id.

```java
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
```

## Authorization test

Reject, then prove the resource is untouched. A 403 that silently mutated state is still a bug.

```java
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
```

## Known issue

```java
@Test
@KnownIssue
@Disabled("Validation not implemented: the API accepts checkout before checkin and returns 200. "
        + "The restful-booker docs do not specify this rule, so 400 here is the expected behaviour we argue for.")
void shouldRejectBookingWithCheckoutBeforeCheckin() {
    bookingService.create(BookingFactory.invalidBookingWithInvalidDates())
            .then().statusCode(400);
}
```

## Service method

`Specs.base()` for anonymous, `Specs.auth()` for authenticated. Return the raw `Response`
and let the test decide what is correct.

```java
public Response searchByLastname(String lastname) {
    return given().spec(Specs.base())
            .queryParam("lastname", lastname)
            .when().get("/booking");
}

public Response partialUpdateBookingById(int id, Map<String, Object> fields) {
    return given().spec(Specs.auth())
            .body(fields)
            .when().patch("/booking/" + id);
}
```

## Factory method

Randomize identity fields; hardcode anything an assertion depends on. Take parameters only
when the test must pin a value.

```java
public static Booking validBookingWithLastname(String lastname) {
    return new Booking(
            faker.name().firstName(),
            lastname,
            faker.number().numberBetween(100, 200),
            true,
            new BookingDates("2026-10-10", "2026-10-15"),
            "Breakfast");
}
```

Name it for the property under test, not for the endpoint that uses it.

## Model record

Field names must match the JSON keys exactly — Jackson maps by name, and restful-booker
uses lowercase with no separators (`firstname`, `depositpaid`, `additionalneeds`).

```java
package com.booker.restful.model;

public record BookingId(int bookingid) {}
```

## Tag

Only when a new run selector earns its own `make` target.

```java
package com.booker.restful.tags;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;

/** One line saying what this selects and when to run it. */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Tag("contract")
public @interface Contract {}
```

Then add the target to the `Makefile` and to its `help` block:

```makefile
contract:
	$(MVN) test -Dgroups=contract
```
