# Script to set up SSH key authentication without password prompt

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

# Copy public key to Raspberry Pi using ssh-copy-id equivalent
Write-Host "Setting up SSH key authentication..."
Write-Host "You will need to manually enter the password for user $User on Raspberry Pi"

try {
    # Create .ssh directory on Raspberry Pi if it doesn't exist
    Write-Host "Creating .ssh directory on Raspberry Pi..."
    ssh -q -o StrictHostKeyChecking=no -o UserKnownHostsFile=NUL -o ConnectTimeout=10 "$User@$IP" "mkdir -p ~/.ssh"
    
    # Copy public key to Raspberry Pi
    Write-Host "Copying public key to Raspberry Pi..."
    scp -q -o StrictHostKeyChecking=no -o UserKnownHostsFile=NUL -o ConnectTimeout=10 $PublicKeyPath "${User}@${IP}:~/.ssh/authorized_keys"
    
    # Set permissions for authorized_keys file
    Write-Host "Setting permissions for authorized_keys..."
    ssh -q -o StrictHostKeyChecking=no -o UserKnownHostsFile=NUL -o ConnectTimeout=10 "$User@$IP" "chmod 600 ~/.ssh/authorized_keys"
    
    # Set permissions for .ssh directory
    Write-Host "Setting permissions for .ssh directory..."
    ssh -q -o StrictHostKeyChecking=no -o UserKnownHostsFile=NUL -o ConnectTimeout=10 "$User@$IP" "chmod 700 ~/.ssh"
    
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