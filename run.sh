#!/bin/bash

set -e

USER_SERVICE_DIR="user-service"
AUDIT_SERVICE_DIR="audit-service"
GATEWAY_DIR="gateway"

# Start user-service
echo "Starting user-service..."
(cd "$USER_SERVICE_DIR" && mvn spring-boot:run > ../user-service.log 2>&1 &)
USER_PID=$!

# Start audit-service
echo "Starting audit-service..."
(cd "$AUDIT_SERVICE_DIR" && mvn spring-boot:run > ../audit-service.log 2>&1 &)
AUDIT_PID=$!

# Wait for user-service to be up
echo "Waiting for user-service on port 8081..."
until nc -z localhost 8081; do sleep 2; done

# Wait for audit-service to be up
echo "Waiting for audit-service on port 8082..."
until nc -z localhost 8082; do sleep 2; done

# Start gateway
echo "Starting gateway..."
(cd "$GATEWAY_DIR" && mvn spring-boot:run > ../gateway.log 2>&1 &)
GATEWAY_PID=$!

echo "All services started."
echo "PIDs: user-service=$USER_PID, audit-service=$AUDIT_PID, gateway=$GATEWAY_PID"