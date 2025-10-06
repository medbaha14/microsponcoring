#!/bin/bash
echo "🎉 MICROSPONSORING DEPLOYMENT STATUS 🎉"

echo ""
echo "📊 PODS STATUS:"
kubectl get pods -n microsponsoring

echo ""
echo "🌐 SERVICES:"
kubectl get svc -n microsponsoring

echo ""
echo "🚀 DEPLOYMENTS:"
kubectl get deployments -n microsponsoring

echo ""
echo "🔍 BACKEND HEALTH:"
kubectl exec -it deployment/backend-deployment -n microsponsoring -- curl -s http://localhost:8080/actuator/health || echo "Backend health check failed"

echo ""
echo "📈 APPLICATION READY!"
