# Quick Start Guide

Get the Multi-Tenant Clinic Management System running in 5 minutes!

## Prerequisites

✅ Docker Desktop installed and running  
✅ Java 17+ (for local API)  
✅ Node.js 18+ (for local frontend)

## Fastest Way to Run

### Option 1: Everything in Docker (Easiest)

```bash
# 1. Make sure Docker Desktop is running

# 2. Start all services
./start-all.sh

# 3. Wait ~2 minutes for services to start

# 4. Open SAAS Admin Panel
open http://localhost:3002
```

**That's it!** 🎉

### Option 2: Local Development (Fastest for Development)

```bash
# 1. Start database
./start-local.sh

# 2. Start API (new terminal)
cd apps/api
./mvnw spring-boot:run

# 3. Start SAAS Admin Panel (new terminal)
cd apps/saas-admin-nuxt
npm install
npm run dev

# 4. Open browser
open http://localhost:3002
```

## What You Get

Once running, you have access to:

- 🎨 **SAAS Admin Panel** - http://localhost:3002
  - Manage tenants
  - Monitor system health
  - View analytics
  - Full accessibility support (WCAG AA)
  
- 🏥 **Tenant Admin Panel** - http://localhost:3000
  - Clinic management
  - Appointments
  - Patient records
  
- 🌐 **Public Web App** - http://localhost:3001
  - Patient portal
  - Online booking
  
- 🔌 **API** - http://localhost:8080
  - REST API
  - Swagger docs at `/swagger-ui.html`

## Test Accessibility Features

The SAAS Admin Panel includes comprehensive accessibility:

```bash
# Visit the accessibility test page
open http://localhost:3002/accessibility-test

# Try keyboard navigation
# Press Tab to see skip link
# Navigate with Tab, Enter, Space, Arrow keys

# Test with screen reader (macOS)
# Enable VoiceOver: Cmd + F5
```

## Common Commands

```bash
# View logs (Docker)
docker compose logs -f

# Stop everything (Docker)
docker compose down

# Restart a service (Docker)
docker compose restart saas-admin

# Stop database (Local)
docker stop clinic-postgres
```

## Troubleshooting

### Docker not running?
```bash
# Start Docker Desktop app
# Wait for whale icon in menu bar
```

### Port already in use?
```bash
# Kill process on port 3002
lsof -ti:3002 | xargs kill -9
```

### Build fails?
```bash
# Clean and rebuild
docker compose down -v
docker builder prune -af
docker compose up --build
```

## Next Steps

1. ✅ System is running
2. 📝 Create SAAS manager account (via API)
3. 🔐 Login to SAAS Admin Panel
4. 🏢 Create your first tenant
5. ⌨️ Test accessibility features
6. 🎨 Configure tenant branding
7. 📊 Monitor system health

## Need More Help?

- 📖 Full guide: `RUNNING.md`
- 🐛 Troubleshooting: `apps/saas-admin-nuxt/docs/TROUBLESHOOTING.md`
- ♿ Accessibility: `apps/saas-admin-nuxt/docs/accessibility.md`
- 🐳 Docker fix: `DOCKER_FIX_SUMMARY.md`

## Pro Tips

💡 **For development**: Use local setup (Option 2) - faster hot reload  
💡 **For testing**: Use Docker (Option 1) - consistent environment  
💡 **For production**: See `apps/saas-admin-nuxt/DEPLOYMENT.md`

---

**Ready to go!** Start with `./start-all.sh` or `./start-local.sh` 🚀
