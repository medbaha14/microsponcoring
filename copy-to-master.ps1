# Copy deployment files to Kubernetes master node
# This script helps you copy the necessary files to your master node

param(
    [Parameter(Mandatory=$true)]
    [string]$MasterNodeIP,
    
    [Parameter(Mandatory=$false)]
    [string]$Username = "root",
    
    [Parameter(Mandatory=$false)]
    [string]$TargetPath = "/home/masternode/microsponcoring"
)

Write-Host "=== Copying files to Kubernetes master node ===" -ForegroundColor Green
Write-Host "Master Node IP: $MasterNodeIP" -ForegroundColor Cyan
Write-Host "Username: $Username" -ForegroundColor Cyan
Write-Host "Target Path: $TargetPath" -ForegroundColor Cyan

# Files to copy
$filesToCopy = @(
    "k8s/",
    "deploy-to-k8s.ps1",
    "deploy-to-k8s.sh",
    "update-and-deploy.ps1",
    "update-and-deploy.sh",
    "KUBERNETES_DEPLOYMENT.md"
)

Write-Host "`n1. Creating target directory on master node..." -ForegroundColor Yellow
ssh $Username@$MasterNodeIP "mkdir -p $TargetPath"

Write-Host "`n2. Copying files to master node..." -ForegroundColor Yellow
foreach ($file in $filesToCopy) {
    if (Test-Path $file) {
        Write-Host "Copying: $file" -ForegroundColor Cyan
        if (Test-Path $file -PathType Container) {
            # It's a directory
            scp -r $file $Username@$MasterNodeIP`:$TargetPath/
        } else {
            # It's a file
            scp $file $Username@$MasterNodeIP`:$TargetPath/
        }
    } else {
        Write-Warning "File not found: $file"
    }
}

Write-Host "`n3. Setting executable permissions on shell scripts..." -ForegroundColor Yellow
ssh $Username@$MasterNodeIP "chmod +x $TargetPath/deploy-to-k8s.sh $TargetPath/update-and-deploy.sh"

Write-Host "`n4. Verifying files on master node..." -ForegroundColor Yellow
ssh $Username@$MasterNodeIP "ls -la $TargetPath/"

Write-Host "`n=== Files copied successfully! ===" -ForegroundColor Green
Write-Host "`nTo deploy on the master node, run:" -ForegroundColor Cyan
Write-Host "ssh $Username@$MasterNodeIP" -ForegroundColor White
Write-Host "cd $TargetPath" -ForegroundColor White
Write-Host "./deploy-to-k8s.sh" -ForegroundColor White
Write-Host "`nOr to update with latest image:" -ForegroundColor Cyan
Write-Host "./update-and-deploy.sh" -ForegroundColor White
