# CI/CD Pipeline Setup Guide

## 🚀 Overview

This project now includes a comprehensive CI/CD pipeline with multiple quality checks, security scanning, and automated deployment capabilities.

## 📋 What's Included

### 1. **GitHub Actions Workflow** (`.github/workflows/ci-cd.yml`)
- **Backend Build & Test**: Maven compilation, testing, and Docker image creation
- **Frontend Build & Test**: Node.js build, linting, and Docker image creation
- **Code Quality Analysis**: SonarCloud integration with fallback to local tools
- **Security Scanning**: OWASP dependency check
- **Docker Build & Push**: Automated container registry deployment
- **Kubernetes Deployment**: Automated deployment to K8s clusters

### 2. **Code Quality Tools**
- **SonarCloud**: Cloud-based code analysis (primary)
- **SpotBugs**: Static analysis for bug detection
- **Checkstyle**: Code style and formatting checks
- **JaCoCo**: Code coverage reporting
- **OWASP Dependency Check**: Security vulnerability scanning

### 3. **Local Development Scripts**
- `local-quality-check.bat/.ps1`: Run all quality checks locally
- `build-backend-for-k8s.bat/.ps1`: Build backend for Kubernetes
- `build-all-for-k8s.bat`: Build entire application

## 🔧 Setup Instructions

### 1. **GitHub Secrets Configuration**

Add these secrets to your GitHub repository:

```bash
# Required for SonarCloud
SONAR_TOKEN=your_sonarcloud_token

# Required for container registry
CONTAINER_REGISTRY=your-registry.com
CONTAINER_USERNAME=your_username
CONTAINER_PASSWORD=your_password

# Required for Kubernetes deployment
KUBE_CONFIG=base64_encoded_kubeconfig
```

### 2. **SonarCloud Setup**

1. Go to [SonarCloud](https://sonarcloud.io)
2. Create a new project for your repository
3. Generate a token in your account settings
4. Add the token as `SONAR_TOKEN` secret in GitHub

### 3. **Container Registry Setup**

Configure your preferred container registry:
- **Docker Hub**: `docker.io/yourusername`
- **Azure Container Registry**: `yourregistry.azurecr.io`
- **AWS ECR**: `your-account.dkr.ecr.region.amazonaws.com`
- **Google GCR**: `gcr.io/your-project`

### 4. **Kubernetes Setup**

1. Create a kubeconfig file for your cluster
2. Base64 encode it: `base64 -i kubeconfig`
3. Add the encoded string as `KUBE_CONFIG` secret

## 🚦 Pipeline Triggers

The pipeline runs on:
- **Push to main/develop branches**: Full CI/CD pipeline
- **Pull requests**: Build and test only
- **Manual trigger**: Available in GitHub Actions tab

## 📊 Quality Gates

### SonarCloud Quality Gate
- **Coverage**: Minimum 80% line coverage
- **Duplications**: Maximum 3% duplicated lines
- **Maintainability**: A rating
- **Reliability**: A rating
- **Security**: A rating

### Local Quality Checks
- **Compilation**: Must succeed
- **Tests**: Should pass (warnings allowed)
- **Static Analysis**: SpotBugs and Checkstyle (warnings allowed)
- **Security**: OWASP dependency check (warnings allowed)

## 🔍 Troubleshooting

### SonarCloud Issues
If SonarCloud fails (like the 500 error you experienced):
1. The pipeline will continue with local quality checks
2. Check SonarCloud status at [status.sonarcloud.io](https://status.sonarcloud.io)
3. Retry the analysis manually from SonarCloud dashboard

### Build Failures
1. Check the GitHub Actions logs for specific errors
2. Run local quality checks: `local-quality-check.bat`
3. Verify all secrets are properly configured

### Docker Build Issues
1. Ensure Docker is running locally
2. Check container registry credentials
3. Verify image names and tags

## 📈 Monitoring

### GitHub Actions
- View pipeline status in the "Actions" tab
- Download artifacts (reports, logs, etc.)
- Check individual job logs

### SonarCloud Dashboard
- View code quality metrics
- Track technical debt
- Monitor security vulnerabilities

### Local Reports
After running local quality checks, find reports in:
- `microsponsoring-backend/target/spotbugsXml.xml`
- `microsponsoring-backend/target/checkstyle-result.xml`
- `microsponsoring-backend/target/dependency-check-reports/`
- `microsponsoring-backend/target/site/jacoco/index.html`

## 🛠️ Customization

### Adding New Quality Checks
1. Add Maven plugin to `pom.xml`
2. Update GitHub Actions workflow
3. Add to local quality check scripts

### Modifying Quality Gates
1. Update SonarCloud project settings
2. Modify `sonar-project.properties`
3. Adjust Maven plugin configurations

### Custom Deployment
1. Update Kubernetes deployment files in `k8s/`
2. Modify the deploy job in GitHub Actions
3. Add environment-specific configurations

## 📚 Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [SonarCloud Documentation](https://docs.sonarcloud.io/)
- [Maven Plugin Documentation](https://maven.apache.org/plugins/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)

## 🆘 Support

If you encounter issues:
1. Check the troubleshooting section above
2. Review GitHub Actions logs
3. Run local quality checks for debugging
4. Check SonarCloud status page
