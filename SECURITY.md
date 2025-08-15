# Security Guide for Microsponsoring Project

This document describes the security features implemented in the Microsponsoring project and how to use them.

## 🔒 Security Features Implemented

### 1. **Dependabot Integration**
- **Automatic dependency updates** for npm and Maven packages
- **Weekly security scans** for vulnerabilities
- **Automated pull requests** for security patches
- **Configuration**: `.github/dependabot.yml`

### 2. **CI/CD Security Pipeline**
- **Trivy vulnerability scanning** for container images
- **OWASP dependency check** for Java/Maven dependencies
- **NPM audit** for Node.js dependencies
- **Security gates** to prevent vulnerable builds

### 3. **Local Security Tools**
- **Security check script**: `security-check.sh`
- **Maven security configuration**: `.mvnrc`
- **NPM security configuration**: `.npmrc`
- **OWASP dependency check plugin** in `pom.xml`

## 🚀 How to Use Security Features

### **Automated Security (GitHub Actions)**
The CI/CD pipeline automatically:
1. **Scans dependencies** for vulnerabilities
2. **Blocks builds** if critical issues found (CVSS ≥ 7)
3. **Generates security reports** as artifacts
4. **Integrates with GitHub Security tab**

### **Local Security Checks**
Run the security check script:
```bash
# Make script executable
chmod +x security-check.sh

# Run security check
./security-check.sh
```

### **Manual Security Checks**

#### **Frontend (NPM)**
```bash
cd microsponsoring-frontend

# Check for vulnerabilities
npm audit

# Check for outdated packages
npm outdated

# Check specific package
npm ls @angular/core
npm ls ng2-charts
```

#### **Backend (Maven)**
```bash
cd microsponsoring-backend

# Check dependency tree
mvn dependency:tree

# Run OWASP dependency check
mvn org.owasp:dependency-check-maven:check

# Check for updates
mvn versions:display-dependency-updates
```

## 📊 Security Reports

### **Generated Reports**
- **NPM Audit**: `microsponsoring-frontend/npm-audit.json`
- **OWASP Reports**: `microsponsoring-backend/target/dependency-check-reports/`
  - HTML report for web viewing
  - JSON report for automation
  - SARIF report for GitHub integration

### **GitHub Security Tab**
- **Security scanning results** from Trivy
- **Dependency alerts** from Dependabot
- **Code scanning** results
- **Security advisories** and vulnerabilities

## ⚠️ Security Alerts

### **Critical Issues (CVSS ≥ 7)**
- **Automatically block builds** in CI/CD
- **Require immediate attention**
- **Update packages** or apply patches

### **Moderate Issues (CVSS 4-6)**
- **Warnings in pipeline** but don't block builds
- **Review and plan updates**
- **Monitor for patches**

### **Low Issues (CVSS 1-3)**
- **Informational only**
- **Update when convenient**
- **Monitor for improvements**

## 🔧 Configuration

### **Dependabot Settings**
- **Weekly scans** for all ecosystems
- **Automatic PR creation** for security updates
- **Reviewers and assignees** configured
- **Commit message formatting** standardized

### **Security Thresholds**
- **Build blocking**: CVSS ≥ 7
- **Warning level**: CVSS ≥ 4
- **Report formats**: HTML, JSON, SARIF
- **Retention**: 30 days for artifacts

## 📋 Best Practices

### **Regular Security Maintenance**
1. **Weekly**: Review Dependabot alerts
2. **Monthly**: Run local security checks
3. **Quarterly**: Review and update security policies
4. **Immediately**: Address critical vulnerabilities

### **Dependency Management**
1. **Keep dependencies updated** regularly
2. **Review security advisories** for critical packages
3. **Test updates** before deployment
4. **Document security decisions** in suppression files

### **Monitoring and Alerting**
1. **Watch repository** for security alerts
2. **Enable email notifications** for security issues
3. **Set up team notifications** for critical alerts
4. **Monitor CI/CD pipeline** for security failures

## 🆘 Getting Help

### **Security Issues**
- **Create an issue** with security label
- **Tag security team** members
- **Include vulnerability details** and CVSS scores
- **Provide reproduction steps** if applicable

### **False Positives**
- **Document in suppression.xml** with reasoning
- **Include evidence** of false positive
- **Review periodically** for accuracy
- **Update when packages are fixed**

## 📚 Resources

- [GitHub Security Features](https://docs.github.com/en/github/getting-started-with-github/learning-about-github/about-github-security)
- [OWASP Dependency Check](https://owasp.org/www-project-dependency-check/)
- [NPM Security](https://docs.npmjs.com/cli/v8/commands/npm-audit)
- [Dependabot Documentation](https://docs.github.com/en/code-security/supply-chain-security/keeping-your-dependencies-updated-automatically/about-dependabot-version-updates)

---

**Last Updated**: $(date)
**Security Contact**: Repository maintainers
**Review Schedule**: Quarterly 