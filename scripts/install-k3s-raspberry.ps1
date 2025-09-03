# Script to install k3s on Raspberry Pi and deploy SubTracker services
param(
    [string]$IP = "192.168.50.220",
    [string]$User = "hegin"
)

function Connect-And-Execute {
    param(
        [string]$Command
    )
    
    Write-Host "Executing: $Command"
    
    # Try to use SSH key for authentication
    $SSHKeyPath = "$env:USERPROFILE\.ssh\id_rsa"
    if (Test-Path $SSHKeyPath) {
        $result = ssh -i $SSHKeyPath $User@$IP $Command 2>&1
    } else {
        # If key not found, use standard authentication
        $result = ssh $User@$IP $Command 2>&1
    }
    
    Write-Host $result
    return $result
}

# Connect to Raspberry Pi
Write-Host "Connecting to $User@$IP"

# Check connection
Write-Host "Checking connection..."
try {
    $connectionTest = Connect-And-Execute "echo 'Connection successful'"
    if ($connectionTest -match "Connection successful") {
        Write-Host "Connection established successfully"
    } else {
        Write-Host "Connection error"
        Write-Host "Make sure that:"
        Write-Host "1. Raspberry Pi is available on the network"
        Write-Host "2. Credentials are correct"
        Write-Host "3. SSH key is configured (optional)"
        exit 1
    }
} catch {
    Write-Host "Connection error: $_"
    Write-Host "Make sure that:"
    Write-Host "1. Raspberry Pi is available on the network"
    Write-Host "2. Credentials are correct"
    Write-Host "3. SSH key is configured (optional)"
    exit 1
}

# Check if k3s is installed
Write-Host "Checking if k3s is installed..."
$k3sCheck = Connect-And-Execute "which k3s"
if ($k3sCheck -match "k3s") {
    Write-Host "k3s is already installed"
} else {
    Write-Host "k3s is not installed. Installing k3s..."
    
    # Update system
    Write-Host "Updating system..."
    Connect-And-Execute "sudo apt update && sudo apt upgrade -y"
    
    # Install k3s
    Write-Host "Installing k3s..."
    Connect-And-Execute "curl -sfL https://get.k3s.io | sh -"
    
    # Enable k3s to start on boot
    Write-Host "Enabling k3s to start on boot..."
    Connect-And-Execute "sudo systemctl enable k3s"
}

# Check k3s service status
Write-Host "Checking k3s service status..."
Connect-And-Execute "sudo systemctl status k3s"

# Setup kubectl configuration
Write-Host "Setting up kubectl configuration..."
Connect-And-Execute "mkdir -p ~/.kube"
Connect-And-Execute "sudo cp /etc/rancher/k3s/k3s.yaml ~/.kube/config"
Connect-And-Execute "sudo chown ${USER}:${USER} ~/.kube/config"

# Create necessary directories on Raspberry Pi
Write-Host "Creating necessary directories..."
Connect-And-Execute "mkdir -p ~/subtracker/k8s"

# Copy configuration files to Raspberry Pi
Write-Host "Copying configuration files..."
# Using scp to copy files
scp k8s/*.yaml ${User}@${IP}:~/subtracker/k8s/

# Create namespace if it doesn't exist
Write-Host "Creating namespace subtracker..."
Connect-And-Execute "kubectl create namespace subtracker --dry-run=client -o yaml | kubectl apply -f -"

# Apply configurations
Write-Host "Applying ConfigMap and Secret..."
Connect-And-Execute "kubectl apply -f ~/subtracker/k8s/configmap.yaml -n subtracker"
Connect-And-Execute "kubectl apply -f ~/subtracker/k8s/secret.yaml -n subtracker"

# Apply database manifests
Write-Host "Applying database manifests..."
Connect-And-Execute "kubectl apply -f ~/subtracker/k8s/postgresql.yaml -n subtracker"
Connect-And-Execute "kubectl apply -f ~/subtracker/k8s/redis.yaml -n subtracker"

# Apply application manifests
Write-Host "Applying application manifests..."
Connect-And-Execute "kubectl apply -f ~/subtracker/k8s/backend.yaml -n subtracker"
Connect-And-Execute "kubectl apply -f ~/subtracker/k8s/web-frontend.yaml -n subtracker"
Connect-And-Execute "kubectl apply -f ~/subtracker/k8s/telegram-bot.yaml -n subtracker"

# Apply ingress
Write-Host "Applying ingress..."
Connect-And-Execute "kubectl apply -f ~/subtracker/k8s/ingress.yaml -n subtracker"

Write-Host "Deployment completed!"

# Check deployment status
Write-Host "Checking deployment status..."
# Check pods
Write-Host "=== Pods ==="
Connect-And-Execute "kubectl get pods -n subtracker"

# Check services
Write-Host "=== Services ==="
Connect-And-Execute "kubectl get services -n subtracker"

# Check deployments
Write-Host "=== Deployments ==="
Connect-And-Execute "kubectl get deployments -n subtracker"

Write-Host "Installation and deployment completed successfully!"