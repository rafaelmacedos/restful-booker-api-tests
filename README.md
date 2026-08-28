# restful-booker API tests

API test suite for [restful-booker](https://restful-booker.herokuapp.com/apidoc/index.html),
the public booking API used as a practice target for test automation. It covers create, read,
update and delete over `/booking`, plus auth and a `/ping` health check.

REST Assured 5.5, JUnit 5, AssertJ, Datafaker, Allure. Java 17, Maven.

[![Tests](https://github.com/rafaelmacedos/restful-booker-api-tests/actions/workflows/tests.yml/badge.svg)](https://github.com/rafaelmacedos/restful-booker-api-tests/actions/workflows/tests.yml)
[Latest report](https://rafaelmacedos.github.io/restful-booker-api-tests/)

## Running it

You need a JDK between 17 and 25 (Allure's AspectJ weaver cannot read Java 26 class files)
and Maven. The Makefile pins JDK 17 on macOS, so `make` works in any shell.

```bash
cp src/test/resources/config/local.properties.example src/test/resources/config/local.properties
# fill in AUTH_LOGIN and AUTH_PASSWORD, then:
make test
```

That file is gitignored. Any value in it can be overridden with `-DKEY=value` or an env var,
which is how CI passes credentials.

| Command | What it does |
|---|---|
| `make test` | Runs everything |
| `make smoke` | Critical path only |
| `make regression` | Full tagged suite |
| `make known-issue` | The `@Disabled` tests that document API defects |
| `make report` | Runs the suite and builds the Allure report |
| `make serve` | Opens the last report without running anything |
| `make clean` | Cleans `target/` |

Any target accepts a tag expression: `make report GROUPS='smoke | regression'`.
Add `-DHTTP_LOG=true` to dump every request and response while debugging.

## Architecture

The suite is split into four layers, and the dependencies only ever flow one way:
`tests` to `service` to `config`, with `data` producing `model` payloads.

```
src/test/java/com/booker/restful/
  config/   Config (env resolution), Auth (cached token), Specs (request/response specs)
  model/    Records mirroring the API JSON
  data/     BookingFactory, payload builders
  service/  Thin REST Assured wrappers that return the raw Response
  tags/     @Smoke, @Regression, @KnownIssue
  tests/    Test classes, all extending BaseTest
```

It is the API equivalent of the page object pattern: the HTTP details live in one place and
the tests describe behaviour. Each layer earns its place.

**`config`** is the only thing that knows where the API is and who it logs in as. `Config`
resolves a value from a system property, then an env var, then `config/${env}.properties`,
so the same suite runs against a local instance or CI with a flag and no code change.
`Specs` holds the shared request and response specs (base URI, content type, response time
budget, the Allure filter), which is why no test repeats them. `Auth` caches the token so
fifteen tests do not log in fifteen times.

**`model`** mirrors the API JSON as records. That buys `isEqualTo` for free, so a test can
compare an entire booking in one line instead of asserting field by field, and a field the
API silently drops still fails the test.

**`data`** builds the payloads. Identity fields are randomized with Datafaker because the
target is a shared public instance and fixed names collide with whatever someone else left
behind. A test asks for `validBookingWithPaidDeposit()` and stays about the scenario, not
about JSON.

**`service`** is a thin wrapper per endpoint that returns the raw `Response` and asserts
nothing. When a route or a header changes, one method changes. Because it returns the raw
response, each test still decides what a correct answer looks like for its own case.

**`tests`** holds intent and assertions only. Every class extends `BaseTest`, which pings
`/ping` before the class runs (so a sleeping API reports as unavailable instead of fifteen
confusing failures) and deletes whatever the test created afterwards. `tags` exists so the
same suite can be sliced into a fast critical path, a full run, or the known defects,
without maintaining separate test sets.

The rule that keeps it honest: a test never builds a request, and a service never asserts.
When that holds, a failure points straight at its cause. A red assertion means the API
behaved differently, not that the test set itself up wrong.

## GitHub Actions

[`.github/workflows/tests.yml`](.github/workflows/tests.yml) runs the suite on every push to
`main`, on every pull request and on demand. It wakes the API up first, since the free dyno
sleeps and a cold start would look like the whole suite failing.

Pull requests get the Allure report as an artifact and a comment listing what broke.
Pushes to `main` publish the report to GitHub Pages. Trends survive across runs because the
report history is cached, and only `main` writes to that cache, so a pull request reads the
trend without distorting it.

The workflow expects Pages set to *GitHub Actions* and the secrets `AUTH_LOGIN` and
`AUTH_PASSWORD`. `BASE_URL` falls back to the public instance.

## Skills

[`.claude/skills/add-api-test/`](.claude/skills/add-api-test/) is a Claude Code skill holding
the conventions for this suite: which layer a change belongs in, naming, how to assert,
cleanup, what to do when the API itself is wrong, and copy-paste templates. It loads
automatically when an agent adds or reviews a test here, and it reads fine on its own if you
are writing one by hand.

It is committed on purpose, same as the Makefile. [AGENTS.md](AGENTS.md) is the short version
and points at it.
