#!/bin/bash

# Load environment variables from .env.clean
set -a
source .env.clean
set +a

# Start the application
./gradlew bootRun