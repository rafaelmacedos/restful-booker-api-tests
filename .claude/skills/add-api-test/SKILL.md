---
name: add-api-test
description: Conventions and step-by-step recipe for adding or changing tests in this REST Assured + JUnit 5 suite against the restful-booker API. Use whenever creating a new test, test class, service method, payload factory, model record, or tag, when covering a new endpoint, and when reviewing test code for consistency with the suite.
---

# Adding tests to the restful-booker suite

Four layers, one direction of dependency: `tests` -> `service` -> `config`, with
`data` producing `model` payloads. A test never builds a request; a service never asserts.
Break that and the suite stops being predictable.

## 1. Pick the layer before writing code

| What you are adding | Where it goes | Rule |
|---|---|---|
| A new HTTP call | `service/*Service.java` | Returns raw `Response`. No assertions, no status codes, no logic. |
| A new request payload | `data/BookingFactory.java` | `static` method, no arguments unless the test needs to pin a value. |
| A new response/request shape | `model/*.java` | A `record`. Field names must match the JSON exactly. |
| A new scenario | `tests/*.java` | Method on the class that owns that operation. |
| A new operation on an existing resource | `tests/<Resource><Operation>.java` | New class, e.g. `BookingSearch`. |
| A new run selector | `tags/*.java` | Meta-annotation + a `make` target. Rare. |

If a scenario needs a helper that is not an HTTP call and not a payload, put it on
`BaseTest` — not in a utility class nobody will find.

## 2. The recipe

Adding a scenario to an endpoint that already has a service method is one step: write the
test method. Everything else is only for new endpoints.

1. **Model** — only if the endpoint returns or accepts a shape no record covers yet.
   `POST /booking` returns the `{bookingid, booking}` envelope (`BookingResponse`);
   `GET` and `PUT` return the bare `Booking`. Deserialize into the right one.
2. **Factory** — a `BookingFactory` method named `validX` / `invalidX`. Randomize
   identity fields with `faker`; hardcode whatever the assertion depends on.
3. **Service** — a method on the matching `*Service`, using `Specs.base()` for
   anonymous calls and `Specs.auth()` for authenticated ones. Name the unauthenticated
   variant `xWithoutAuthToken` — that is how the suite expresses "same call, no token".
4. **Test** — see conventions below.
5. **Run it**: `make test` from `rest-assured-tests/`. A green run you did not watch is not a pass.

Copy-paste starting points: `.claude/skills/add-api-test/references/templates.md`
(sits next to this file). Repo-wide orientation lives in `AGENTS.md`.

## 3. Test class conventions

```java
@Regression
public class BookingCreate extends BaseTest {
```

- **Always** `extends BaseTest`. It health-checks `/ping` before the class runs and
  deletes tracked bookings after each test.
- **Always** `@Regression` at class level — never per method. Regression is the full suite.
- Class name is `<Resource><Operation>`: `BookingCreate`, `BookingRead`, `BookingUpdate`,
  `BookingDelete`. No `Test` suffix.
- Class must sit **directly** in `com.booker.restful.tests`. Surefire includes
  `**/tests/*.java`, which does not match subpackages — a test in `tests/booking/` silently never runs.
- Any non-test class you add to `tests/` must be `abstract`, or Surefire will treat it as a test class.

## 4. Test method conventions

- Package-private `void`, no `public`.
- Named `should<ExpectedOutcome>`: `shouldCreateValidBookingWithPaidDeposit`,
  `shouldReturnNotFoundForUnknownBookingId`, `shouldNotUpdateWithoutAuthToken`.
  The name states the expectation, not the steps.
- One `@Smoke` per class at most, on the single call that answers "is this operation
  fundamentally working?". Smoke is the critical path, not the important tests.
- Comments explain **why**, never what. Existing examples worth matching:
  `// Not tracked for cleanup: deleting it is the test.`
  `// Re-reads instead of trusting the PUT response body, so the assertion covers persistence.`

## 5. Assertions: two tools, two jobs

- **Status codes and response shape** — REST Assured: `.then().statusCode(200)`.
- **Response values** — AssertJ: `assertThat(res.booking().depositpaid()).isTrue()`.

Never assert a body value through Hamcrest `body(...)` matchers; extract into a record and
use AssertJ. Records give `isEqualTo` for free, which is why full-object comparison is the
default assertion here:

```java
assertThat(found).isEqualTo(created);
```

Prefer that over field-by-field checks. Drop to individual fields only when the API
legitimately changes something (a generated id, a normalized value).

Response time is asserted globally by `Specs.TIMED` (`MAX_RESPONSE_TIME_MS`, default 5000ms).
Do not re-assert timing per test.

## 6. Cleanup is not optional

Every booking a test creates must be registered:

```java
int id = trackForCleanup(bookingService.create(booking)
        .then().statusCode(200)
        .extract().path("bookingid"));
```

`trackForCleanup` returns the id, so it wraps the extraction inline. The one exception is a
test whose subject *is* the deletion — say so in a comment, as `BookingDelete` does.

Cleanup runs unvalidated on purpose: a failing delete must never turn a passing test red.

## 7. When the API is wrong

Do not weaken the assertion to make it pass, and do not delete the test. Assert the behaviour
you argue is correct, then:

```java
@Test
@KnownIssue
@Disabled("API defect: a payload with null required fields returns 500 instead of 400. "
        + "A 5xx for malformed client input is a server-side error leaking out, not a validation response.")
void shouldRejectBookingWithMissingFields() {
```

The `@Disabled` reason must say (a) what the API actually does and (b) why the expected
behaviour is what you claim — especially where the restful-booker docs are silent. A reason
that only restates the method name is not a reason.

`make known-issue` runs these by deactivating JUnit's `DisabledCondition`; use it to check
whether a defect has been fixed upstream.

## 8. Configuration

Never hardcode a URL, credential, or timeout. Read it through `Config`, which resolves
System property -> env var -> `config/${env}.properties`. New setting = new accessor on
`Config` + a line in `local.properties.example`. Never put credentials in a committed file.

## 9. Before you call it done

- [ ] Test lives directly in `tests/`, extends `BaseTest`, class carries `@Regression`.
- [ ] Method named `should...`, package-private.
- [ ] Every created booking is `trackForCleanup`-ed, or the exception is commented.
- [ ] Status via REST Assured, values via AssertJ, whole records compared where possible.
- [ ] No request building inside the test and no assertion inside the service.
- [ ] `make test` run and green — or a new `@KnownIssue` + `@Disabled` with a real reason.
- [ ] Java, comments, and test names in English.

## Commands (from `rest-assured-tests/`)

| Command | Runs |
|---|---|
| `make test` | Everything |
| `make smoke` | `-Dgroups=smoke`, the critical path |
| `make regression` | `-Dgroups=regression`, the full tagged suite |
| `make known-issue` | The `@Disabled` defect tests, condition deactivated |
| `make clean` | Cleans `target/` |

Debug a single failure with `-DHTTP_LOG=true` to dump every request and response.
