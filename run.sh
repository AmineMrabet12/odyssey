#!/bin/bash
# Run Odyssey with Java 21 (required for Lombok compatibility)
JAVA_HOME=/Users/mohamedaminemrabet/Library/Java/JavaVirtualMachines/corretto-21.0.3/Contents/Home
export JAVA_HOME

echo "Building Odyssey..."
mvn clean package -DskipTests -q
echo "Starting Odyssey on http://localhost:8080 ..."
java -jar target/odyssey-1.0.0.jar --spring.profiles.active=dev
