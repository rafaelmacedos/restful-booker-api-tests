MVN = mvn -f pom.xml

.DEFAULT_GOAL := help

help:
	@echo "make test        - run every test"
	@echo "make smoke       - run the critical path only"
	@echo "make regression  - run the full tagged suite"
	@echo "make known-issue - run the disabled tests that document API defects"
	@echo "make clean       - clean the target directory"

test:
	$(MVN) test

smoke:
	$(MVN) test -Dgroups=smoke

regression:
	$(MVN) test -Dgroups=regression

# @Disabled outranks tags, so the condition has to be deactivated for these to actually run.
known-issue:
	$(MVN) test -Dgroups=known-issue \
		-Djunit.jupiter.conditions.deactivate=org.junit.jupiter.engine.extension.DisabledCondition

clean:
	$(MVN) clean

.PHONY: help test smoke regression known-issue clean
