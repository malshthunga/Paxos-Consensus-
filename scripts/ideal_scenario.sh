#!/bin/bash
# ==============================================
# Scenario 1: Ideal Network
# All 9 council members run with reliable profiles.
# Expected: Consensus reached quickly and correctly.
# ==============================================

echo ""
echo "=== Scenario 1: Ideal Network ==="
mkdir -p logs

# Step 1 – Compile everything
mvn -q clean compile

# Step 2 – Run the Paxos simulation
# Main.java automatically starts all 9 members and initiates M4 → M5 proposal
mvn -q exec:java -Dexec.mainClass=paxos.Main -Dexec.args="1"  | tee logs/ideal_scenario_output.txt

# Step 3 – Simple check for success
if grep -q "CONSENSUS" logs/ideal_scenario_output.txt; then
  echo " Consensus reached successfully!"
else
  echo " Consensus not detected — check logs/ideal_scenario_output.txt"
fi

echo ""
echo "[INFO] Logs saved to logs/ideal_scenario_output.txt"
