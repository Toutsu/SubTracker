# Script to check k3s service status on Raspberry Pi

# Connection parameters
$IP = "192.168.50.220"
$User = "hegin"

function Connect-And-Execute {
    param(
        [string]$Command
    )
    
    Write-Host "Executing: $Command"
    $result = ssh $User@$IP $Command 2>&1
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
        exit 1
    }
} catch {
    Write-Host "Connection error: $_"
    exit 1
}

# Check k3s service status
Write-Host "Checking k3s service status..."
Connect-And-Execute "sudo systemctl status k3s"

# Check kubectl configuration file
Write-Host "Checking kubectl configuration file..."
Connect-And-Execute "ls -la ~/.kube/config"

# Check which port k3s is listening on
Write-Host "Checking port k3s is listening on..."
Connect-And-Execute "sudo netstat -tlnp | grep k3s"

# Check k3s service logs
Write-Host "Checking k3s service logs..."
Connect-And-Execute "sudo journalctl -u k3s -n 50 --no-pager"