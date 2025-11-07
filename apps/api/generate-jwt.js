const jwt = require('jsonwebtoken');
const fs = require('fs');

// Read the private key
const privateKey = fs.readFileSync('./src/main/resources/keys/staff_private.pem', 'utf8');

// Create JWT payload
const payload = {
  sub: "1",
  aud: "staff", 
  roles: ["ROLE_ADMIN"],
  iss: "https://api.example-clinic.com",
  name: "Clinic Admin",
  email: "admin@clinic.com",
  exp: Math.floor(Date.now() / 1000) + (30 * 24 * 60 * 60), // 30 days
  iat: Math.floor(Date.now() / 1000)
};

// Generate JWT
const token = jwt.sign(payload, privateKey, { algorithm: 'RS256' });

console.log('Generated JWT Token:');
console.log(token);
console.log('\nUse this token in your Authorization header:');
console.log(`Authorization: Bearer ${token}`);