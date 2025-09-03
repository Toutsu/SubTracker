# Script to connect to Raspberry Pi and update the project
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

# Search for project on server
Write-Host "Searching for SubTracker project..."
$projectPaths = @()
$findCommands = @(
    "find /home -type d -name 'SubTracker' 2>/dev/null",
    "find /var/www -type d -name 'SubTracker' 2>/dev/null", 
    "find /opt -type d -name 'SubTracker' 2>/dev/null"
)

foreach ($cmd in $findCommands) {
    $paths = Connect-And-Execute $cmd
    if ($paths -and $paths.Trim() -ne "") {
        # Split paths by newline and add to array
        $splitPaths = $paths -split "`n"
        foreach ($path in $splitPaths) {
            if ($path -and $path.Trim() -ne "") {
                # Check if this directory contains a .git folder
                $gitCheck = Connect-And-Execute "if [ -d '$path/.git' ]; then echo 'GIT_FOUND'; fi"
                if ($gitCheck -match "GIT_FOUND") {
                    $projectPaths += $path.Trim()
                }
            }
        }
    }
}

if ($projectPaths.Count -eq 0) {
    Write-Host "Project not found on server or .git directory not found"
    exit 1
}

# Use the first found project path
$projectPath = $projectPaths[0].Trim()
Write-Host "Project found: $projectPath"

# Check project status
Write-Host "Checking project status..."
Connect-And-Execute "cd $projectPath && pwd && git status"

# Update code from Git repository
Write-Host "Updating code from Git repository..."
$updateResult = Connect-And-Execute "cd $projectPath && git pull origin main"
Write-Host "Update result: $updateResult"

Write-Host "Update completed"