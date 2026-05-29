# ============================================
# Makefile for Paxos Consensus Project
# ============================================

# Default target (compile the project)
all: compile

# Compile Java code using Maven
compile:
	mvn clean compile

# Run all Paxos test scenarios (using scripts/run_tests.sh)
run-tests:
	chmod +x scripts/run_tests.sh
	bash scripts/run_tests.sh

# Clean build files and logs
clean:
	mvn clean
	rm -rf logs/*.txt logs/*.log target

.PHONY: all compile run-tests clean
