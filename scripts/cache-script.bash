#!/bin/bash
BASE_URL="http://localhost:8080/api/v1"
TOKEN="paste-your-real-token-here"
RUNS=20

echo "Warming up..."
for i in $(seq 1 3); do
  curl -s -o /dev/null "$BASE_URL/summaries/monthly" -H "Authorization: Bearer $TOKEN"
done

echo "Measuring $RUNS runs..."
TOTAL=0
for i in $(seq 1 $RUNS); do
  START=$(date +%s%N)
  curl -s -o /dev/null "$BASE_URL/summaries/monthly" -H "Authorization: Bearer $TOKEN"
  END=$(date +%s%N)
  MS=$(( (END - START) / 1000000 ))
  echo "Run $i: ${MS}ms"
  TOTAL=$((TOTAL + MS))
done

echo "Average: $((TOTAL / RUNS))ms"