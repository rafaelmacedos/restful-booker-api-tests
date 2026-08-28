#!/usr/bin/env bash
#
# Writes the two metadata files Allure picks up from the results directory:
#   environment.properties - the "Environment" widget (which API was tested, with what)
#   executor.json          - the "Executor" link back to the CI run that produced the report
#
# Runs from Maven (make allure-report) and from the CI workflow, so a local report shows
# the same context as the published one.
set -euo pipefail

results_dir="${ALLURE_RESULTS_DIR:-target/allure-results}"
properties_file="src/test/resources/config/${ENV:-local}.properties"

# Same precedence Config.get() uses: env var first, properties file second.
from_properties() {
  [ -f "$properties_file" ] || return 0
  sed -n "s/^$1=//p" "$properties_file" | head -1
}

base_url="${BASE_URL:-$(from_properties BASE_URL)}"
max_response_time="${MAX_RESPONSE_TIME_MS:-$(from_properties MAX_RESPONSE_TIME_MS)}"

mkdir -p "$results_dir"

{
  echo "API.Base.URL=${base_url:-unknown}"
  echo "Max.Response.Time.Ms=${max_response_time:-5000}"
  echo "Java=$(java -version 2>&1 | head -1 | cut -d'"' -f2)"
  echo "Branch=${GITHUB_REF_NAME:-$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo unknown)}"
  echo "Commit=$(git rev-parse --short HEAD 2>/dev/null || echo unknown)"
  echo "Run.By=${GITHUB_ACTOR:-$(whoami)}"
} > "$results_dir/environment.properties"

# Only CI has a run to link to; locally the widget would just show dead links.
if [ "${GITHUB_ACTIONS:-}" = "true" ]; then
  run_url="${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/actions/runs/${GITHUB_RUN_ID}"
  cat > "$results_dir/executor.json" <<JSON
{
  "name": "GitHub Actions",
  "type": "github",
  "reportName": "Allure report",
  "url": "${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}",
  "buildOrder": ${GITHUB_RUN_NUMBER},
  "buildName": "${GITHUB_WORKFLOW} #${GITHUB_RUN_NUMBER}",
  "buildUrl": "${run_url}",
  "reportUrl": "${ALLURE_REPORT_URL:-$run_url}"
}
JSON
fi

echo "Allure metadata written to $results_dir"
