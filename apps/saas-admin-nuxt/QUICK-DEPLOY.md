# SAAS Manager Admin Panel - Quick Deploy Reference

Quick reference for common deployment commands and operations.

## Quick Start

### Development
```bash
# Start development server
pnpm dev

# Access at http://localhost:3002/saas-admin
```

### Docker - Quick Deploy
```bash
# One-command deployment
./deploy.sh

# Or step by step:
./deploy.sh build    # Build image only
./deploy.sh start    # Start container only
./deploy.sh stop     # Stop container
./deploy.sh restart  # Restart container
./deploy.sh logs     # View logs
```

## Docker Commands

### Build Image
```bash
# Development
docker build \
  --build-arg NUXT_PUBLIC_API_BASE=http://localhost:8080 \
  --build-arg NUXT_PUBLIC_SAAS_API_BASE=http://localhost:8080/saas \
  -f apps/saas-admin-nuxt/Dockerfile \
  -t clinic-saas-admin:dev .

# Production
docker build \
  --build-arg NUXT_PUBLIC_API_BASE=https://api.example.com \
  --build-arg NUXT_PUBLIC_SAAS_API_BASE=https://api.example.com/saas \
  -f apps/saas-admin-nuxt/Dockerfile \
  -t clinic-saas-admin:latest .
```

### Run Container
```bash
# Basic run
docker run -d \
  --name clinic-saas-admin \
  -p 3002:3002 \
  --env-file apps/saas-admin-nuxt/.env \
  clinic-saas-admin:latest

# With restart policy
docker run -d \
  --name clinic-saas-admin \
  -p 3002:3002 \
  --env-file apps/saas-admin-nuxt/.env \
  --restart unless-stopped \
  clinic-saas-admin:latest
```

### Container Management
```bash
# Start/Stop/Restart
docker start clinic-saas-admin
docker stop clinic-saas-admin
docker restart clinic-saas-admin

# Remove container
docker rm -f clinic-saas-admin

# View logs
docker logs clinic-saas-admin
docker logs -f clinic-saas-admin          # Follow logs
docker logs --tail 100 clinic-saas-admin  # Last 100 lines
docker logs --since 1h clinic-saas-admin  # Last hour

# Execute commands in container
docker exec -it clinic-saas-admin sh
docker exec clinic-saas-admin node -v

# Check container stats
docker stats clinic-saas-admin

# Inspect container
docker inspect clinic-saas-admin
```

## Docker Compose

### Development Environment
```bash
cd apps/saas-admin-nuxt

# Start all services
docker-compose -f docker-compose.dev.yml up -d

# View logs
docker-compose -f docker-compose.dev.yml logs -f

# Stop services
docker-compose -f docker-compose.dev.yml down

# Rebuild and restart
docker-compose -f docker-compose.dev.yml up -d --build
```

### Production Environment
```bash
cd apps/saas-admin-nuxt

# Start with production config
docker-compose -f docker-compose.prod.yml up -d

# View logs
docker-compose -f docker-compose.prod.yml logs -f saas-admin

# Stop services
docker-compose -f docker-compose.prod.yml down

# Update and restart
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
```

### Full Stack (from project root)
```bash
# Start all services (API, Web, Admin, SAAS Admin)
docker-compose up -d

# View specific service logs
docker-compose logs -f saas-admin

# Stop all services
docker-compose down

# Rebuild specific service
docker-compose up -d --build saas-admin
```

## Nginx Commands

### Configuration
```bash
# Test configuration
sudo nginx -t

# Reload configuration
sudo systemctl reload nginx

# Restart Nginx
sudo systemctl restart nginx

# Check status
sudo systemctl status nginx

# View logs
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log
```

### SSL Certificate (Let's Encrypt)
```bash
# Obtain certificate
sudo certbot --nginx -d saas-admin.example.com

# Renew certificate
sudo certbot renew

# Test renewal
sudo certbot renew --dry-run

# List certificates
sudo certbot certificates
```

## Environment Management

### Setup Environment
```bash
# Development
cp .env.example .env

# Production
cp .env.production.template .env.production
```

### Load Environment Variables
```bash
# Export from .env file
export $(cat .env | grep -v '^#' | xargs)

# Verify variables
echo $NUXT_PUBLIC_API_BASE
```

## Health Checks

