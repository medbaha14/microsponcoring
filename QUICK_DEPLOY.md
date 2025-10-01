# Quick Deploy Reference

## 🚀 One-Command Deployment

### On your master node at `/home/masternode/microsponcoring`:

```bash
# Pull latest code
git pull origin main

# Deploy to Kubernetes (Linux/Mac)
./deploy-latest.sh

# OR Deploy to Kubernetes (Windows)
.\deploy-latest.ps1
```

## 📋 What the script does:

1. ✅ Checks kubectl connection
2. ✅ Creates namespace if needed
3. ✅ Pulls latest Docker images:
   - `medbaha/pfebackend:lastVer`
   - `medbaha/pfefrontend:lastVer`
4. ✅ Deploys to worker nodes (3 replicas each)
5. ✅ Shows deployment status
6. ✅ Applies secrets and monitoring

## 🔍 Quick Status Check:

```bash
# Check all pods
kubectl get pods -n microsponsoring

# Check services
kubectl get services -n microsponsoring

# Check deployments
kubectl get deployments -n microsponsoring
```

## 📊 View Logs:

```bash
# Backend logs
kubectl logs -f deployment/backend-deployment -n microsponsoring

# Frontend logs
kubectl logs -f deployment/frontend-deployment -n microsponsoring
```

## 🛠️ Troubleshooting:

```bash
# If deployment fails
kubectl describe pods -n microsponsoring

# Force restart deployments
kubectl rollout restart deployment/backend-deployment -n microsponsoring
kubectl rollout restart deployment/frontend-deployment -n microsponsoring
```

---
**Note**: The scripts automatically handle pulling the latest images and distributing them across your worker nodes.
