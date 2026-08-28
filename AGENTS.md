# rest-assured-tests

API test suite for [restful-booker](https://restful-booker.herokuapp.com/apidoc/index.html).
REST Assured 5.5 + JUnit 5 + AssertJ + Datafaker, Java 17, Maven.

**Adding or changing a test? Read
[`.claude/skills/add-api-test/SKILL.md`](.claude/skills/add-api-test/SKILL.md) first** — it is the
full guide: recipe, conventions, and copy-paste templates. What follows is only the orientation.

## Layout

```
src/test/java/com/booker/restful/
  config/   Config (env resolution), Auth (cached token), Specs (request/response specs)
  model/    Records mirroring the API JSON
  data/     BookingFactory - payload builders
  service/  Thin REST Assured wrappers, return raw Response
  tags/     @Smoke @Regression @KnownIssue
  tests/    Test classes, extend BaseTest
```

Dependency direction is one-way: `tests` -> `service` -> `config`. A test never builds a
request; a service never asserts.

## Hard rules

- Test classes live **directly** in `tests/` — Surefire includes `**/tests/*.java`, which
  does not match subpackages. Non-test classes there must be `abstract`.
- Class name `<Resource><Operation>` (`BookingCreate`), no `Test` suffix. Methods
  `should<Outcome>`, package-private.
- `@Regression` at class level, `@Smoke` on at most one method per class.
- Status codes via REST Assured `.then().statusCode(n)`; response values via AssertJ
  `assertThat`. Compare whole records with `isEqualTo` where the API returns them unchanged.
- Every created booking goes through `trackForCleanup(id)` unless the test *is* the delete.
- API misbehaving? Keep the correct assertion and add `@KnownIssue` + `@Disabled("why")`.
  Never weaken an assertion to get green.
- No hardcoded URLs, credentials, or timeouts — read them through `Config`.
- Java, comments, and test names in English.

## Commands

`make test` | `make smoke` | `make regression` | `make known-issue` | `make report` |
`make report-smoke` | `make report-regression` | `make report-known-issue` | `make serve` |
`make clean`

The `report*` targets run the suite and write `target/allure-report/index.html`; `make serve`
opens the last one and runs no test. They use `-Dmaven.test.failure.ignore=true`, because a red
report is still a report, and they wipe `allure-results` first so a filtered report never shows
leftovers from a wider run. Any target takes a tag expression: `make report GROUPS='smoke |
regression'`.

Build with **JDK 17-25**: Allure's AspectJ weaver cannot read Java 26 class files and dies with
an unrelated-looking stack trace. The Makefile pins JDK 17 and `make help` prints which one is
in use, so `make` works in any shell. Running `mvn` directly is on you; the enforcer rule in the
pom fails the build with an explanation. What counts is the JDK running Maven (`mvn -version`),
not the one on `PATH`: Homebrew's maven picks the newest JDK installed.

Config comes from `src/test/resources/config/local.properties` (gitignored; copy
`local.properties.example`). Override any value with `-DKEY=value` or an env var.
`-DHTTP_LOG=true` dumps every request and response.

## Reporting and CI

[`.github/workflows/tests.yml`](.github/workflows/tests.yml) runs the suite on every push to
`main`, every pull request and on demand, then publishes the Allure report to GitHub Pages
from `main` only. Pull requests get the report as a build artifact plus a summary comment.

- Every REST Assured call is attached to the report by `AllureRestAssured`, registered in
  `Specs`. Use `@Step` on service or helper methods to add named steps; the AspectJ weaver
  is already wired into Surefire.
- `scripts/allure-metadata.sh` writes `environment.properties` and `executor.json` into the
  results directory, so the report states which API was tested, on which commit, by which run.
- Trends survive between runs because the workflow caches `allure-report/history` and feeds
  it back as `allure-results/history`. Only `main` writes that cache, so pull requests read
  the trend without distorting it.

Repository setup this expects: Pages source set to *GitHub Actions*, and the secrets
`AUTH_LOGIN` / `AUTH_PASSWORD`. `BASE_URL` defaults to the public instance and can be
overridden with a repository variable of the same name.
