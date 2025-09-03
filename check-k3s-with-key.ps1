# Script to check k3s service status on Raspberry Pi using SSH key

# Connection parameters
$IP = "192.168.50.220"
$User = "hegin"
$SSHKeyPath = "$env:USERPROFILE\.ssh\id_rsa"

function Connect-And-Execute {
    param(
        [string]$Command
    )
    
    Write-Host "Executing: $Command"
    
    # Use SSH key for authentication
    if (Test-Path $SSHKeyPath) {
        $result = ssh -i $SSHKeyPath $User@$IP $Command 2>&1
    } else {
        Write-Host "SSH key not found. Using standard authentication."
        $result = ssh $User@$IP $Command 2>&1
    }
    
    Write-Host $result
    return $result
}

# Connect to Raspberry Pi
Write-Host "Connecting to $User@$IP using SSH key"

# Check connection
Write-Host "Checking connection..."
try {
    $connectionTest = Connect-And-Execute "echo 'Connection successful'"
    if ($connectionTest -match "Connection successful") {
        Write-Host "Connection established successfully"
    } else {
        Write-Host "Connection error"
        exit 1
    }
} catch {
    Write-Host "Connection error: $_"
    Write-Host "Make sure that:"
    Write-Host "1. Raspberry Pi is available on the network"
    Write-Host "2. SSH key exists and has correct permissions"
    Write-Host "3. Public key is added to ~/.ssh/authorized_keys on Raspberry Pi"
    exit 1
}

# Check k3s service status
Write-Host "`n=== Checking k3s service status ==="
Connect-And-Execute "sudo systemctl status k3s"

# Check kubectl configuration file
Write-Host "`n=== Checking kubectl configuration file ==="
Connect-And-Execute "ls -la ~/.kube/config"

# Check which port k3s is listening on
Write-Host "`n=== Checking port k3s is listening on ==="
Connect-And-Execute "sudo netstat -tlnp | grep k3s"

# Check k3s service logs
Write-Host "`n=== Checking k3s service logs (last 50 lines) ==="
Connect-And-Execute "sudo journalctl -u k3s -n 50 --no-pager"

Write-Host "`nCheck completed!"