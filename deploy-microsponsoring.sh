#!/bin/bash

# Microsponsoring Application Deployment Script
# Deploy Angular frontend, Spring backend, and MySQL to Kubernetes cluster

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    print_error "kubectl is not installed or not in PATH"
    exit 1
fi

# Check if we can connect to the cluster
if ! kubectl cluster-info &> /dev/null; then
    print_error "Cannot connect to Kubernetes cluster"
    exit 1
fi

print_status "Connected to Kubernetes cluster: $(kubectl cluster-info | head -n1)"

# Create namespace
print_status "Creating microsponsoring namespace..."
kubectl create namespace microsponsoring --dry-run=client -o yaml | kubectl apply -f -

# Create ConfigMap for MySQL configuration
print_status "Creating MySQL ConfigMap..."
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ConfigMap
metadata:
  name: mysql-config
  namespace: microsponsoring
data:
  mysql.cnf: |
    [mysqld]
    default-authentication-plugin=mysql_native_password
    character-set-server=utf8mb4
    collation-server=utf8mb4_unicode_ci
    innodb_buffer_pool_size=256M
    max_connections=200
EOF

# Create Secret for MySQL credentials
print_status "Creating MySQL Secret..."
kubectl create secret generic mysql-secret \
  --namespace microsponsoring \
  --from-literal=root-password=rootpassword123 \
  --from-literal=db-name=microsponsoring \
  --from-literal=db-user=microsponsoring \
  --from-literal=db-password=microsponsoring123 \
  --dry-run=client -o yaml | kubectl apply -f -

# Deploy MySQL
print_status "Deploying MySQL..."
cat <<EOF | kubectl apply -f -
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysql
  namespace: microsponsoring
  labels:
    app: mysql
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
      - name: mysql
        image: mysql:8.0
        ports:
        - containerPort: 3306
        env:
        - name: MYSQL_ROOT_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: root-password
        - name: MYSQL_DATABASE
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: db-name
        - name: MYSQL_USER
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: db-user
        - name: MYSQL_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: db-password
        volumeMounts:
        - name: mysql-config
          mountPath: /etc/mysql/conf.d
        - name: mysql-data
          mountPath: /var/lib/mysql
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
      volumes:
      - name: mysql-config
        configMap:
          name: mysql-config
      - name: mysql-data
        persistentVolumeClaim:
          claimName: mysql-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: mysql
  namespace: microsponsoring
spec:
  ports:
  - port: 3306
    targetPort: 3306
  selector:
    app: mysql
  clusterIP: None
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mysql-pvc
  namespace: microsponsoring
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
EOF

# Wait for MySQL to be ready
print_status "Waiting for MySQL to be ready..."
kubectl wait --for=condition=ready pod -l app=mysql -n microsponsoring --timeout=300s

# Deploy Spring Backend
print_status "Deploying Spring Backend..."
cat <<EOF | kubectl apply -f -
apiVersion: apps/v1
kind: Deployment
metadata:
  name: microsponsoring-backend
  namespace: microsponsoring
  labels:
    app: microsponsoring-backend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: microsponsoring-backend
  template:
    metadata:
      labels:
        app: microsponsoring-backend
    spec:
      containers:
      - name: backend
        image: microsponsoring-backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:mysql://mysql:3306/microsponsoring?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: db-user
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: db-password
        - name: SPRING_JPA_HIBERNATE_DDL_AUTO
          value: "update"
        - name: SPRING_JPA_SHOW_SQL
          value: "false"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: microsponsoring-backend
  namespace: microsponsoring
spec:
  type: ClusterIP
  ports:
  - port: 8080
    targetPort: 8080
  selector:
    app: microsponsoring-backend
EOF

# Deploy Angular Frontend
print_status "Deploying Angular Frontend..."
cat <<EOF | kubectl apply -f -
apiVersion: apps/v1
kind: Deployment
metadata:
  name: microsponsoring-frontend
  namespace: microsponsoring
  labels:
    app: microsponsoring-frontend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: microsponsoring-frontend
  template:
    metadata:
      labels:
        app: microsponsoring-frontend
    spec:
      containers:
      - name: frontend
        image: microsponsoring-frontend:latest
        ports:
        - containerPort: 80
        resources:
          requests:
            memory: "128Mi"
            cpu: "100m"
          limits:
            memory: "256Mi"
            cpu: "200m"
        livenessProbe:
          httpGet:
            path: /
            port: 80
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /
            port: 80
          initialDelaySeconds: 10
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: microsponsoring-frontend
  namespace: microsponsoring
spec:
  type: ClusterIP
  ports:
  - port: 80
    targetPort: 80
  selector:
    app: microsponsoring-frontend
EOF

# Deploy Ingress Controller (if not already installed)
print_status "Checking if Ingress Controller is installed..."
if ! kubectl get pods -n ingress-nginx &> /dev/null; then
    print_warning "Ingress Controller not found. Installing nginx-ingress..."
    kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.2/deploy/static/provider/baremetal/deploy.yaml
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/component=controller -n ingress-nginx --timeout=300s
fi

# Deploy Ingress
print_status "Deploying Ingress..."
cat <<EOF | kubectl apply -f -
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: microsponsoring-ingress
  namespace: microsponsoring
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "false"
spec:
  ingressClassName: nginx
  rules:
  - host: microsponsoring.local
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: microsponsoring-frontend
            port:
              number: 80
      - path: /api
        pathType: Prefix
        backend:
          service:
            name: microsponsoring-backend
            port:
              number: 8080
EOF

# Wait for all deployments to be ready
print_status "Waiting for all deployments to be ready..."
kubectl wait --for=condition=available deployment/microsponsoring-backend -n microsponsoring --timeout=300s
kubectl wait --for=condition=available deployment/microsponsoring-frontend -n microsponsoring --timeout=300s

# Display deployment status
print_status "Deployment Status:"
kubectl get all -n microsponsoring

print_status "Ingress Status:"
kubectl get ingress -n microsponsoring

print_success "Microsponsoring application deployed successfully!"
print_warning "Note: You need to build and push Docker images before deployment"
print_warning "Add 'microsponsoring.local' to your /etc/hosts file for local access"
