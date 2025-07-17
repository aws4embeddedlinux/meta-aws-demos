# Fleet Provisioning for AWS IoT Greengrass

This directory contains scripts and templates for setting up fleet provisioning for AWS IoT Greengrass devices.

## Overview

Fleet provisioning allows you to securely provision IoT devices at scale without hardcoding device certificates. The process works as follows:

1. A CloudFormation stack is deployed to set up all necessary AWS resources
2. A claim certificate is generated and used for initial device authentication
3. When a device boots for the first time, it uses the claim certificate to:
   - Generate a unique device certificate
   - Register itself as an IoT thing
   - Join the specified thing group
   - Obtain the necessary policies for Greengrass operation

## Files

- `fleet.sh`: Main script to set up fleet provisioning infrastructure
- `fleet-provisioning-cfn.yaml`: CloudFormation template that creates all required AWS resources
- `ggl.gg_fleetprovisioning.service`: Systemd service that runs the fleet provisioning process on the device

## Usage

1. Run the fleet.sh script to set up the AWS infrastructure:
   ```
   ./fleet.sh
   ```

2. Copy the generated local.conf snippet to your Yocto build's local.conf:
   ```
   cat build/fleetprovisioning/local.conf.sample >> build/conf/local.conf
   ```

3. Build your image with fleet provisioning enabled:
   ```
   bitbake aws-iot-greengrass-lite-demo-image
   ```

4. When the device boots for the first time, the `ggl.gg_fleetprovisioning.service` will:
   - Generate a unique device ID based on hardware identifiers (MAC address, CPU serial, etc.)
   - Update the fleet provisioning configuration with this unique ID
   - Use the claim certificates to authenticate with AWS IoT
   - Register the device using the fleet provisioning template
   - Store the new device certificates
   - Configure Greengrass to use the new certificates

## Unique Device ID Generation

The fleet provisioning service automatically generates a unique device ID using the following methods (in order of preference):

1. MAC address of the first network interface
2. CPU serial number
3. Device tree serial number
4. DMI board serial number
5. Fallback to timestamp + random number

This ensures that each device gets a unique identity during the provisioning process.

## Resources Created

The CloudFormation stack creates the following resources:

- IAM roles for token exchange and fleet provisioning
- IoT policies for claim certificates and device certificates
- IoT role alias for token exchange
- IoT thing group for Greengrass devices
- Fleet provisioning template

## Reference

This implementation is based on the approach described in the article:
[Fleet Provisioning for Embedded Linux Devices with AWS IoT Greengrass](https://dev.to/iotbuilders/fleet-provisioning-for-embedded-linux-devices-with-aws-iot-greengrass-4h8b)
