MVN = mvn -f pom.xml
ALLURE_RESULTS = target/allure-results
ALLURE_REPORT = target/allure-report

# Allure's AspectJ weaver breaks on Java 26 and Homebrew's maven runs on the newest JDK
# installed, so pin 17. Falls back to the environment where java_home does not exist.
JAVA_HOME := $(shell /usr/libexec/java_home -v 17 2>/dev/null || echo $$JAVA_HOME)
export JAVA_HOME

# @Disabled outranks tags, so it has to be switched off for those tests to run.
DEACTIVATE_DISABLED = -Djunit.jupiter.conditions.deactivate=org.junit.jupiter.engine.extension.DisabledCondition

GROUP_FLAG = $(if $(GROUPS),-Dgroups=$(GROUPS))

.DEFAULT_GOAL := help

help:
	@echo "make test               - run every test"
	@echo "make smoke              - run the critical path only"
	@echo "make regression         - run the full tagged suite"
	@echo "make known-issue        - run the disabled tests that document API defects"
	@echo "make report             - run every test and generate the Allure report"
	@echo "make report-smoke       - report for the smoke suite only"
	@echo "make report-regression  - report for the regression suite only"
	@echo "make report-known-issue - report for the known issues only"
	@echo "make serve              - open the last report, without running any test"
	@echo "make clean              - clean the target directory"
	@echo
	@echo "Add GROUPS=<tag expression>, e.g. make report GROUPS='smoke | regression'"
	@echo "JDK in use: $(JAVA_HOME)"

test:
	$(MVN) test $(GROUP_FLAG)

smoke:
	$(MVN) test -Dgroups=smoke

regression:
	$(MVN) test -Dgroups=regression

known-issue:
	$(MVN) test -Dgroups=known-issue $(DEACTIVATE_DISABLED)

# A failing test must not stop the report, and old results must not leak into a filtered one.
report:
	rm -rf $(ALLURE_RESULTS)
	$(MVN) test -Dmaven.test.failure.ignore=true $(GROUP_FLAG) $(EXTRA_FLAGS)
	./scripts/allure-metadata.sh
	$(MVN) allure:report
	@echo "Report at $(ALLURE_REPORT)/index.html - open it with 'make serve'"

report-smoke:
	$(MAKE) report GROUPS=smoke

report-regression:
	$(MAKE) report GROUPS=regression

report-known-issue:
	$(MAKE) report GROUPS=known-issue EXTRA_FLAGS="$(DEACTIVATE_DISABLED)"

serve:
	@test -d $(ALLURE_RESULTS) || { echo "No report yet - run 'make report' first."; exit 1; }
	$(MVN) allure:serve

clean:
	$(MVN) clean

.PHONY: help test smoke regression known-issue report report-smoke report-regression \
        report-known-issue serve clean
