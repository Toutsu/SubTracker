# Script to set up SSH key for connecting to Raspberry Pi

# Parameters
$IP = "192.168.50.220"
$User = "hegin"
$Email = "hegin4@yandex.ru"
$SSHDir = "$env:USERPROFILE\.ssh"
$PrivateKeyPath = "$SSHDir\id_rsa"
$PublicKeyPath = "$SSHDir\id_rsa.pub"

# Check for OpenSSH
function Test-OpenSSH {
    $sshPath = Get-Command ssh -ErrorAction SilentlyContinue
    $sshKeyGenPath = Get-Command ssh-keygen -ErrorAction SilentlyContinue
    return ($sshPath -and $sshKeyGenPath)
}

# Install OpenSSH if needed
function Install-OpenSSH {
    Write-Host "OpenSSH not found. Installing OpenSSH..."
    try {
        # Check for administrator rights
        $isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
        if (-not $isAdmin) {
            Write-Host "Administrator rights are required to install OpenSSH. Please run the script as administrator."
            exit 1
        }
        
        # Install OpenSSH Client
        Add-WindowsCapability -Online -Name OpenSSH.Client~~~~0.0.1.0
        Write-Host "OpenSSH installed successfully."
    } catch {
        Write-Host "Error installing OpenSSH: $_"
        exit 1
    }
}

# Check and install OpenSSH
if (-not (Test-OpenSSH)) {
    Install-OpenSSH
}

# Create .ssh directory if it doesn't exist
if (!(Test-Path $SSHDir)) {
    Write-Host "Creating directory $SSHDir"
    New-Item -ItemType Directory -Path $SSHDir -Force
}

# Generate SSH key if it doesn't exist
if (!(Test-Path $PrivateKeyPath)) {
    Write-Host "Generating new SSH key..."
    ssh-keygen -t rsa -b 4096 -C $Email -f $PrivateKeyPath -N '""'
    
    if (Test-Path $PrivateKeyPath) {
        Write-Host "SSH key created successfully at $PrivateKeyPath"
    } else {
        Write-Host "Error creating SSH key"
        exit 1
    }
} else {
    Write-Host "SSH key already exists at $PrivateKeyPath"
}

# Copy public key to Raspberry Pi
Write-Host "Copying public key to Raspberry Pi..."
Write-Host "You will be prompted to enter the password for user $User on Raspberry Pi"

# Temporarily change error action preference to ignore SSH warnings
$originalErrorActionPreference = $ErrorActionPreference
$ErrorActionPreference = "Continue"

# Check if Raspberry Pi is reachable
Write-Host "Checking if Raspberry Pi is reachable at $IP..."
$pingResult = Test-Connection -ComputerName $IP -Count 1 -Quiet
if (-not $pingResult) {
    Write-Host "Raspberry Pi is not reachable at $IP"
    exit 1
}

# Check if plink is available
$plinkAvailable = $false
try {
    $plinkPath = Get-Command plink -ErrorAction Stop
    $plinkAvailable = $true
    Write-Host "plink is available at: $($plinkPath.Path)"
} catch {
    Write-Host "plink is not available in PATH"
}

try {
    # Create .ssh directory on Raspberry Pi if it doesn't exist
    Write-Host "Creating .ssh directory on Raspberry Pi..."
    if ($plinkAvailable) {
        # Use plink if available
        $sshResult = plink -ssh -l $User -host $IP -batch "mkdir -p ~/.ssh"
    } else {
        # Use ssh if plink is not available
        # Use -q flag to suppress warnings and redirect stderr to stdout
        $sshResult = ssh -q -o StrictHostKeyChecking=no -o UserKnownHostsFile=NUL -o ConnectTimeout=10 "$User@$IP" "mkdir -p ~/.ssh" 2>&1
    }
    Write-Host "SSH command result: $sshResult"
    
    # Check if SSH command was successful
    if (!$?) {
        Write-Host "Error creating .ssh directory on Raspberry Pi"
        Write-Host "Last exit code: $LASTEXITCODE"
        throw "SSH mkdir failed"
    }
    
    # Copy public key to Raspberry Pi
    Write-Host "Copying public key to Raspberry Pi..."
    $Destination = "${User}@${IP}:~/.ssh/authorized_keys"
    Write-Host "Copying $PublicKeyPath to $Destination"
    if ($plinkAvailable) {
        # Use pscp if plink is available
        $scpResult = pscp -scp -l $User -host $IP $PublicKeyPath ~/.ssh/authorized_keys
    } else {
        # Use scp if plink is not available
        # Use -q flag to suppress warnings and redirect stderr to stdout
        $scpResult = scp -q -o StrictHostKeyChecking=no -o UserKnownHostsFile=NUL -o ConnectTimeout=10 $PublicKeyPath $Destination 2>&1
    }
    Write-Host "SCP command result: $scpResult"
    
    # Check if SCP command was successful
    if (!$?) {
        Write-Host "Error copying public key to Raspberry Pi"
        Write-Host "Last exit code: $LASTEXITCODE"
        throw "SCP copy failed"
    }
    
    # Set permissions for authorized_keys file
    Write-Host "Setting permissions for authorized_keys..."
    if ($plinkAvailable) {
        # Use plink if available
        $chmod1Result = plink -ssh -l $User -host $IP -batch "chmod 600 ~/.ssh/authorized_keys"
    } else {
        # Use ssh if plink is not available
        # Use -q flag to suppress warnings and redirect stderr to stdout
        $chmod1Result = ssh -q -o StrictHostKeyChecking=no -o UserKnownHostsFile=NUL -o ConnectTimeout=10 "$User@$IP" "chmod 600 ~/.ssh/authorized_keys" 2>&1
    }
    Write-Host "chmod 600 command result: $chmod1Result"
    if (!$?) {
        Write-Host "Error setting permissions for authorized_keys"
        Write-Host "Last exit code: $LASTEXITCODE"
        throw "chmod 600 failed"
    }
    
    # Set permissions for .ssh directory
    Write-Host "Setting permissions for .ssh directory..."
    if ($plinkAvailable) {
        # Use plink if available
        $chmod2Result = plink -ssh -l $User -host $IP -batch "chmod 700 ~/.ssh"
    } else {
        # Use ssh if plink is not available
        # Use -q flag to suppress warnings and redirect stderr to stdout
        $chmod2Result = ssh -q -o StrictHostKeyChecking=no -o UserKnownHostsFile=NUL -o ConnectTimeout=10 "$User@$IP" "chmod 700 ~/.ssh" 2>&1
    }
    Write-Host "chmod 700 command result: $chmod2Result"
    if (!$?) {
        Write-Host "Error setting permissions for .ssh directory"
        Write-Host "Last exit code: $LASTEXITCODE"
        throw "chmod 700 failed"
    }
    
    Write-Host "Public key copied successfully to Raspberry Pi"
} catch {
    Write-Host "Error setting up SSH key: $_"
    Write-Host "Try manually executing the commands:"
    Write-Host "ssh $User@$IP `"mkdir -p ~/.ssh`""
    Write-Host "scp $PublicKeyPath ${User}@${IP}:~/.ssh/authorized_keys"
    Write-Host "ssh $User@$IP `"chmod 600 ~/.ssh/authorized_keys`""
    Write-Host "ssh $User@$IP `"chmod 700 ~/.ssh`""
    exit 1
} finally {
    # Restore original error action preference
    $ErrorActionPreference = $originalErrorActionPreference
}

Write-Host "SSH key setup completed!"
Write-Host "You can now connect to Raspberry Pi without entering a password:"
Write-Host "ssh $User@$IP"
exit 0