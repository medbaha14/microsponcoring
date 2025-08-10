# Microsponsoring Application Deployment Guide

This guide covers the complete setup and deployment of the Microsponsoring application using Docker and Kubernetes.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Docker Setup](#docker-setup)
4. [Kubernetes Deployment](#kubernetes-deployment)
5. [Monitoring Setup](#monitoring-setup)
6. [CI/CD Pipeline Setup](#cicd-pipeline-setup)
7. [Troubleshooting](#troubleshooting)

## Prerequisites

### Required Software
- Docker Desktop or Docker Engine
- kubectl (Kubernetes CLI)
- Git
- Java 21 (for backend development)
- Node.js 18 (for frontend development)
- Maven 3.9+ (for backend builds)

### Required Accounts
- Docker Hub account (for image registry)
- GitHub account (for CI/CD)
- SonarQube account (for code quality analysis)

## Local Development Setup

### Backend Setup
```bash
cd microsponsoring-backend
mvn clean install
mvn spring-boot:run
```

### Frontend Setup
```bash
cd microsponsoring-frontend
npm install
npm start
```

## Docker Setup

### Building Images Locally

#### Backend
```bash
cd microsponsoring-backend
docker build -t medbaha/pfebackend:latest .
```

#### Frontend
```bash
cd microsponsoring-frontend
docker build -t medbaha/pfefrontend:latest .
```

### Running with Docker Compose
Create a `docker-compose.yml` file in the root directory:

```yaml
version: '3.8'
services:
  backend:
    image: medbaha/pfebackend:latest
    ports:
      - "8080:8080"
    environment:
      - DB_URL=jdbc:mysql://mysql:3306/microsponsoring
      - DB_USERNAME=root
      - DB_PASSWORD=password
    depends_on:
      - mysql
    volumes:
      - images-data:/app/images
      - invoices-data:/app/invoices

  frontend:
    image: medbaha/pfefrontend:latest
    ports:
      - "80:80"
    depends_on:
      - backend

  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=password
      - MYSQL_DATABASE=microsponsoring
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql

volumes:
  mysql-data:
  images-data:
  invoices-data:
```

Run with:
```bash
docker-compose up -d
```

## Kubernetes Deployment

### 1. Create Namespaces
```bash
kubectl apply -f k8s/namespace.yaml
```

### 2. Create Secrets
**Important**: Update the secrets with your actual values before applying.

```bash
# Edit k8s/secrets.yaml with your actual values
kubectl apply -f k8s/secrets.yaml
```

### 3. Deploy Applications
```bash
kubectl apply -f k8s/backend-deployment.yaml
kubectl apply -f k8s/frontend-deployment.yaml
```

### 4. Deploy Monitoring Stack
```bash
kubectl apply -f k8s/monitoring.yaml
```

### 5. Verify Deployment
```bash
kubectl get pods -n microsponsoring
kubectl get services -n microsponsoring
kubectl get ingress -n microsponsoring
```

## Monitoring Setup

### Access Grafana
```bash
# Port forward Grafana service
kubectl port-forward -n microsponsoring svc/grafana 3000:3000
```

Access Grafana at: http://localhost:3000
- Username: `admin`
- Password: `admin123`

### Access Prometheus
```bash
# Port forward Prometheus service
kubectl port-forward -n microsponsoring svc/prometheus 9090:9090
```

Access Prometheus at: http://localhost:9090

## CI/CD Pipeline Setup

### 1. GitHub Secrets Configuration

Add the following secrets to your GitHub repository:

#### Docker Hub Credentials
- `DOCKER_USERNAME`: Your Docker Hub username
- `DOCKER_PASSWORD`: Your Docker Hub access token

#### SonarQube Configuration
- `SONAR_TOKEN`: Your SonarQube authentication token
- `SONAR_HOST_URL`: Your SonarQube server URL

#### Kubernetes Configuration
- `KUBE_CONFIG_STAGING`: Base64 encoded kubeconfig for staging cluster
- `KUBE_CONFIG_PRODUCTION`: Base64 encoded kubeconfig for production cluster

#### Optional Notifications
- `SLACK_WEBHOOK_URL`: Slack webhook URL for deployment notifications

### 2. Branch Protection Rules

Set up branch protection for `main` and `develop` branches:
- Require pull request reviews
- Require status checks to pass
- Require branches to be up to date

### 3. Environment Setup

Create environments in GitHub:
- `staging`: For staging deployments
- `production`: For production deployments

## Application Configuration

### Backend Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | MySQL database connection URL | `jdbc:mysql://localhost:3306/microsponsoring` |
| `DB_USERNAME` | Database username | `root` |
| `DB_PASSWORD` | Database password | `` |
| `CHECKOUT_SECRET_KEY` | Checkout.com secret key | Test key |
| `CHECKOUT_PUBLIC_KEY` | Checkout.com public key | Test key |
| `MAIL_HOST` | SMTP server host | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP server port | `587` |
| `MAIL_USERNAME` | Email username | Required |
| `MAIL_PASSWORD` | Email password | Required |

### Frontend Configuration

The frontend is configured to work with the backend service. Update the API endpoints in your Angular services to point to the correct backend URL.

## Scaling and Performance

### Horizontal Pod Autoscaling
```bash
kubectl autoscale deployment backend-deployment --cpu-percent=70 --min=3 --max=10 -n microsponsoring
kubectl autoscale deployment frontend-deployment --cpu-percent=70 --min=3 --max=10 -n microsponsoring
```

### Resource Limits
Adjust resource limits in the deployment files based on your application's needs and cluster capacity.

## Security Considerations

### 1. Secrets Management
- Use Kubernetes secrets for sensitive data
- Consider using external secret management solutions (HashiCorp Vault, AWS Secrets Manager)
- Rotate secrets regularly

### 2. Network Policies
Implement network policies to restrict pod-to-pod communication:

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: backend-network-policy
  namespace: microsponsoring
spec:
  podSelector:
    matchLabels:
      app: backend
  policyTypes:
  - Ingress
  ingress:
  - from:
    - podSelector:
        matchLabels:
          app: frontend
    ports:
    - protocol: TCP
      port: 8080
```

### 3. RBAC
Implement proper RBAC for service accounts and limit permissions to minimum required.

## Backup and Recovery

### Database Backup
```bash
# Create backup job
kubectl create job --from=cronjob/backup-db backup-db-manual -n microsponsoring
```

### Persistent Volume Backups
Implement regular backups of your persistent volumes using your cloud provider's backup solutions.

## Troubleshooting

### Common Issues

#### 1. Pods Not Starting
```bash
kubectl describe pod <pod-name> -n microsponsoring
kubectl logs <pod-name> -n microsponsoring
```

#### 2. Services Not Accessible
```bash
kubectl get endpoints -n microsponsoring
kubectl describe service <service-name> -n microsponsoring
```

#### 3. Image Pull Issues
```bash
kubectl describe pod <pod-name> -n microsponsoring | grep -A 10 Events
```

#### 4. Resource Issues
```bash
kubectl top pods -n microsponsoring
kubectl describe node <node-name>
```

### Logs and Debugging
```bash
# View logs for all pods in namespace
kubectl logs -l app=backend -n microsponsoring --tail=100

# Execute commands in running pods
kubectl exec -it <pod-name> -n microsponsoring -- /bin/bash
```

## Support and Maintenance

### Regular Maintenance Tasks
1. **Weekly**: Check pod health and resource usage
2. **Monthly**: Review and update security patches
3. **Quarterly**: Review and optimize resource allocation
4. **Annually**: Review and update deployment strategies

### Monitoring Alerts
Set up alerts in Grafana for:
- High CPU/Memory usage
- Pod restart frequency
- Response time degradation
- Error rate increases

## Additional Resources

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Docker Documentation](https://docs.docker.com/)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Angular Documentation](https://angular.io/docs) 