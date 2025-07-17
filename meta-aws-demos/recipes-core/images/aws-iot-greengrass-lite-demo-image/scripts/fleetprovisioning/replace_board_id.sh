#!/bin/bash
# replace_board_id.sh - Script to generate and set a unique device ID
# This script can be run during first boot to generate a unique ID for the device

set -e

# Configuration
CONFIG_FILE="/etc/greengrass/config.d/fleetprovisioning-config.yaml"
LOG_FILE="/var/log/replace_board_id.log"

# Function to log messages
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a $LOG_FILE
}

# Create log file if it doesn't exist
touch $LOG_FILE
log "Starting board ID replacement process"

# Check if config file exists
if [ ! -f "$CONFIG_FILE" ]; then
    log "Error: Config file $CONFIG_FILE not found"
    exit 1
fi

# Generate a unique ID based on available hardware identifiers
generate_unique_id() {
    # Try different methods to get a unique identifier
    
    # Method 1: Use MAC address of first network interface if available
    if [ -d "/sys/class/net" ]; then
        for interface in $(ls /sys/class/net/ | grep -v lo); do
            if [ -f "/sys/class/net/$interface/address" ]; then
                MAC=$(cat "/sys/class/net/$interface/address" | sed 's/://g')
                if [ ! -z "$MAC" ]; then
                    echo "mac-${MAC}"
                    return
                fi
            fi
        done
    fi
    
    # Method 2: Use CPU serial if available
    if [ -f "/proc/cpuinfo" ]; then
        CPU_SERIAL=$(grep -i "Serial" /proc/cpuinfo | awk '{print $3}')
        if [ ! -z "$CPU_SERIAL" ]; then
            echo "cpu-${CPU_SERIAL}"
            return
        fi
    fi
    
    # Method 3: Use device tree serial if available (for embedded devices)
    if [ -f "/proc/device-tree/serial-number" ]; then
        DT_SERIAL=$(cat /proc/device-tree/serial-number | tr -d '\0')
        if [ ! -z "$DT_SERIAL" ]; then
            echo "dt-${DT_SERIAL}"
            return
        fi
    fi
    
    # Method 4: Use DMI board serial if available
    if command -v dmidecode &> /dev/null; then
        DMI_SERIAL=$(dmidecode -s baseboard-serial-number 2>/dev/null)
        if [ ! -z "$DMI_SERIAL" ] && [ "$DMI_SERIAL" != "Unknown" ]; then
            echo "dmi-${DMI_SERIAL}"
            return
        fi
    fi
    
    # Fallback: Use timestamp + random number
    echo "dev-$(date +%s)-$(shuf -i 1000-9999 -n 1)"
}

# Get unique ID
UNIQUE_ID=$(generate_unique_id)
log "Generated unique ID: $UNIQUE_ID"

# Update the config file with the unique ID
if grep -q "SerialNumber" "$CONFIG_FILE"; then
    # Replace the SerialNumber in the config file
    sed -i "s/\"SerialNumber\": \"[^\"]*\"/\"SerialNumber\": \"$UNIQUE_ID\"/" "$CONFIG_FILE"
    log "Updated SerialNumber in $CONFIG_FILE to $UNIQUE_ID"
else
    log "Error: SerialNumber field not found in $CONFIG_FILE"
    exit 1
fi

log "Board ID replacement completed successfully"
exit 0
