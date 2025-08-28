# Kubernetes Cluster Setup for Microsponsoring Application

This guide will help you create a 3-node Kubernetes cluster using Ubuntu VMs and deploy your microsponsoring application (Angular frontend, Spring backend, and MySQL database).

## Prerequisites

- Windows 10/11 with Hyper-V enabled
- At least 16GB RAM available for VMs
- Ubuntu 22.04 LTS ISO file
- PowerShell with administrative privileges

## Step 1: Enable Hyper-V

If Hyper-V is not already enabled, run this command in PowerShell as Administrator:

```powershell
Enable-WindowsOptionalFeature -Online -FeatureName Microsoft-Hyper-V-All -All
```

## Step 2: Download Ubuntu ISO

Download Ubuntu 22.04 LTS Server ISO from: https://ubuntu.com/download/server
Place it in `C:\ISOs\` directory.

## Step 3: Create Virtual Machines

Run the PowerShell script to create the VMs:

```powershell
.\create-k8s-cluster.ps1
```

This will create:
- **k8s-master**: Master node for Kubernetes control plane
- **k8s-worker1**: First worker node
- **k8s-worker2**: Second worker node

## Step 4: Install Ubuntu on Each VM

1. **Master Node (k8s-master)**:
   - Start the VM and complete Ubuntu installation
   - Use hostname: `k8s-master`
   - Create a user account and note the credentials
   - Install OpenSSH server when prompted

2. **Worker Node 1 (k8s-worker1)**:
   - Start the VM and complete Ubuntu installation
   - Use hostname: `k8s-worker1`
   - Create a user account and note the credentials
   - Install OpenSSH server when prompted

3. **Worker Node 2 (k8s-worker2)**:
   - Start the VM and complete Ubuntu installation
   - Use hostname: `k8s-worker2`
   - Create a user account and note the credentials
   - Install OpenSSH server when prompted

## Step 5: Configure Network

After Ubuntu installation, get the IP addresses of each VM:

```bash
ip addr show
```

Note down the IP addresses for each node.

## Step 6: Setup Kubernetes on Master Node

1. SSH into the master node:
   ```bash
   ssh username@<master-ip>
   ```

2. Run the Kubernetes setup script:
   ```bash
   sudo chmod +x setup-k8s-nodes.sh
   sudo ./setup-k8s-nodes.sh master
   ```

3. Save the join command output - you'll need it for worker nodes.

## Step 7: Setup Kubernetes on Worker Nodes

1. SSH into each worker node:
   ```bash
   ssh username@<worker-ip>
   ```

2. Run the Kubernetes setup script:
   ```bash
   sudo chmod +x setup-k8s-nodes.sh
   sudo ./setup-k8s-nodes.sh worker
   ```

3. Use the join command from the master node to join the cluster:
   ```bash
   sudo kubeadm join <master-ip>:6443 --token <token> --discovery-token-ca-cert-hash <hash>
   ```

## Step 8: Verify Cluster Status

On the master node, check cluster status:

```bash
kubectl get nodes
kubectl get pods --all-namespaces
```

## Step 9: Build Docker Images

Before deploying, you need to build Docker images for your application:

### Backend (Spring Boot)
```bash
cd microsponsoring-backend
docker build -t microsponsoring-backend:latest .
```

### Frontend (Angular)
```bash
cd microsponsoring-frontend
docker build -t microsponsoring-frontend:latest .
```

## Step 10: Deploy Application

1. Copy the deployment script to the master node
2. Make it executable:
   ```bash
   chmod +x deploy-microsponsoring.sh
   ```

3. Run the deployment:
   ```bash
   sudo ./deploy-microsponsoring.sh
   ```

## Step 11: Access the Application

1. Add to your hosts file (`C:\Windows\System32\drivers\etc\hosts`):
   ```
   <master-ip> microsponsoring.local
   ```

2. Access the application at: http://microsponsoring.local

## Troubleshooting

### Common Issues

1. **VMs won't start**: Check if Hyper-V is enabled and virtualization is enabled in BIOS
2. **Network issues**: Ensure the Default Switch is created and VMs are connected
3. **Kubernetes join fails**: Check if the token is still valid (tokens expire after 24 hours)
4. **Pods stuck in Pending**: Check if there are enough resources on worker nodes

### Useful Commands

```bash
# Check node status
kubectl get nodes

# Check pod status
kubectl get pods -n microsponsoring

# Check service status
kubectl get services -n microsponsoring

# Check logs
kubectl logs <pod-name> -n microsponsoring

# Describe resources
kubectl describe pod <pod-name> -n microsponsoring
```

### Reset Cluster (if needed)

On master node:
```bash
sudo kubeadm reset
sudo rm -rf /etc/cni/net.d
sudo rm -rf $HOME/.kube/config
```

On worker nodes:
```bash
sudo kubeadm reset
```

## Resource Requirements

- **Master Node**: 4GB RAM, 2 CPU cores, 50GB disk
- **Worker Nodes**: 4GB RAM, 2 CPU cores, 50GB disk each
- **Total**: 12GB RAM, 6 CPU cores, 150GB disk

## Security Notes

- Change default passwords in production
- Use proper secrets management
- Enable RBAC and network policies
- Regular security updates
- Monitor cluster access

## Next Steps

After successful deployment:
1. Set up monitoring with Prometheus and Grafana
2. Configure backup strategies for MySQL data
3. Set up CI/CD pipelines
4. Implement proper logging and alerting
5. Configure SSL certificates for production use

## Support

If you encounter issues:
1. Check the troubleshooting section
2. Verify all prerequisites are met
3. Check Kubernetes and Docker logs
4. Ensure network connectivity between nodes
