# Kubernetes Cluster VM Creation Script
# This script creates 3 Ubuntu VMs: 1 master + 2 workers for Kubernetes

# Configuration
$VMNamePrefix = "k8s"
$MasterVMName = "${VMNamePrefix}-master"
$Worker1VMName = "${VMNamePrefix}-worker1"
$Worker2VMName = "${VMNamePrefix}-worker2"
$VMPath = "C:\HyperV\VMs"
$VHDXPath = "C:\HyperV\VHDs"
$ISOPath = "C:\ISOs\ubuntu-22.04.3-live-server-amd64.iso"
$Memory = 4GB
$ProcessorCount = 2
$DiskSize = 50GB
$SwitchName = "Default Switch"

# Create directories if they don't exist
if (!(Test-Path $VMPath)) {
    New-Item -ItemType Directory -Path $VMPath -Force
    Write-Host "Created VM directory: $VMPath"
}

if (!(Test-Path $VHDXPath)) {
    New-Item -ItemType Directory -Path $VHDXPath -Force
    Write-Host "Created VHDX directory: $VHDXPath"
}

# Function to create VM
function Create-VM {
    param(
        [string]$VMName,
        [string]$VHDXPath,
        [string]$VMPath,
        [string]$ISOPath,
        [long]$Memory,
        [int]$ProcessorCount,
        [long]$DiskSize,
        [string]$SwitchName
    )
    
    Write-Host "Creating VM: $VMName" -ForegroundColor Green
    
    # Create VHDX
    $VHDXFile = Join-Path $VHDXPath "$VMName.vhdx"
    New-VHD -Path $VHDXFile -SizeBytes $DiskSize -Dynamic
    Write-Host "Created VHDX: $VHDXFile"
    
    # Create VM
    New-VM -Name $VMName -MemoryStartupBytes $Memory -VHDPath $VHDXFile -Path $VMPath -Generation 2
    Write-Host "Created VM: $VMName"
    
    # Configure VM settings
    Set-VMProcessor -VMName $VMName -Count $ProcessorCount
    Set-VMMemory -VMName $VMName -DynamicMemoryEnabled $true -MinimumBytes 1GB -MaximumBytes $Memory -StartupBytes $Memory
    
    # Add DVD drive and mount ISO
    Add-VMDvdDrive -VMName $VMName -Path $ISOPath
    
    # Connect to switch
    Connect-VMNetworkAdapter -VMName $VMName -SwitchName $SwitchName
    
    # Enable nested virtualization for Kubernetes
    Set-VMProcessor -VMName $VMName -ExposeVirtualizationExtensions $true
    
    # Set boot order to DVD first
    Set-VMFirmware -VMName $VMName -FirstBootDevice (Get-VMDvdDrive -VMName $VMName)
    
    Write-Host "VM $VMName created successfully!" -ForegroundColor Green
}

# Function to start VM
function Start-VM {
    param([string]$VMName)
    Write-Host "Starting VM: $VMName" -ForegroundColor Yellow
    Start-VM -Name $VMName
}

# Check if Hyper-V is enabled
Write-Host "Checking Hyper-V status..." -ForegroundColor Cyan
if (!(Get-WindowsOptionalFeature -Online -FeatureName Microsoft-Hyper-V-All).Enabled) {
    Write-Host "Hyper-V is not enabled. Please enable it first:" -ForegroundColor Red
    Write-Host "Enable-WindowsOptionalFeature -Online -FeatureName Microsoft-Hyper-V-All -All" -ForegroundColor Yellow
    exit 1
}

# Check if Default Switch exists
if (!(Get-VMSwitch -Name $SwitchName -ErrorAction SilentlyContinue)) {
    Write-Host "Creating Default Switch..." -ForegroundColor Yellow
    New-VMSwitch -Name $SwitchName -SwitchType Internal
}

# Create Master Node
Write-Host "`n=== Creating Master Node ===" -ForegroundColor Magenta
Create-VM -VMName $MasterVMName -VHDXPath $VHDXPath -VMPath $VMPath -ISOPath $ISOPath -Memory $Memory -ProcessorCount $ProcessorCount -DiskSize $DiskSize -SwitchName $SwitchName

# Create Worker Node 1
Write-Host "`n=== Creating Worker Node 1 ===" -ForegroundColor Magenta
Create-VM -VMName $Worker1VMName -VHDXPath $VHDXPath -VMPath $VMPath -ISOPath $ISOPath -Memory $Memory -ProcessorCount $ProcessorCount -DiskSize $DiskSize -SwitchName $SwitchName

# Create Worker Node 2
Write-Host "`n=== Creating Worker Node 2 ===" -ForegroundColor Magenta
Create-VM -VMName $Worker2VMName -VHDXPath $VHDXPath -VMPath $VMPath -ISOPath $ISOPath -Memory $Memory -ProcessorCount $ProcessorCount -DiskSize $DiskSize -SwitchName $SwitchName

# Start all VMs
Write-Host "`n=== Starting all VMs ===" -ForegroundColor Magenta
Start-VM -VMName $MasterVMName
Start-VM -VMName $Worker1VMName
Start-VM -VMName $Worker2VMName

# Display VM status
Write-Host "`n=== VM Status ===" -ForegroundColor Magenta
Get-VM | Where-Object {$_.Name -like "$VMNamePrefix*"} | Format-Table Name, State, Status, CPUCount, MemoryStartup

Write-Host "`n=== Next Steps ===" -ForegroundColor Green
Write-Host "1. Complete Ubuntu installation on each VM" -ForegroundColor White
Write-Host "2. Install Docker on all nodes" -ForegroundColor White
Write-Host "3. Install Kubernetes on all nodes" -ForegroundColor White
Write-Host "4. Initialize master node with: kubeadm init" -ForegroundColor White
Write-Host "5. Join worker nodes with the join command from master" -ForegroundColor White

Write-Host "`nVMs created and started successfully!" -ForegroundColor Green
