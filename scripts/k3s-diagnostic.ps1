# Script for k3s diagnostics and troubleshooting on Raspberry Pi

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
    
    return $result
}

function Check-K3s-Status {
    Write-Host "`n=== Checking k3s service status ==="
    $status = Connect-And-Execute "sudo systemctl status k3s"
    Write-Host $status
    
    # Check if service is active
    if ($status -match "active \(running\)") {
        Write-Host "✓ k3s service is running and active"
        return $true
    } else {
        Write-Host "✗ k3s service is not running or has issues"
        return $false
    }
}

function Check-Kubeconfig {
    Write-Host "`n=== Checking kubectl configuration file ==="
    $configCheck = Connect-And-Execute "ls -la ~/.kube/config"
    Write-Host $configCheck
    
    if ($configCheck -match "config") {
        Write-Host "✓ kubectl configuration file found"
        return $true
    } else {
        Write-Host "✗ kubectl configuration file not found"
        return $false
    }
}

function Check-K3s-Port {
    Write-Host "`n=== Checking port k3s is listening on ==="
    $portCheck = Connect-And-Execute "sudo netstat -tlnp | grep k3s"
    Write-Host $portCheck
    
    if ($portCheck -match ":6443") {
        Write-Host "✓ k3s is listening on standard port 6443"
        return $true
    } else {
        Write-Host "✗ k3s is not listening on standard port 6443"
        return $false
    }
}

function Check-K3s-Logs {
    Write-Host "`n=== Checking k3s service logs (last 50 lines) ==="
    $logs = Connect-And-Execute "sudo journalctl -u k3s -n 50 --no-pager"
    Write-Host $logs
    
    # Check for errors
    if ($logs -match "error|Error|ERROR|failed|Failed") {
        Write-Host "⚠ Possible errors found in logs"
        return $false
    } else {
        Write-Host "✓ No critical errors found in logs"
        return $true
    }
}

function Restart-K3s {
    Write-Host "`n=== Restarting k3s service ==="
    $restart = Connect-And-Execute "sudo systemctl restart k3s"
    Write-Host $restart
    
    Write-Host "Waiting 10 seconds after restart..."
    Start-Sleep -Seconds 10
    
    return Check-K3s-Status
}

# Main execution block
Write-Host "k3s diagnostics on Raspberry Pi ($User@$IP)"
Write-Host "=========================================="

# Check connection
Write-Host "`n=== Checking connection ==="
try {
    $connectionTest = Connect-And-Execute "echo 'Connection successful'"
    if ($connectionTest -match "Connection successful") {
        Write-Host "✓ Connection established successfully"
    } else {
        Write-Host "✗ Connection error"
        exit 1
    }
} catch {
    Write-Host "✗ Connection error: $_"
    Write-Host "Make sure that:"
    Write-Host "1. Raspberry Pi is available on the network"
    Write-Host "2. SSH key exists and has correct permissions"
    Write-Host "3. Public key is added to ~/.ssh/authorized_keys on Raspberry Pi"
    exit 1
}

# Perform checks
$k3sStatus = Check-K3s-Status
$kubeconfigStatus = Check-Kubeconfig
$k3sPortStatus = Check-K3s-Port
$k3sLogsStatus = Check-K3s-Logs

# If there are issues, offer to restart the service
if (!($k3sStatus -and $kubeconfigStatus -and $k3sPortStatus)) {
    $restartChoice = Read-Host "`nIssues detected. Restart k3s service? (y/n)"
    if ($restartChoice -eq "y" -or $restartChoice -eq "Y") {
        $newStatus = Restart-K3s
        if ($newStatus) {
            Write-Host "✓ k3s service restarted successfully"
        } else {
            Write-Host "✗ Issues with restarting k3s service"
        }
    }
} else {
    Write-Host "`n✓ All checks passed successfully"
}

Write-Host "`nDiagnostics completed!"