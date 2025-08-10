# Microsponsoring Application

A comprehensive microsponsoring platform built with Spring Boot backend and Angular frontend, designed for containerized deployment with Docker and Kubernetes.

## 🚀 Quick Start

### Prerequisites
- Docker Desktop or Docker Engine
- Docker Compose
- Git

### One-Click Deployment
```bash
# Linux/Mac
chmod +x quick-start.sh
./quick-start.sh

# Windows
quick-start.bat
```

This will automatically:
- Build all Docker images
- Start all services
- Wait for services to be healthy
- Display access URLs

## 📁 Project Structure

```
microsponsoring/
├── microsponsoring-backend/          # Spring Boot Backend
│   ├── Dockerfile                    # Backend container image
│   ├── .dockerignore                 # Docker build exclusions
│   └── src/                          # Java source code
├── microsponsoring-frontend/         # Angular Frontend
│   ├── Dockerfile                    # Frontend container image
│   ├── .dockerignore                 # Docker build exclusions
│   ├── nginx.conf                    # Nginx configuration
│   └── src/                          # Angular source code
├── k8s/                              # Kubernetes manifests
│   ├── namespace.yaml                # Namespace definitions
│   ├── backend-deployment.yaml       # Backend deployment
│   ├── frontend-deployment.yaml      # Frontend deployment
│   ├── secrets.yaml                  # Kubernetes secrets
│   └── monitoring.yaml               # Monitoring stack
├── monitoring/                       # Monitoring configuration
│   ├── prometheus.yml                # Prometheus config
│   └── grafana/                      # Grafana configs
├── .github/workflows/                # CI/CD pipeline
├── docker-compose.yml                # Local development
├── quick-start.sh                    # Linux/Mac startup script
├── quick-start.bat                   # Windows startup script
└── DEPLOYMENT_GUIDE.md               # Detailed deployment guide
```

## 🐳 Docker Images

### Backend Image
- **Repository**: `medbaha/pfebackend`
- **Base**: Eclipse Temurin 21 JRE Alpine
- **Features**: Multi-stage build, health checks, non-root user

### Frontend Image
- **Repository**: `medbaha/pfefrontend`
- **Base**: Nginx Alpine
- **Features**: Angular build, optimized Nginx config, health checks

## ☸️ Kubernetes Deployment

### Namespaces
- `microsponsoring`: Production environment
- `microsponsoring-staging`: Staging environment

### Services
- **Backend**: 3 replicas with auto-scaling
- **Frontend**: 3 replicas with auto-scaling
- **MySQL**: Persistent database
- **Redis**: Caching layer

### Monitoring
- **Prometheus**: Metrics collection
- **Grafana**: Dashboards and visualization
- **Health checks**: Built-in application monitoring

## 🔄 CI/CD Pipeline

### GitHub Actions Features
- **Code Quality**: SonarQube analysis for both backend and frontend
- **Testing**: Automated testing for Java and Angular
- **Security**: Trivy vulnerability scanning
- **Building**: Docker image building and pushing
- **Deployment**: Automatic deployment to staging and production
- **Notifications**: Slack/Discord integration

### Pipeline Stages
1. **Code Quality Analysis** - SonarQube scanning
2. **Backend Testing** - Maven tests and coverage
3. **Frontend Testing** - Angular tests and coverage
4. **Security Scanning** - Vulnerability assessment
5. **Docker Build** - Image creation and registry push
6. **Deployment** - Kubernetes deployment
7. **Notifications** - Team communication

## 📊 Monitoring & Observability

### Metrics Collection
- **Application Metrics**: Spring Boot Actuator endpoints
- **System Metrics**: Kubernetes node and pod metrics
- **Database Metrics**: MySQL performance metrics
- **Cache Metrics**: Redis performance metrics

### Dashboards
- **Application Health**: Service status and performance
- **Infrastructure**: Cluster resource utilization
- **Business Metrics**: Custom application KPIs

### Alerting
- **Resource Usage**: CPU, memory, and storage alerts
- **Application Health**: Response time and error rate alerts
- **Infrastructure**: Node and pod health alerts

## 🔐 Security Features

### Container Security
- Non-root user execution
- Minimal base images
- Security scanning in CI/CD
- Regular vulnerability updates

### Kubernetes Security
- RBAC implementation
- Network policies
- Secrets management
- Pod security standards

### Application Security
- JWT authentication
- Input validation
- SQL injection prevention
- XSS protection

## 🚀 Performance & Scaling

### Horizontal Scaling
- **Backend**: 3-10 replicas based on CPU usage
- **Frontend**: 3-10 replicas based on CPU usage
- **Database**: Read replicas and connection pooling

### Resource Optimization
- **Memory**: Optimized JVM settings
- **CPU**: Efficient thread management
- **Storage**: Persistent volume management
- **Network**: Optimized Nginx configuration

## 🛠️ Development Workflow

### Local Development
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f [service-name]

# Stop services
docker-compose down

# Rebuild and restart
docker-compose up --build -d
```

### Testing
```bash
# Backend tests
cd microsponsoring-backend
mvn test

# Frontend tests
cd microsponsoring-frontend
npm test
```

### Building
```bash
# Backend
cd microsponsoring-backend
mvn clean package

# Frontend
cd microsponsoring-frontend
npm run build
```

## 🌐 Access URLs

### Local Development
- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080
- **Grafana**: http://localhost:3000 (admin/admin123)
- **Prometheus**: http://localhost:9090
- **MySQL**: localhost:3306
- **Redis**: localhost:6379

### Production
- **Frontend**: https://your-domain.com
- **Backend API**: https://api.your-domain.com
- **Grafana**: https://grafana.your-domain.com
- **Prometheus**: https://prometheus.your-domain.com

## 📚 Documentation

- [**Deployment Guide**](DEPLOYMENT_GUIDE.md) - Complete deployment instructions
- [**API Documentation**](microsponsoring-backend/README.md) - Backend API reference
- [**Frontend Guide**](microsponsoring-frontend/README.md) - Frontend development guide

## 🔧 Configuration

### Environment Variables
See [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md#application-configuration) for complete environment variable documentation.

### Kubernetes Configuration
- Update `k8s/secrets.yaml` with your actual values
- Modify `k8s/frontend-deployment.yaml` with your domain
- Adjust resource limits based on your cluster capacity

## 🆘 Support & Troubleshooting

### Common Issues
- **Service Health**: Check health endpoints and logs
- **Database Connection**: Verify MySQL credentials and network
- **Image Pull Issues**: Check Docker Hub authentication
- **Resource Limits**: Monitor pod resource usage

### Getting Help
1. Check the [troubleshooting section](DEPLOYMENT_GUIDE.md#troubleshooting)
2. Review application logs
3. Check Kubernetes events
4. Verify configuration files

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests and documentation
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Angular team for the powerful frontend framework
- Kubernetes community for container orchestration
- Docker team for containerization technology

---

**Happy Coding! 🎉** 