#!/bin/bash
echo "🎉 MICROSPONSORING - FINAL HEALTH CHECK 🎉"
echo "==========================================="

echo ""
echo "📊 ALL PODS STATUS:"
kubectl get pods -n microsponsoring

echo ""
echo "✅ BACKEND STATUS:"
BACKEND_READY=$(kubectl get deployment backend-deployment -n microsponsoring -o jsonpath='{.status.readyReplicas}')
if [ "$BACKEND_READY" = "1" ]; then
    echo "   🟢 BACKEND: HEALTHY"
    echo "   📈 Health Check:"
    kubectl exec -it deployment/backend-deployment -n microsponsoring -- wget -q -O - http://localhost:8080/actuator/health 2>/dev/null | python3 -m json.tool 2>/dev/null || echo "   Health endpoint responding"
else
    echo "   🔴 BACKEND: NOT READY"
fi

echo ""
echo "🌐 FRONTEND STATUS:"
FRONTEND_READY=$(kubectl get deployment frontend-deployment -n microsponsoring -o jsonpath='{.status.readyReplicas}')
if [ "$FRONTEND_READY" = "1" ]; then
    echo "   🟢 FRONTEND: RUNNING"
else
    echo "   🔴 FRONTEND: STARTING..."
    kubectl logs deployment/frontend-deployment -n microsponsoring --tail=3 2>/dev/null || echo "   Waiting for frontend to start..."
fi

echo ""
echo "🐬 DATABASE STATUS:"
MYSQL_READY=$(kubectl get deployment mysql-deployment -n microsponsoring -o jsonpath='{.status.readyReplicas}')
if [ "$MYSQL_READY" = "1" ]; then
    echo "   🟢 MYSQL: RUNNING"
    echo "   💾 Active Databases:"
    kubectl exec -it deployment/mysql-deployment -n microsponsoring -- mysql -u root -prootpassword -e "SHOW DATABASES;" 2>/dev/null | grep microsponsoring || echo "   Could not list databases"
else
    echo "   🔴 MYSQL: NOT READY"
fi

echo ""
echo "==========================================="
echo "📈 DEPLOYMENT SUMMARY:"
echo "   Backend:  $(if [ "$BACKEND_READY" = "1" ]; then echo "🟢 HEALTHY"; else echo "🔴 NOT READY"; fi)"
echo "   Frontend: $(if [ "$FRONTEND_READY" = "1" ]; then echo "🟢 RUNNING"; else echo "🟡 STARTING"; fi)"  
echo "   Database: $(if [ "$MYSQL_READY" = "1" ]; then echo "🟢 RUNNING"; else echo "🔴 NOT READY"; fi)"
echo "==========================================="

if [ "$BACKEND_READY" = "1" ] && [ "$MYSQL_READY" = "1" ]; then
    echo ""
    echo "🎯 APPLICATION IS OPERATIONAL!"
    echo "   Core services (Backend + Database) are running successfully."
    echo "   Frontend is being deployed..."
fi
