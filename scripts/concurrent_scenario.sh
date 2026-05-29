#!/usr/bin/env bash
set -euo pipefail

# =========================================================
# Scenario 2: Concurrent Proposals
# Author: Nethmi Ranathunga
# Two proposers (M1 and M8) initiate proposals at the same time.
# Expected: Paxos resolves the conflict and reaches a single consensus.
# =========================================================

echo ""
echo "==========================================="
echo "   Scenario 2: Concurrent Proposals"
echo "   Description: M1 proposes M1, M8 proposes M8 concurrently"
echo "==========================================="
echo ""

# Step 1 – Prepare logs folder
mkdir -p logs

# Step 2 – Compile project (fresh build each run)
echo "[INFO] Compiling project..."
mvn -q clean compile

# Step 3 – Run Paxos simulation for Scenario 2
echo "[INFO] Starting Scenario 2 (Concurrent Proposals)..."
echo "=== $(date): Starting Scenario 2 Run ===" > logs/concurrent_scenario_output.txt
mvn -q exec:java -Dexec.mainClass=paxos.Main -Dexec.args="2" | tee -a logs/concurrent_scenario_output.txt

# Step 4 – Simple check for consensus in output
if grep -q "CONSENSUS" logs/concurrent_scenario_output.txt; then
  echo ""
  echo "Consensus reached successfully! Paxos resolved the conflict."
else
  echo ""
  echo "Consensus not detected — check logs/concurrent_scenario_output.txt"
fi

# Step 5 – Final confirmation
echo ""
echo "[INFO] Logs saved to logs/concurrent_scenario_output.txt"
echo "[INFO] Scenario 2 completed at $(date)"
