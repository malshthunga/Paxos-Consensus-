#!/usr/bin/env bash
set -euo pipefail

# =========================================================
# Scenario 3: Fault Tolerance
# Mixed profiles: M1(reliable), M2(latent), M3(failure), M4–M9(standard)
# Expected: Consensus achieved in all subtests despite latency and failures.
# Author: Nethmi Ranathunga
# =========================================================

echo ""
echo "==========================================="
echo "   Scenario 3: Fault Tolerance"
echo "   Description: M4→M5, M2→M6, M3→M7 (fails)"
echo "==========================================="
echo ""

# Step 1 – Prepare logs folder
mkdir -p logs

# Step 2 – Compile project
echo "[INFO] Compiling project..."
mvn -q clean compile

# Step 3 – Run Paxos simulation for Scenario 3
echo "[INFO] Starting Scenario 3 (Fault Tolerance)..."
echo "=== $(date): Starting Scenario 3 Run ===" > logs/fault_tolerance_output.txt
mvn -q exec:java -Dexec.mainClass=paxos.Main -Dexec.args="3" | tee -a logs/fault_tolerance_output.txt

# Step 4 – Verify that consensus was achieved
if grep -q "CONSENSUS" logs/fault_tolerance_output.txt; then
  echo ""
  echo "Consensus reached successfully under mixed/failure conditions!"
else
  echo ""
  echo "Consensus not detected — check logs/fault_tolerance_output.txt"
fi

# Step 5 – Completion message
echo ""
echo "[INFO] Logs saved to logs/fault_tolerance_output.txt"
echo "[INFO] Scenario 3 completed at $(date)"
echo "==========================================="
