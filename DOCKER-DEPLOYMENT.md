# Pebble Docker Deployment Guide

**Status**: Implementation of Legacy Containerization Strategy  
**Date**: November 10, 2025  
**Based on**: Containerization Assessment from Discovery Phase

## Quick Start

### Option 1: Automated Setup (Recommended)
```bash
# Run the automated setup script
./start-pebble-docker.sh
```

### Option 2: Manual Setup
```bash
# Build the application container
docker build -f Dockerfile.multistage -t pebble-blog:latest .

# Start with Docker Compose
docker-compose up -d

# Check status
docker-compose ps
```

## What's Been Implemented

### ✅ Containerization Strategy
Based on our discovery assessment, we've implemented the **Legacy Dependency Isolation Strategy**:

- **Java 8** (closest available to required Java 6)
- **Tomcat 7.0.109** (latest 7.x version)
- **Ubuntu 18.04** base (LTS support for legacy packages)
- **Multi-stage build** (Maven build + Runtime container)
- **Volume persistence** for blog data and logs
- **Health checks** using the existing `/ping` endpoint

### Container Architecture
```
┌─────────────────────────────────────────┐
│           Builder Stage                 │
│  Maven 3.8 + OpenJDK 8                │
│  → Builds Pebble WAR file             │
└─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│           Runtime Stage                 │
│  Ubuntu 18.04 + OpenJDK 8             │
│  + Tomcat 7.0.109                     │
│  + Pebble WAR                         │
│  → Running Application                 │
└─────────────────────────────────────────┘
```

## Access Information

### Application URLs
- **Main Application**: http://localhost:8080/pebble/
- **Health Check**: http://localhost:8080/pebble/ping
- **Admin Interface**: http://localhost:8080/pebble/ (login required)

### Default Credentials
- **Username**: `username`
- **Password**: `password`

⚠️ **IMPORTANT**: Change these default credentials immediately after first login!

## Container Management

### Basic Commands
```bash
# Start the application
docker-compose up -d

# Stop the application
docker-compose down

# View logs
docker-compose logs -f pebble

# Restart the application
docker-compose restart pebble

# Access container shell
docker-compose exec pebble bash

# View container status
docker-compose ps
```

### Volume Management
```bash
# Backup blog data
docker run --rm -v pebble_pebble-data:/backup-source -v $(pwd):/backup \
  alpine tar czf /backup/pebble-backup-$(date +%Y%m%d).tar.gz -C /backup-source .

# Restore blog data
docker run --rm -v pebble_pebble-data:/restore-target -v $(pwd):/backup \
  alpine tar xzf /backup/pebble-backup-20251110.tar.gz -C /restore-target
```

## Configuration

### Environment Variables
The container supports the following environment variables:

```yaml
environment:
  # Java Runtime Settings
  - JAVA_OPTS=-Xmx1024m -Xms512m -XX:MaxPermSize=256m
  - CATALINA_OPTS=-Dfile.encoding=UTF-8 -Duser.timezone=UTC
  
  # Pebble Configuration
  - PEBBLE_DATA_DIR=/app/data
```

### Volume Mounts
| Volume | Container Path | Purpose |
|--------|---------------|---------|
| `pebble-data` | `/app/data` | Blog content, configuration, search indices |
| `pebble-logs` | `/app/logs` | Application and Tomcat logs |

### Port Mappings
| Host Port | Container Port | Service |
|-----------|----------------|---------|
| 8080 | 8080 | HTTP Web Interface |

## Monitoring and Health Checks

### Health Check
The container includes automatic health monitoring:
- **Endpoint**: http://localhost:8080/pebble/ping
- **Interval**: 30 seconds
- **Timeout**: 10 seconds  
- **Start Period**: 60 seconds (application startup time)
- **Retries**: 3 attempts before marking unhealthy

### Checking Application Health
```bash
# Check container health status
docker-compose ps

# View health check logs
docker inspect pebble-blog | jq '.[].State.Health'

# Manual health check
curl -f http://localhost:8080/pebble/ping
```

## Troubleshooting

### Common Issues

#### 1. Container Won't Start
```bash
# Check container logs
docker-compose logs pebble

# Common causes:
# - Port 8080 already in use
# - Insufficient memory
# - Volume mount issues
```

