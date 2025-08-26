#!/bin/bash

echo "Stopping user-service, audit-service, and gateway..."

# Stop services by port
for PORT in 8081 8082 8080; do
  PID=$(lsof -ti tcp:$PORT)
  if [ -n "$PID" ]; then
    echo "Stopping service on port $PORT (PID: $PID)"
    kill $PID
  else
    echo "No service running on port $PORT"
  fi
done

echo "All services stopped."