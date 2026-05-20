#!/bin/bash
# setup.sh - Complete local setup script
# Usage: bash setup.sh

set -e

echo "================================"
echo "Journal Backend Setup Script"
echo "================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check Prerequisites
echo -e "${YELLOW}Checking prerequisites...${NC}"

if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ Java 17+ not found. Install Java first.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Java $(java -version 2>&1 | grep 'version' | awk -F'"' '{print $2}')${NC}"

if ! command -v mvn &> /dev/null; then
    echo -e "${RED}❌ Maven not found. Install Maven first.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Maven installed${NC}"

if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker not found. Install Docker first.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Docker installed${NC}"

# Start Docker services
echo -e "${YELLOW}Starting Docker services (MongoDB, Redis, Kafka, Zookeeper)...${NC}"
docker-compose up -d

echo -e "${GREEN}✓ Services started${NC}"
echo "  - MongoDB: localhost:27017"
echo "  - Redis: localhost:6379"
echo "  - Kafka: localhost:9092"
echo "  - Zookeeper: localhost:2181"

# Wait for services to be ready
echo -e "${YELLOW}Waiting for services to be ready (30 seconds)...${NC}"
sleep 30

# Test MongoDB connection
echo -e "${YELLOW}Testing MongoDB connection...${NC}"
if docker exec journal-mongo mongosh --eval "db.adminCommand('ping')" &> /dev/null; then
    echo -e "${GREEN}✓ MongoDB is ready${NC}"
else
    echo -e "${RED}⚠ MongoDB might not be ready. Check: docker logs journal-mongo${NC}"
fi

# Test Redis connection
echo -e "${YELLOW}Testing Redis connection...${NC}"
if docker exec redis redis-cli ping | grep -q PONG; then
    echo -e "${GREEN}✓ Redis is ready${NC}"
else
    echo -e "${RED}⚠ Redis might not be ready. Check: docker logs redis${NC}"
fi

# Build the application
echo -e "${YELLOW}Building application with Maven...${NC}"
mvn clean package -DskipTests

if [ -f "target/Backend-0.0.1-SNAPSHOT.jar" ]; then
    echo -e "${GREEN}✓ Build successful${NC}"
else
    echo -e "${RED}❌ Build failed${NC}"
    exit 1
fi

# Instructions for running
echo ""
echo -e "${GREEN}================================"
echo "Setup Complete! 🎉"
echo "================================${NC}"
echo ""
echo "Next steps:"
echo ""
echo "1️⃣  Start the application:"
echo "    mvn -Dspring-boot.run.profiles=dev spring-boot:run"
echo ""
echo "    OR"
echo ""
echo "    java -jar target/Backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev"
echo ""
echo "2️⃣  Access the application:"
echo "    - API: http://localhost:8081"
echo "    - Swagger UI: http://localhost:8081/swagger-ui.html"
echo "    - OpenAPI JSON: http://localhost:8081/v3/api-docs"
echo ""
echo "3️⃣  Test Kafka:"
echo "    - Consumer logs: docker logs -f kafka"
echo "    - Register a user and check welcome message"
echo ""
echo "4️⃣  Useful Docker commands:"
echo "    - View logs: docker logs -f journal-mongo"
echo "    - Stop services: docker-compose down"
echo "    - Reset everything: docker-compose down -v && docker-compose up -d"
echo ""
echo -e "${YELLOW}Note: Dev credentials are already set in application-dev.yml${NC}"
echo ""