#### 2. Application Not Accessible
```bash
# Verify container is running
docker-compose ps

# Check port mapping
docker port pebble-blog

# Test internal connectivity
docker-compose exec pebble curl localhost:8080/pebble/ping
```

#### 3. Build Failures
```bash
# Common build issues:
# - Docker daemon not running
# - Insufficient disk space
# - Network issues downloading dependencies

# Clean build
docker system prune
docker build --no-cache -f Dockerfile.multistage -t pebble-blog:latest .
```

#### 4. Data Persistence Issues
```bash
# Check volume mounts
docker volume ls | grep pebble

# Inspect volume
docker volume inspect pebble_pebble-data

# Check permissions
docker-compose exec pebble ls -la /app/data
```

### Log Locations
- **Application Logs**: Accessible via `docker-compose logs pebble`
- **Tomcat Logs**: `/app/logs/tomcat/` inside container
- **Pebble Logs**: `/app/logs/pebble/` inside container

## Security Considerations

### Container Security
✅ **Implemented**:
- Non-root user execution (`pebble` user)
- Minimal base image (Ubuntu 18.04)
- Read-only filesystem for application code
- Volume isolation for data

⚠️ **Legacy Security Risks**:
- Java 8 (not Java 6 as originally required)
- Tomcat 7.0.109 (past EOL but latest 7.x)
- Legacy application dependencies with known vulnerabilities

### Network Security
```bash
# Run on isolated network (already configured in docker-compose.yml)
# Additional security measures:

# 1. Use reverse proxy for HTTPS termination
# 2. Restrict network access to trusted sources
# 3. Regular security updates of base image
# 4. Monitor container for security issues
```

## Production Deployment

### Production Checklist
- [ ] Change default admin credentials
- [ ] Configure HTTPS/TLS termination
- [ ] Set up regular data backups
- [ ] Implement log aggregation
- [ ] Configure monitoring and alerting
- [ ] Review and update security settings
- [ ] Document operational procedures

### Recommended Production Setup
```yaml
# Example production docker-compose with reverse proxy
version: '3.8'
services:
  pebble:
    image: pebble-blog:latest
    container_name: pebble-blog-prod
    expose:
      - "8080"
    volumes:
      - pebble-data:/app/data
      - pebble-logs:/app/logs
    environment:
      - JAVA_OPTS=-Xmx2048m -Xms1024m
    restart: always
    networks:
      - backend

  nginx:
    image: nginx:alpine
    container_name: pebble-proxy
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - ./ssl:/etc/nginx/ssl:ro
    depends_on:
      - pebble
    restart: always
    networks:
      - backend

volumes:
  pebble-data:
  pebble-logs:

networks:
  backend:
    driver: bridge
```

## Legacy Modernization Path

This containerization represents **Phase 1** of the modernization strategy:

### ✅ Current Achievement (Phase 1)
- Legacy application containerized and isolated
- Consistent development/production environments
- Data persistence and backup capabilities
- Health monitoring and basic operational tools

### 🎯 Future Phases (Planned)
- **Phase 2**: Java runtime modernization (Java 8 → 17+)
- **Phase 3**: Framework updates (Spring 3.x → 6.x)  
- **Phase 4**: Architecture evolution (REST APIs, modern frontend)
- **Phase 5**: Cloud-native features (observability, scaling)

## Support and Next Steps

### Immediate Actions
1. **Test the application**: Verify all features work as expected
2. **Configure blog settings**: Update blog properties and user accounts
3. **Create content**: Test blog posting and content management
4. **Monitor performance**: Establish baseline metrics

### Long-term Planning
1. **Security hardening**: Implement additional security measures
2. **Performance optimization**: Monitor and optimize container performance
3. **Backup strategy**: Implement automated backup procedures
4. **Modernization planning**: Begin Phase 2 planning for runtime updates

---

**Implementation Status**: ✅ **COMPLETE** - Legacy containerization deployed  
**Application Status**: 🚀 **READY** - Pebble blog accessible at http://localhost:8080/pebble/  
**Next Phase**: Monitor, optimize, and plan Phase 2 modernization  