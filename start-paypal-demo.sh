#!/bin/bash

echo "🚀 Starting PayPal Integration Demo"
echo "=================================="

# Check if PayPal credentials are set
if grep -q "YOUR_SANDBOX_CLIENT_ID_HERE" apps/api/.env.local; then
    echo "❌ PayPal credentials not configured!"
    echo ""
    echo "Please follow these steps:"
    echo "1. Go to https://developer.paypal.com/"
    echo "2. Create a sandbox app"
    echo "3. Copy your Client ID and Client Secret"
    echo "4. Update apps/api/.env.local with your credentials"
    echo ""
    echo "See setup-paypal.md for detailed instructions"
    exit 1
fi

echo "✅ PayPal credentials configured"

# Start backend
echo "🔧 Starting Backend API..."
cd apps/api
./gradlew bootRun &
API_PID=$!
cd ..

# Wait for API to start
echo "⏳ Waiting for API to start..."
sleep 10

# Start admin panel
echo "🔧 Starting Admin Panel..."
cd apps/admin-nuxt
npm run dev &
ADMIN_PID=$!
cd ..

# Start frontend
echo "🔧 Starting Frontend..."
cd apps/web-next
npm run dev &
FRONTEND_PID=$!
cd ..

echo ""
echo "🎉 All services started!"
echo "=================================="
echo "📊 Admin Panel: http://localhost:3000"
echo "🌐 Frontend: http://localhost:3001"
echo "🔌 API: http://localhost:8080"
echo ""
echo "Next steps:"
echo "1. Configure PayPal settings in admin: http://localhost:3000/clinic-settings"
echo "2. Test booking flow: http://localhost:3001"
echo ""
echo "Press Ctrl+C to stop all services"

# Wait for interrupt
trap "echo 'Stopping services...'; kill $API_PID $ADMIN_PID $FRONTEND_PID; exit" INT
wait