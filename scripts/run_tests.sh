#!/usr/bin/env bash
set -euo pipefail

# =========================================================
# Paxos Council - Automated Test Harness
# Author: Nethmi Ranathunga
# Description:
#   Runs all Paxos consensus test scenarios (Ideal, Concurrent, Fault-Tolerant)
#   and compiles results into organized log files for marking evidence.
# =========================================================

mkdir -p logs

echo ""
echo "======================================"
echo "   PAXOS CONSENSUS TEST SUITE START"
echo "======================================"
echo "[INFO] Started at: $(date)"
echo ""

# ---------------------------------------------------------
# Step 1 – Environment Information
# ---------------------------------------------------------
echo "[INFO] Java Version:"
java -version
echo ""
echo "[INFO] Maven Version:"
mvn -v
echo ""

# ---------------------------------------------------------
# Step 2 – Clean Build
# ---------------------------------------------------------
echo "[INFO] Compiling source files..."
mvn clean compile -q
echo "[INFO] Build successful."
echo ""

# ---------------------------------------------------------
# Step 3 – Run Scenario 1 (Ideal Network)
# ---------------------------------------------------------
echo ""
echo "--------------------------------------"
echo " Running Scenario 1: Ideal Network"
echo "--------------------------------------"
bash scripts/ideal_scenario.sh | tee logs/ideal_scenario_run.log

# ---------------------------------------------------------
# Step 4 – Run Scenario 2 (Concurrent Proposals)
# ---------------------------------------------------------
echo ""
echo "--------------------------------------"
echo " Running Scenario 2: Concurrent Proposals"
echo "--------------------------------------"
bash scripts/concurrent_scenario.sh | tee logs/concurrent_scenario_run.log

# ---------------------------------------------------------
# Step 5 – Run Scenario 3 (Fault Tolerance)
# ---------------------------------------------------------
echo ""
echo "--------------------------------------"
echo " Running Scenario 3: Fault Tolerance"
echo "--------------------------------------"
bash scripts/fault_tolerance_scenario.sh | tee logs/fault_tolerance_scenario_run.log

# ---------------------------------------------------------
# Step 6 – Merge Logs for Submission Evidence
# ---------------------------------------------------------
cat logs/*_scenario_output.txt > logs/all_scenarios_summary.txt || true

echo ""
echo "======================================"
echo "   ALL TESTS COMPLETED SUCCESSFULLY"
echo "======================================"
echo "[INFO] Combined summary saved at: logs/all_scenarios_summary.txt"
echo "[INFO] Finished at: $(date)"
echo ""
