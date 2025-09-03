# Script to set up SSH key authentication with automatic password input

# Parameters
$IP = "192.168.50.220"
$User = "hegin"
$Email = "hegin4@yandex.ru"
$SSHDir = "$env:USERPROFILE\.ssh"
$PrivateKeyPath = "$SSHDir\id_rsa"
$PublicKeyPath = "$SSHDir\id_rsa.pub"

# Check if SSH keys exist
if (!(Test-Path $PrivateKeyPath) -or !(Test-Path $PublicKeyPath)) {
    Write-Host "SSH keys not found at $PrivateKeyPath and $PublicKeyPath"
    Write-Host "Please generate SSH keys first using ssh-keygen"
    exit 1
}

Write-Host "SSH keys found:"
Write-Host "Private key: $PrivateKeyPath"
Write-Host "Public key: $PublicKeyPath"

# Prompt for password
$Password = Read-Host "Enter password for user $User on Raspberry Pi" -AsSecureString
$BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($Password)
$PlainPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
[System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($BSTR)

Write-Host "Setting up SSH key authentication with automatic password input..."

# Function to execute SSH command with automatic password input
function Execute-SSHCommand {
    param(
        [string]$Command,
        [string]$Password
    )
    
    # Create process start info
    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = "ssh"
    $psi.Arguments = "-q -o StrictHostKeyChecking=no -o UserKnownHostsFile=NUL -o ConnectTimeout=10 ${User}@${IP} $Command"
    $psi.UseShellExecute = $false
    $psi.RedirectStandardInput = $true
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    
    # Start process
    $process = [System.Diagnostics.Process]::Start($psi)
    
    # Send password to stdin
    $process.StandardInput.WriteLine($Password)
    $process.StandardInput.Close()
    
    # Wait for process to complete
    $process.WaitForExit()
    
    # Return output
    return $process.StandardOutput.ReadToEnd()
}

# Function to execute SCP command with automatic password input
function Execute-SCPCommand {
    param(
        [string]$Source,
        [string]$Destination,
        [string]$Password
    )
    
    # Create process start info
    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = "scp"
    $psi.Arguments = "-q -o StrictHostKeyChecking=no -o UserKnownHostsFile=NUL -o ConnectTimeout=10 $Source $Destination"
    $psi.UseShellExecute = $false
    $psi.RedirectStandardInput = $true
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    
    # Start process
    $process = [System.Diagnostics.Process]::Start($psi)
    
    # Send password to stdin
    $process.StandardInput.WriteLine($Password)
    $process.StandardInput.Close()
    
    # Wait for process to complete
    $process.WaitForExit()
    
    # Return output
    return $process.StandardOutput.ReadToEnd()
}

try {
    # Create .ssh directory on Raspberry Pi
    Write-Host "Creating .ssh directory on Raspberry Pi..."
    Execute-SSHCommand "mkdir -p ~/.ssh" $PlainPassword
    
    # Copy public key to Raspberry Pi
    Write-Host "Copying public key to Raspberry Pi..."
    Execute-SCPCommand $PublicKeyPath "${User}@${IP}:~/.ssh/authorized_keys" $PlainPassword
    
    # Set permissions for authorized_keys file
    Write-Host "Setting permissions for authorized_keys..."
    Execute-SSHCommand "chmod 600 ~/.ssh/authorized_keys" $PlainPassword
    
    # Set permissions for .ssh directory
    Write-Host "Setting permissions for .ssh directory..."
    Execute-SSHCommand "chmod 700 ~/.ssh" $PlainPassword
    
    Write-Host "SSH key authentication setup completed successfully!"
    Write-Host "You can now connect to Raspberry Pi without entering a password:"
    Write-Host "ssh $User@$IP"
} catch {
    Write-Host "Error setting up SSH key authentication: $_"
    Write-Host "Try manually executing the commands:"
    Write-Host "ssh $User@$IP `"mkdir -p ~/.ssh`""
    Write-Host "scp $PublicKeyPath ${User}@${IP}:~/.ssh/authorized_keys"
    Write-Host "ssh $User@$IP `"chmod 600 ~/.ssh/authorized_keys`""
    Write-Host "ssh $User@$IP `"chmod 700 ~/.ssh`""
    exit 1
}