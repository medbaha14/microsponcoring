#!/bin/bash
echo "🚀 Complete Database Fix..."

# Create both possible database names to be safe
echo "Creating databases..."
kubectl exec -it deployment/mysql -n microsponsoring -- mysql -u root -prootpassword -e "
CREATE DATABASE IF NOT EXISTS microsponsoring_db;
CREATE DATABASE IF NOT EXISTS microsponsoring;
SHOW DATABASES;
" 2>/dev/null

# Update backend deployment to use microsponsoring_db (the one it expects)
echo "Updating backend configuration..."
kubectl set env deployment/backend-deployment -n microsponsoring DB_URL="jdbc:mysql://mysql:3306/microsponsoring_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"

# Wait for restart
echo "Waiting for backend..."
sleep 45

echo "✅ Complete fix applied!"
echo "📝 Backend logs:"
kubectl logs deployment/backend-deployment -n microsponsoring --tail=30