### Application Health
```bash
# Check if app is responding
curl http://localhost:3002/saas-admin

# Check with headers
curl -I http://localhost:3002/saas-admin

# Check from external
curl https://saas-admin.example.com/saas-admin
```

### Container Health
```bash
# Check container status
docker ps | grep clinic-saas-admin

# Check health status
docker inspect --format='{{.State.Health.Status}}' clinic-saas-admin

# View health check logs
docker inspect --format='{{json .State.Health}}' clinic-saas-admin | jq
```

## Troubleshooting

### View All Logs
```bash
# Application logs
docker logs --tail 200 clinic-saas-admin

# Nginx access logs
sudo tail -100 /var/log/nginx/access.log

# Nginx error logs
sudo tail -100 /var/log/nginx/error.log

# System logs
sudo journalctl -u docker -n 100
```

### Debug Container
```bash
# Enter container shell
docker exec -it clinic-saas-admin sh

# Check environment variables
docker exec clinic-saas-admin env

# Check running processes
docker exec clinic-saas-admin ps aux

# Check network connectivity
docker exec clinic-saas-admin wget -O- http://api:8080/actuator/health
```

### Common Issues

#### Port Already in Use
```bash
# Find process using port 3002
sudo lsof -i :3002
sudo netstat -tulpn | grep 3002

# Kill process
sudo kill -9 <PID>
```

#### Container Won't Start
```bash
# Check logs for errors
docker logs clinic-saas-admin

# Remove and recreate
docker rm -f clinic-saas-admin
docker run -d --name clinic-saas-admin ...
```

#### 502 Bad Gateway
```bash
# Check if container is running
docker ps | grep clinic-saas-admin

# Check Nginx config
sudo nginx -t

# Restart both
docker restart clinic-saas-admin
sudo systemctl restart nginx
```

## Backup and Restore

### Backup
```bash
# Backup configuration
tar -czf saas-admin-backup-$(date +%Y%m%d).tar.gz \
  apps/saas-admin-nuxt/.env \
  apps/saas-admin-nuxt/nginx.conf

# Backup Docker image
docker save clinic-saas-admin:latest | \
  gzip > saas-admin-image-$(date +%Y%m%d).tar.gz
```

### Restore
```bash
# Restore configuration
tar -xzf saas-admin-backup-20240101.tar.gz

# Restore Docker image
docker load < saas-admin-image-20240101.tar.gz
```

## Updates

### Update Application
```bash
# Pull latest code
git pull origin main

# Rebuild and restart
docker-compose up -d --build saas-admin

# Or with deploy script
./deploy.sh
```

### Rollback
```bash
# Tag current as backup before update
docker tag clinic-saas-admin:latest clinic-saas-admin:backup

# If rollback needed
docker stop clinic-saas-admin
docker rm clinic-saas-admin
docker run -d --name clinic-saas-admin ... clinic-saas-admin:backup
```

## Monitoring

### Resource Usage
```bash
# Real-time stats
docker stats clinic-saas-admin

# Memory usage
docker exec clinic-saas-admin free -m

# Disk usage
docker exec clinic-saas-admin df -h

# System resources
htop
```

### Performance Testing
```bash
# Simple load test
ab -n 1000 -c 10 http://localhost:3002/saas-admin

# With authentication
curl -w "@curl-format.txt" -o /dev/null -s \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:3002/saas-admin/api/tenants
```

## Useful Aliases

Add to your `.bashrc` or `.zshrc`:

```bash
# SAAS Admin aliases
alias saas-logs='docker logs -f clinic-saas-admin'
alias saas-restart='docker restart clinic-saas-admin'
alias saas-shell='docker exec -it clinic-saas-admin sh'
alias saas-stats='docker stats clinic-saas-admin'
alias saas-deploy='cd /path/to/project/apps/saas-admin-nuxt && ./deploy.sh'
```

## Quick Reference URLs

- **Development**: http://localhost:3002/saas-admin
- **Production**: https://saas-admin.example.com/saas-admin
- **API Health**: http://localhost:8080/actuator/health
- **Docker Hub**: https://hub.docker.com/

## Support

For detailed information, see:
- [DEPLOYMENT.md](./DEPLOYMENT.md) - Full deployment guide
- [DEPLOYMENT-CHECKLIST.md](./DEPLOYMENT-CHECKLIST.md) - Deployment checklist
- [README.md](./README.md) - Application documentation
