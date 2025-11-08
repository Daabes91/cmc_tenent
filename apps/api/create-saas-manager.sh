#!/bin/bash

# Script to create a SAAS Manager with proper BCrypt password hash
# Usage: ./create-saas-manager.sh [email] [password] [full_name]

set -e

# Default values
EMAIL="${1:-admin@saas.com}"
PASSWORD="${2:-Admin123!}"
FULL_NAME="${3:-SAAS Administrator}"

echo "=========================================="
echo "Creating SAAS Manager"
echo "=========================================="
echo "Email: $EMAIL"
echo "Password: $PASSWORD"
echo "Full Name: $FULL_NAME"
echo ""

# Database connection details
DB_HOST="localhost"
DB_PORT="5442"
DB_NAME="clinic_multi_tenant"
DB_USER="clinic"
DB_PASSWORD="clinic_password"

# Create a temporary Java class to generate BCrypt hash
TEMP_DIR=$(mktemp -d)
JAVA_FILE="$TEMP_DIR/HashGenerator.java"

cat > "$JAVA_FILE" << 'JAVA_EOF'
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGenerator {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java HashGenerator <password>");
            System.exit(1);
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(args[0]);
        System.out.println(hash);
    }
}
JAVA_EOF

echo "Generating BCrypt hash..."

# Find Spring Security Crypto JAR
SPRING_SECURITY_JAR=$(find ~/.gradle/caches/modules-2/files-2.1/org.springframework.security/spring-security-crypto -name "*.jar" 2>/dev/null | head -1)

if [ -z "$SPRING_SECURITY_JAR" ]; then
    echo "Error: Spring Security Crypto JAR not found in Gradle cache"
    echo "Please run './gradlew build' first to download dependencies"
    rm -rf "$TEMP_DIR"
    exit 1
fi

# Compile and run the hash generator
cd "$TEMP_DIR"
javac -cp "$SPRING_SECURITY_JAR" HashGenerator.java 2>/dev/null

if [ $? -ne 0 ]; then
    echo "Error: Failed to compile hash generator"
    rm -rf "$TEMP_DIR"
    exit 1
fi

PASSWORD_HASH=$(java -cp ".:$SPRING_SECURITY_JAR" HashGenerator "$PASSWORD")

if [ -z "$PASSWORD_HASH" ]; then
    echo "Error: Failed to generate password hash"
    rm -rf "$TEMP_DIR"
    exit 1
fi

echo "Hash generated successfully!"
echo ""

# Clean up temp directory
rm -rf "$TEMP_DIR"

# Create SQL script
SQL_SCRIPT=$(cat << EOF
-- Delete existing SAAS Manager with this email (if any)
DELETE FROM saas_managers WHERE email = '$EMAIL';

-- Insert new SAAS Manager
INSERT INTO saas_managers (email, full_name, password_hash, status, created_at, updated_at)
VALUES (
    '$EMAIL',
    '$FULL_NAME',
    '$PASSWORD_HASH',
    'ACTIVE',
    NOW(),
    NOW()
);

-- Verify insertion
SELECT id, email, full_name, status, created_at 
FROM saas_managers 
WHERE email = '$EMAIL';
EOF
)

echo "Inserting SAAS Manager into database..."
echo ""

# Execute SQL
PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" << EOF
$SQL_SCRIPT
EOF

if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "✓ SAAS Manager created successfully!"
    echo "=========================================="
    echo ""
    echo "You can now login with:"
    echo "  Email: $EMAIL"
    echo "  Password: $PASSWORD"
    echo ""
    echo "Test the login:"
    echo "  curl -X POST http://localhost:8080/saas/auth/login \\"
    echo "    -H 'Content-Type: application/json' \\"
    echo "    -d '{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}'"
    echo ""
else
    echo ""
    echo "Error: Failed to insert SAAS Manager into database"
    exit 1
fi
