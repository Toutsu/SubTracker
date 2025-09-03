# Script to update Docker images and restart services in k3s on Raspberry Pi
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

function Check-Deployment-Status {
    Write-Host "=== Checking current deployment status ==="
    
    # Check pods
    Write-Host "=== Pods ==="
    Connect-And-Execute "kubectl get pods -n subtracker"
    
    # Check deployments
    Write-Host "=== Deployments ==="
    Connect-And-Execute "kubectl get deployments -n subtracker"
}

function Build-Docker-Images {
    Write-Host "=== Building new Docker images for ARM architecture ==="
    
    # Use the existing build script
    if (Test-Path "build-arm-images.sh") {
        Write-Host "Found build-arm-images.sh, executing..."
        & ./build-arm-images.sh
    } else {
        Write-Host "build-arm-images.sh not found, building images manually..."
        
        # Create data directory if it doesn't exist
        if (!(Test-Path "data")) {
            New-Item -ItemType Directory -Path "data" | Out-Null
        }
        
        # Build backend image for ARM
        Write-Host "Building backend image..."
        docker buildx build --platform linux/arm/v7 -t subtracker-backend:arm-latest ./backend --load
        
        # Build web-frontend image for ARM
        Write-Host "Building web-frontend image..."
        docker buildx build --platform linux/arm/v7 -t subtracker-web-frontend:arm-latest ./web-frontend --load
        
        # Build telegram-bot image for ARM
        Write-Host "Building telegram-bot image..."
        docker buildx build --platform linux/arm/v7 -t subtracker-telegram-bot:arm-latest ./telegram-bot --load
    }
}

function Save-Images-To-Tar {
    Write-Host "=== Saving Docker images to tar files ==="
    
    # Create a directory for images if it doesn't exist
    if (!(Test-Path "images")) {
        New-Item -ItemType Directory -Path "images" | Out-Null
    }
    
    # Save images to tar files
    Write-Host "Saving backend image..."
    docker save subtracker-backend:arm-latest -o images/subtracker-backend.tar
    
    Write-Host "Saving web-frontend image..."
    docker save subtracker-web-frontend:arm-latest -o images/subtracker-web-frontend.tar
    
    Write-Host "Saving telegram-bot image..."
    docker save subtracker-telegram-bot:arm-latest -o images/subtracker-telegram-bot.tar
    
    Write-Host "All images saved to tar files in images/ directory"
}

function Transfer-Images-To-Raspberry {
    Write-Host "=== Transferring images to Raspberry Pi ==="
    
    # Create images directory on Raspberry Pi
    Connect-And-Execute "mkdir -p ~/subtracker/images"
    
    # Transfer image files using scp
    Write-Host "Transferring backend image..."
    scp images/subtracker-backend.tar ${User}@${IP}:~/subtracker/images/
    
    Write-Host "Transferring web-frontend image..."
    scp images/subtracker-web-frontend.tar ${User}@${IP}:~/subtracker/images/
    
    Write-Host "Transferring telegram-bot image..."
    scp images/subtracker-telegram-bot.tar ${User}@${IP}:~/subtracker/images/
    
    Write-Host "All images transferred to Raspberry Pi"
}

function Load-Images-On-Raspberry {
    Write-Host "=== Loading images on Raspberry Pi ==="
    
    # Load images on Raspberry Pi
    Write-Host "Loading backend image..."
    Connect-And-Execute "docker load -i ~/subtracker/images/subtracker-backend.tar"
    
    Write-Host "Loading web-frontend image..."
    Connect-And-Execute "docker load -i ~/subtracker/images/subtracker-web-frontend.tar"
    
    Write-Host "Loading telegram-bot image..."
    Connect-And-Execute "docker load -i ~/subtracker/images/subtracker-telegram-bot.tar"
    
    # Tag images properly for k3s
    Write-Host "Tagging images..."
    Connect-And-Execute "docker tag subtracker-backend:arm-latest subtracker-backend:latest"
    Connect-And-Execute "docker tag subtracker-web-frontend:arm-latest subtracker/web-frontend:latest"
    Connect-And-Execute "docker tag subtracker-telegram-bot:arm-latest subtracker-telegram-bot:latest"
    
    Write-Host "Images loaded and tagged successfully"
}

function Restart-Services {
    Write-Host "=== Restarting services in k3s ==="
    
    # Restart deployments to use new images
    Write-Host "Restarting backend deployment..."
    Connect-And-Execute "kubectl rollout restart deployment/backend -n subtracker"
    
    Write-Host "Restarting web-frontend deployment..."
    Connect-And-Execute "kubectl rollout restart deployment/web-frontend -n subtracker"
    
    Write-Host "Restarting telegram-bot deployment..."
    Connect-And-Execute "kubectl rollout restart deployment/telegram-bot -n subtracker"
    
    Write-Host "Waiting for deployments to stabilize..."
    Start-Sleep -Seconds 30
    
    # Check rollout status
    Write-Host "Checking rollout status..."
    Connect-And-Execute "kubectl rollout status deployment/backend -n subtracker"
    Connect-And-Execute "kubectl rollout status deployment/web-frontend -n subtracker"
    Connect-And-Execute "kubectl rollout status deployment/telegram-bot -n subtracker"
}

function Check-Final-Status {
    Write-Host "=== Checking final deployment status ==="
    
    # Check pods
    Write-Host "=== Pods ==="
    Connect-And-Execute "kubectl get pods -n subtracker"
    
    # Check deployments
    Write-Host "=== Deployments ==="
    Connect-And-Execute "kubectl get deployments -n subtracker"
    
    # Check services
    Write-Host "=== Services ==="
    Connect-And-Execute "kubectl get services -n subtracker"
}

# Main execution flow
Write-Host "Starting update process for SubTracker deployment on Raspberry Pi ($IP)"

# Connect to Raspberry Pi and check connection
Write-Host "Connecting to $User@$IP"
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

# Check current deployment status
Check-Deployment-Status

# Build new Docker images
Build-Docker-Images

# Save images to tar files
Save-Images-To-Tar

# Transfer images to Raspberry Pi
Transfer-Images-To-Raspberry

# Load images on Raspberry Pi
Load-Images-On-Raspberry

# Restart services to use new images
Restart-Services

# Check final deployment status
Check-Final-Status

Write-Host "Update process completed successfully!"