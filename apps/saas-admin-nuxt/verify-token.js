#!/usr/bin/env node

/**
 * Quick script to decode and verify JWT token audience
 * Usage: node verify-token.js <your-jwt-token>
 */

function decodeJWT(token) {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) {
      console.error('❌ Invalid JWT format');
      return null;
    }

    const payload = JSON.parse(
      Buffer.from(parts[1], 'base64').toString('utf8')
    );
    
    return payload;
  } catch (error) {
    console.error('❌ Failed to decode JWT:', error.message);
    return null;
  }
}

const token = process.argv[2];

if (!token) {
  console.log('Usage: node verify-token.js <your-jwt-token>');
  console.log('\nOr check localStorage in browser:');
  console.log('  localStorage.getItem("saas_auth_token")');
  process.exit(1);
}

console.log('🔍 Decoding JWT token...\n');

const payload = decodeJWT(token);

if (payload) {
  console.log('✅ Token decoded successfully!\n');
  console.log('Token Details:');
  console.log('─────────────────────────────────────');
  console.log(`Audience (aud):  ${payload.aud}`);
  console.log(`Issuer (iss):    ${payload.iss}`);
  console.log(`Subject (sub):   ${payload.sub}`);
  console.log(`Name:            ${payload.name}`);
  console.log(`Email:           ${payload.email}`);
  console.log(`Roles:           ${JSON.stringify(payload.roles)}`);
  console.log(`Issued At:       ${new Date(payload.iat * 1000).toISOString()}`);
  console.log(`Expires At:      ${new Date(payload.exp * 1000).toISOString()}`);
  console.log('─────────────────────────────────────\n');
  
  if (payload.aud === 'saas-manager') {
    console.log('✅ Correct audience: saas-manager');
  } else {
    console.log(`❌ Wrong audience: ${payload.aud}`);
    console.log('   Expected: saas-manager');
    console.log('\n💡 Solution: Clear localStorage and login again to get a new token');
  }
  
  const now = Date.now() / 1000;
  if (payload.exp < now) {
    console.log('⚠️  Token has expired!');
  } else {
    const hoursLeft = Math.floor((payload.exp - now) / 3600);
    console.log(`✅ Token is valid for ${hoursLeft} more hours`);
  }
}
